package fr.misterassm.kronote.api

import fr.misterassm.kronote.api.models.Page
import fr.misterassm.kronote.api.models.retrieve.Timetable
import kotlinx.serialization.json.JsonElement

interface Kronote {

    companion object {
        const val ERROR_TOKEN = "Erreur"
    }

    /**
     * Represents the connection status of Kronote to the Index-Education account
     */
    enum class KronoteStatus(val isInitialing: Boolean = false) {
        /**
         * Kronote is currently setting up support systems and libraries.
         */
        INITIALIZING(true),

        /**
         * Kronote has finished setting up the support systems and is ready to connect.
         */
        INITIALIZED(true),
        LOGGING_IN(true),
        CONNECTED(true),
        DISCONNECTED(),
        FAILED_TO_LOGIN()
    }

    fun findStatus(): KronoteStatus

    fun isAutoReconnect(): Boolean

    suspend fun connection(): Boolean

    suspend fun callFunction(function: String, dataMap: Map<String, Any> = mapOf()): JsonElement?

    suspend fun navigationTo(page: Page, dataMap: Map<String, Any> = mapOf()): JsonElement

    suspend fun retrieveTimetable(weekNumber: Int? = null): Timetable

}
