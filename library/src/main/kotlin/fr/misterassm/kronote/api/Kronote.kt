package fr.misterassm.kronote.api

import fr.misterassm.kronote.api.models.Page
import fr.misterassm.kronote.api.models.retrieve.Timetable
import kotlinx.serialization.json.JsonElement
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

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

    @NotNull
    fun findStatus(): KronoteStatus

    @NotNull
    fun isAutoReconnect(): Boolean

    @NotNull
    fun connection(): Boolean

    @Nullable
    fun callFunction(function: String, dataMap: Map<String, Any> = mapOf()): JsonElement?

    @NotNull
    fun navigationTo(page: Page, dataMap: Map<String, Any> = mapOf()): JsonElement

    @NotNull
    fun retrieveTimetable(weekNumber: Int? = null): Timetable

}
