package fr.misterassm.kronote.api

import fr.misterassm.kronote.api.adapter.RetrieveAdapter
import fr.misterassm.kronote.api.models.enum.KronoteStatus
import fr.misterassm.kronote.api.models.enum.PronotePage
import fr.misterassm.kronote.api.models.retrieve.Timetable
import kotlinx.serialization.json.JsonElement

interface Kronote {

    fun fetchKronoteStatus(): KronoteStatus

    fun isAutoReconnect(): Boolean

    suspend fun connection(): Boolean

    suspend fun callFunction(function: String, dataMap: Map<String, Any> = mapOf()): JsonElement

    suspend fun navigationTo(pronotePage: PronotePage, dataMap: Map<String, Any> = mapOf()): JsonElement

    suspend fun retrieveTimetable(weekNumber: Int? = null): Timetable

    companion object {
        const val ERROR_TOKEN = "Erreur"
    }

}