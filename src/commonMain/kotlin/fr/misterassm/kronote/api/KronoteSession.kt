package fr.misterassm.kronote.api

import fr.misterassm.kronote.api.adapter.EncryptionAdapter
import fr.misterassm.kronote.api.adapter.KronoteSessionAdapter
import fr.misterassm.kronote.api.models.Period
import fr.misterassm.kronote.api.models.SessionInfo
import fr.misterassm.kronote.api.models.enum.KronoteStatus
import fr.misterassm.kronote.api.models.enum.PronoteGender.Companion.asPronoteGender
import fr.misterassm.kronote.api.models.enum.PronotePage
import fr.misterassm.kronote.api.models.retrieve.Timetable
import fr.misterassm.kronote.internal.KronoteSessionImpl
import fr.misterassm.kronote.internal.factories.EncryptionFactory
import fr.misterassm.kronote.internal.serializer.LookupSerializer
import fr.misterassm.kronote.internal.tools.intQuoted
import fr.misterassm.kronote.internal.tools.longQuoted
import fr.misterassm.kronote.internal.tools.quotedString
import io.ktor.client.network.sockets.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerializersModule
import kotlin.jvm.JvmOverloads
import kotlin.time.Duration

abstract class KronoteSession(
    val username: String,
    val password: String,
    val indexUrl: String,
    val keepSession: Pair<Boolean, Duration>
) : KronoteSessionAdapter {

    private val encryptionService by lazy { EncryptionFactory.createEncryption(this) }
    private val functionUrl by lazy { indexUrl.replace("eleve.html", "appelfonction/3/") }
    private val periodList by lazy { mutableListOf<Period>() }

    var lastPage = PronotePage.HOME
    var kronoteStatus = KronoteStatus.DISCONNECTED
        private set

    lateinit var sessionInfo: SessionInfo
    var sessionJob: Job? = null

    override fun keepSessionJob(): Result<Job> =
        if (sessionJob == null) Result.failure(IllegalArgumentException("The Kronote object has not enabled keepSession"))
        else Result.success(sessionJob!!)

    override suspend fun initEncryption(): Boolean {
        kronoteStatus = KronoteStatus.INITIALIZING

        KronoteSessionImpl.client.request(indexUrl) {
            header("User-Agent", "User-Agent: $USER_AGENT")
        }.bodyAsText().let {
            EncryptionAdapter.ENCRYPTION_PATTERN.toRegex().find(it)?.let { matchResult ->
                val jsonElement = json.parseToJsonElement(matchResult.groupValues[2])

                sessionInfo = SessionInfo(this, jsonElement.jsonObject["h"]?.longQuoted ?: return false)

                callFunction(
                    "FonctionParametres", mapOf(
                        "Uuid" to encryptionService.retrieveUniqueID(
                            jsonElement.jsonObject["MR"]?.quotedString ?: TODO("THROW"),
                            jsonElement.jsonObject["ER"]?.quotedString ?: TODO("THROW")
                        )
                    )
                )

                encryptionService.apply { iv = tempIv }
                kronoteStatus = KronoteStatus.INITIALIZED
                return true
            }
        }

        return false
    }

    override suspend fun requestAuthentication(username: String, password: String): Boolean {
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
        ).jsonObject.let {
            it.takeIf { ERROR_TOKEN !in it }?.get("donneesSec")?.jsonObject?.get("donnees")?.jsonObject?.let { result ->
                if (encryptionService.executeChallenge(
                        username.lowercase(),
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

    override suspend fun connection(): Boolean = (initEncryption() && requestAuthentication(
        username.lowercase(),
        password
    ).apply { kronoteStatus = KronoteStatus.LOGGING_IN } && kotlin.run {
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
    }).apply {
        kronoteStatus = if (this) KronoteStatus.CONNECTED else KronoteStatus.FAILED_TO_LOGIN
        prepareSessionKeeping()
    }

    override suspend fun disconnect(): Result<Boolean> =
        if (kronoteStatus.isInitialing || kronoteStatus.isConnected()) {
            callFunction("SaisieDeconnexion")
            keepSessionJob().onSuccess { it.cancel() }
            Result.success(true)
        } else Result.failure(IllegalStateException("Kronote is not connected to a Pronote server"))

    override suspend fun callFunction(function: String, dataMap: Map<String, Any>): JsonElement =
        sessionInfo.findFunctionSessionOrder(encryptionService).let { functionSessionOrder ->
            json.parseToJsonElement(KronoteSessionImpl.client.request("$functionUrl${sessionInfo.sessionId}/$functionSessionOrder") {
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

        return with(callFunction(pronotePage.kPageName, dataMap)) {
            this.takeIf { !jsonObject.containsKey(ERROR_TOKEN) } ?: throw ConnectTimeoutException(
                jsonObject[ERROR_TOKEN]?.jsonObject?.get("G")?.intQuoted?.asPronoteGender()?.title
                    ?: "An error has occurred"
            )
        }
    }

    override fun prepareSessionKeeping() {
        if (keepSession.first) {
            sessionJob = with(CoroutineScope(Dispatchers.Default)) {
                launch {
                    delay(keepSession.second)
                    if (kronoteStatus.isConnected()) {
                        callFunction("Presence")
                    }
                }
            }
        }
    }

    override suspend fun retrieveTimetable(weekNumber: Int?): Timetable =
        navigationTo(
            PronotePage.TIMETABLE,
            if (weekNumber != null) mapOf("NumeroSemaine" to weekNumber) else mapOf()
        ).jsonObject["donneesSec"]
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

    companion object {
        internal const val ERROR_TOKEN = "Erreur"
        internal const val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.85 Safari/537.36 Edg/90.0.818.46"

        @OptIn(ExperimentalSerializationApi::class)
        val json: Json = Json {
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