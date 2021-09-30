package fr.misterassm.kronote.internal

import fr.misterassm.kronote.api.Kronote
import fr.misterassm.kronote.api.Kronote.Companion.ERROR_TOKEN
import fr.misterassm.kronote.api.models.Page
import fr.misterassm.kronote.api.models.Period
import fr.misterassm.kronote.api.models.SessionInfo
import fr.misterassm.kronote.api.models.retrieve.Timetable
import fr.misterassm.kronote.api.service.LookupSerializer
import fr.misterassm.kronote.api.service.longQuoted
import fr.misterassm.kronote.api.service.quotedString
import fr.misterassm.kronote.internal.services.EncryptionService
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerializersModule
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.*

class KronoteImpl(
    private val username: String,
    private val password: String,
    private val indexUrl: String,
) : Kronote {

    private val okHttpClient by lazy { OkHttpClient() }
    private val encryptionService by lazy { EncryptionService(this) }
    private var lastPage = Page.HOME
    private val functionUrl by lazy { indexUrl.replace("eleve.html", "appelfonction/3/") }
    private val periodList by lazy { mutableListOf<Period>() }

    lateinit var sessionInfo: SessionInfo

    companion object {
        const val encryptionPattern =
            "(<body id=\"id_body\" role=\"application\" onload=\"try . Start )\\((.+)\\) . catch"

        @OptIn(ExperimentalSerializationApi::class)
        val json by lazy {
            Json {
                isLenient = true
                ignoreUnknownKeys = true
                coerceInputValues = true
                encodeDefaults = true
                allowStructuredMapKeys = true
                prettyPrint = true
                serializersModule = SerializersModule {
                    contextual(Any::class, LookupSerializer())
                }
            }
        }
    }

    override fun findStatus(): Kronote.KronoteStatus {
        TODO("Not yet implemented")
    }

    override fun isAutoReconnect(): Boolean {
        TODO("Not yet implemented")
    }

    override fun connection(): Boolean {

        return initEncryption() && requestAuthentication(
            username.lowercase(Locale.getDefault()),
            password
        ) && kotlin.run {
            callFunction("ParametresUtilisateur")
                ?.jsonObject?.get("donneesSec")
                ?.jsonObject?.get("donnees")
                ?.jsonObject?.get("ressource")
                ?.jsonObject?.get("listeOngletsPourPeriodes")
                ?.jsonObject?.get("V")
                ?.jsonArray?.get(0)
                ?.jsonObject?.get("listePeriodes")
                ?.jsonObject?.get("V")
                ?.jsonArray?.map {
                    json.decodeFromJsonElement<Period>(it)
                }?.let {
                    return periodList.addAll(it)
                } ?: false
        }
    }

    override fun callFunction(function: String, dataMap: Map<String, Any>): JsonElement? {
        val functionSessionOrder = sessionInfo.findFunctionSessionOrder(encryptionService)

        okHttpClient.newCall(
            Request.Builder()
                .url("$functionUrl${sessionInfo.sessionId}/$functionSessionOrder")
                .post(json.encodeToString(buildJsonObject {
                    put("nom", function)
                    put("numeroOrdre", functionSessionOrder)
                    put("session", sessionInfo.sessionId)
                    putJsonObject("donneesSec") {
                        put("donnees", json.encodeToJsonElement(dataMap))
                        putJsonObject("_Signature_") {
                            put("onglet", lastPage.id)
                        }
                    }
                }).toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()
        ).execute().body?.let {
            return json.parseToJsonElement(it.string())
        } ?: return null
    }

    override fun navigationTo(page: Page, dataMap: Map<String, Any>): JsonElement {
        callFunction(
            "Navigation", mapOf(
                "onglet" to page.id,
                "ongletPrec" to lastPage.kPageName
            )
        )

        callFunction(page.kPageName, dataMap)?.let {

            return when {
                it.jsonObject.containsKey("Erreur") -> TODO("THROW G and Titre")
                else -> it
            }

        } ?: TODO("NULL")
    }

    private fun initEncryption(): Boolean {
        okHttpClient.newCall(
            Request.Builder()
                .url(indexUrl)
                .header(
                    "User-Agent",
                    "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.85 Safari/537.36 Edg/90.0.818.46"
                ).build()
        ).execute().body?.let {

            encryptionPattern.toRegex().find(it.string())?.let { matchResult ->

                val jsonElement = json.parseToJsonElement(matchResult.groupValues[2])

                sessionInfo = SessionInfo(this, jsonElement.jsonObject["h"]?.jsonPrimitive?.longQuoted ?: return false)

                callFunction("FonctionParametres", mapOf(buildString {
                    append("Uuid")
                } to encryptionService.retrieveUUID(
                    jsonElement.jsonObject["MR"]?.jsonPrimitive?.quotedString ?: TODO("THROW"),
                    jsonElement.jsonObject["ER"]?.jsonPrimitive?.quotedString ?: TODO("THROW")
                )))

                encryptionService.apply { iv = tempIv }
                return true
            }

        } ?: return false
    }

    private fun requestAuthentication(username: String, password: String): Boolean {

        callFunction(
            "Identification", mapOf(
                "genreConnexion" to 0,
                "genreEspace" to 3,
                "identifiant" to username.lowercase(Locale.getDefault()),
                "pourENT" to false,
                "enConnexionAuto" to false,
                "demandeConnexionAUto" to false,
                "enConnexionAppliMobile" to false,
                "demandeConnexionAppliMobile" to false,
                "demandeConnexionAppliMobileJeton" to false,
                "uuidAppliMobile" to "",
                "loginTokenSAV" to "",
            )
        )?.jsonObject?.let {
            if (it.containsKey(ERROR_TOKEN)) {
                return false
            }

            it["donneesSec"]?.jsonObject?.get("donnees")?.jsonObject?.let { result ->
                if (encryptionService.executeChallenge(
                        username.lowercase(Locale.getDefault()),
                        password,
                        result["alea"]?.jsonPrimitive?.content ?: TODO("TODO"),
                        result["challenge"]?.jsonPrimitive?.content ?: TODO("TODO")
                    )
                ) {
                    navigationTo(Page.HOME)
                    return true
                }
            }
        }

        return false
    }

    override fun retrieveTimetable(weekNumber: Int?): Timetable =
        navigationTo(Page.TIMETABLE, if (weekNumber != null) mapOf("NumeroSemaine" to weekNumber) else mapOf())
            .jsonObject["donneesSec"]
            ?.jsonObject
            ?.get("donnees")
            ?.let {
                json.decodeFromJsonElement<Timetable>(it)
            } ?: TODO("ERROR")

}

