package fr.misterassm.kronote.internal

import fr.misterassm.kronote.api.KronoteSession
import fr.misterassm.kronote.api.models.retrieve.Timetable
import io.ktor.client.*
import kotlinx.datetime.LocalDate
import kotlin.time.Duration

internal actual class KronoteSessionImpl(
    username: String,
    password: String,
    indexUrl: String,
    autoReconnect: Pair<Boolean, Duration>
) : KronoteSession(username, password, indexUrl, autoReconnect) {

    override suspend fun retrieveTimetable(localDate: LocalDate): Timetable {
        TODO("Not yet implemented")
    }

    actual companion object {
        actual val client: HttpClient
            get() = TODO("Not yet implemented")
    }
}