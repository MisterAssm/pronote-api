package fr.misterassm.kronote.internal

import fr.misterassm.kronote.api.Kronote
import fr.misterassm.kronote.api.models.Period
import fr.misterassm.kronote.api.models.SessionInfo
import fr.misterassm.kronote.api.models.enum.KronoteStatus
import fr.misterassm.kronote.api.models.enum.PronotePage
import fr.misterassm.kronote.api.models.retrieve.Timetable
import fr.misterassm.kronote.internal.factories.EncryptionFactory
import fr.misterassm.kronote.internal.serializer.LookupSerializer
import fr.misterassm.kronote.internal.tools.longQuoted
import fr.misterassm.kronote.internal.tools.quotedString
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerializersModule
import java.time.DayOfWeek
import java.time.Month
import java.time.temporal.WeekFields
import java.util.*

internal actual class KronoteImpl actual constructor(
    actual val username: String,
    actual val password: String,
    actual val indexUrl: String,
    actual val autoReconnect: Boolean
) : Kronote {

    actual companion object {
        private val client = HttpClient(OkHttp)
        private const val encryptionPattern =
            "(onload=\"try . Start )\\((.+)\\) . catch"

        @OptIn(ExperimentalSerializationApi::class)
        actual val json: Json by lazy {
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

    private val encryptionService by lazy { EncryptionFactory.createEncryption(this) }
    private var lastPage = PronotePage.HOME
    private val functionUrl by lazy { indexUrl.replace("eleve.html", "appelfonction/3/") } // TODO
    private val periodList by lazy { mutableListOf<Period>() }

    lateinit var sessionInfo: SessionInfo

    private suspend fun initEncryption(): Boolean {
        client.request(indexUrl) {
            header(
                "User-Agent",
                "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.85 Safari/537.36 Edg/90.0.818.46"
            )
        }.bodyAsText().let {
            encryptionPattern.toRegex().find(it)?.let { matchResult ->
                val jsonElement = json.parseToJsonElement(matchResult.groupValues[2])

                sessionInfo = SessionInfo(this, jsonElement.jsonObject["h"]?.jsonPrimitive?.longQuoted ?: return false)

                callFunction("FonctionParametres", mapOf(buildString {
                    append("Uuid")
                } to encryptionService.retrieveUniqueID(
                    jsonElement.jsonObject["MR"]?.jsonPrimitive?.quotedString ?: TODO("THROW"),
                    jsonElement.jsonObject["ER"]?.jsonPrimitive?.quotedString ?: TODO("THROW")
                )))

                encryptionService.apply { iv = tempIv }
                return true
            }
        }

        return false
    }

    private suspend fun requestAuthentication(username: String, password: String): Boolean {

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
        ).jsonObject.let {
            if (it.containsKey(Kronote.ERROR_TOKEN)) {
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
                    navigationTo(PronotePage.HOME)
                    return true
                }
            }
        }

        return false
    }

    override fun fetchKronoteStatus(): KronoteStatus {
        TODO("Not yet implemented")
    }

    override suspend fun connection(): Boolean = initEncryption() && requestAuthentication(
        username.lowercase(Locale.getDefault()),
        password
    ) && kotlin.run {
        callFunction("ParametresUtilisateur")
            .jsonObject["donneesSec"]
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

    override suspend fun callFunction(function: String, dataMap: Map<String, Any>): JsonElement =
        sessionInfo.findFunctionSessionOrder(encryptionService).let { functionSessionOrder ->
            json.parseToJsonElement(client.request("$functionUrl${sessionInfo.sessionId}/$functionSessionOrder") {
                method = HttpMethod.Post
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
            }.bodyAsText())
        }

    override suspend fun navigationTo(pronotePage: PronotePage, dataMap: Map<String, Any>): JsonElement {
        callFunction(
            "Navigation", mapOf(
                "onglet" to pronotePage.id,
                "ongletPrec" to lastPage.kPageName
            )
        )

        callFunction(pronotePage.kPageName, dataMap).let {
            return when {
                it.jsonObject.containsKey("Erreur") -> TODO("THROW G and Titre")
                else -> it
            }
        }
    }

    override suspend fun retrieveTimetable(weekNumber: Int?): Timetable =
        navigationTo(PronotePage.TIMETABLE, if (weekNumber != null) mapOf("NumeroSemaine" to weekNumber) else mapOf())
            .jsonObject["donneesSec"]
            ?.jsonObject
            ?.get("donnees")
            ?.let {
                json.decodeFromJsonElement<Timetable>(it).apply {
                    when (weekNumber) {
                        null -> {
                            it.jsonObject["absences"]?.jsonObject?.get("joursCycle")?.jsonObject?.get("V")?.jsonArray?.first()?.jsonObject?.get(
                                "numeroSemaine"
                            )?.jsonPrimitive?.int?.let { week ->
                                this.weekNumber = week
                            }
                        }
                        else -> this.weekNumber = weekNumber
                    }
                }
            } ?: TODO("ERROR")

    override suspend fun retrieveTimetable(localDate: LocalDate): Timetable {
        return retrieveTimetable(localDate.toJavaLocalDate().get(WeekFields.of(DayOfWeek.MONDAY, 7).weekOfYear()) - 34).apply {
            courseList = courseList.filter { it.date.date == localDate }
        }
    }
}