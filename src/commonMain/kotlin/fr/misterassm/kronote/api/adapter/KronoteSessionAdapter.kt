package fr.misterassm.kronote.api.adapter

import fr.misterassm.kronote.api.models.enum.PronotePage
import fr.misterassm.kronote.api.models.retrieve.Timetable
import kotlinx.coroutines.Job
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonElement

interface KronoteSessionAdapter {

    fun keepSessionJob(): Result<Job>

    suspend fun initEncryption(): Boolean

    suspend fun requestAuthentication(username: String, password: String): Boolean

    suspend fun connection(): Boolean

    suspend fun disconnect(): Result<Boolean>

    suspend fun callFunction(function: String, dataMap: Map<String, Any> = mapOf()): JsonElement

    suspend fun navigationTo(pronotePage: PronotePage, dataMap: Map<String, Any> = mapOf()): JsonElement

    fun prepareSessionKeeping()

    suspend fun retrieveTimetable(weekNumber: Int? = null): Timetable

    suspend fun retrieveTimetable(localDate: LocalDate): Timetable

}