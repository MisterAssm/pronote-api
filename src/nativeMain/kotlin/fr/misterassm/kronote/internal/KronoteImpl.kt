package fr.misterassm.kronote.internal

import fr.misterassm.kronote.api.Kronote
import fr.misterassm.kronote.api.models.enum.KronoteStatus
import fr.misterassm.kronote.api.models.enum.PronotePage
import fr.misterassm.kronote.api.models.retrieve.Timetable
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

internal actual class KronoteImpl actual constructor(
    actual val username: String,
    actual val password: String,
    actual val indexUrl: String,
    actual val autoReconnect: Boolean
) : Kronote {

    override fun fetchKronoteStatus(): KronoteStatus {
        TODO("Not yet implemented")
    }

    override fun isAutoReconnect(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun connection(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun callFunction(function: String, dataMap: Map<String, Any>): JsonElement {
        TODO("Not yet implemented")
    }

    override suspend fun navigationTo(pronotePage: PronotePage, dataMap: Map<String, Any>): JsonElement {
        TODO("Not yet implemented")
    }

    override suspend fun retrieveTimetable(weekNumber: Int?): Timetable {
        TODO("Not yet implemented")
    }

    override suspend fun retrieveTimetable(localDate: LocalDate): Timetable {
        TODO("Not yet implemented")
    }

    actual companion object {
        actual val json: Json
            get() = TODO("Not yet implemented")
    }

}