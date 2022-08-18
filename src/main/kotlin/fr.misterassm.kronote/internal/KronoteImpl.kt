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
import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerializersModule

class KronoteImpl(
    private val username: String,
    private val password: String,
    private val indexUrl: String,
) : Kronote {

    private val httpClient = HttpClient(Js) {
        install(UserAgent) {
            agent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/90.0.4430.85 Safari/537.36 Edg/90.0.818.46"
        }
    }
    private val encryptionService by lazy { EncryptionService(this) }
    private var lastPage = Page.HOME
    private val functionUrl by lazy { indexUrl.replace("eleve.html", "appelfonction/3/") }
    private val periodList by lazy { mutableListOf<Period>() }

    lateinit var sessionInfo: SessionInfo

    companion object {
        const val encryptionPattern =
            "(onload=\"try . Start )\\((.+)\\) . catch" //TODO: Push specific update encryption pattern

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

    override suspend fun connection(): Boolean {
        return initEncryption() && requestAuthentication(
            username.lowercase(),
            password
        ).apply { println(this) } && kotlin.run {
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

    override suspend fun callFunction(function: String, dataMap: Map<String, Any>): JsonElement? {
        val functionSessionOrder = sessionInfo.findFunctionSessionOrder(encryptionService)

        println(functionSessionOrder)

        return httpClient.post("$functionUrl${sessionInfo.sessionId}/$functionSessionOrder") {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(buildJsonObject {
                put("nom", function)
                put("numeroOrdre", functionSessionOrder)
                put("session", sessionInfo.sessionId)
                putJsonObject("donneesSec") {
                    put("donnees", json.encodeToJsonElement(dataMap))
                    putJsonObject("_Signature_") {
                        put("onglet", lastPage.id)
                    }
                }
            }))
        }.bodyAsText().let {
            json.parseToJsonElement(it)
        }
    }

    override suspend fun navigationTo(page: Page, dataMap: Map<String, Any>): JsonElement {
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

    private suspend fun initEncryption(): Boolean {
        httpClient.get(indexUrl).bodyAsText().let {

            encryptionPattern.toRegex().find(it)?.let { matchResult ->

                val jsonElement = json.parseToJsonElement(matchResult.groupValues[2])

                sessionInfo = SessionInfo(this, jsonElement.jsonObject["h"]?.jsonPrimitive?.longQuoted ?: return false)

                callFunction("FonctionParametres", mapOf(buildString {
                    append("Uuid")
                } to encryptionService.retrieveUUID(
                    jsonElement.jsonObject["MR"]?.jsonPrimitive?.quotedString ?: TODO("THROW"),
                    jsonElement.jsonObject["ER"]?.jsonPrimitive?.quotedString ?: TODO("THROW")
                )))

                println("AFTER")

                encryptionService.apply { iv = tempIv }
                return true
            }

        } ?: return false
    }

    private suspend fun requestAuthentication(username: String, password: String): Boolean {
        callFunction(
            "Identification", mapOf(
                "genreConnexion" to 0,
                "genreEspace" to 3,
                "identifiant" to username.lowercase(),
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
                println(it.toString())
                return false
            }

            it["donneesSec"]?.jsonObject?.get("donnees")?.jsonObject?.let { result ->
                if (encryptionService.executeChallenge(
                        username.lowercase(),
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

    override suspend fun retrieveTimetable(weekNumber: Int?): Timetable =
        navigationTo(Page.TIMETABLE, if (weekNumber != null) mapOf("NumeroSemaine" to weekNumber) else mapOf())
            .jsonObject["donneesSec"]
            ?.jsonObject
            ?.get("donnees")
            ?.let {
                json.decodeFromJsonElement<Timetable>(it).apply {
                    it.jsonObject["absences"]?.jsonObject?.get("joursCycle")?.jsonObject?.get("V")?.jsonArray?.first()?.jsonObject?.get(
                        "numeroSemaine"
                    )?.jsonPrimitive?.int?.let { week ->
                        this.weekNumber = week
                    }
                }
            } ?: TODO("ERROR")

}

