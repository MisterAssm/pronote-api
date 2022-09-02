package fr.misterassm.kronote.internal

import fr.misterassm.kronote.api.KronoteSession
import fr.misterassm.kronote.api.models.retrieve.Timetable
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.DayOfWeek
import java.time.temporal.WeekFields

internal actual class KronoteSessionImpl(
    username: String,
    password: String,
    indexUrl: String,
    autoReconnect: Boolean
) : KronoteSession(username, password, indexUrl, autoReconnect) {

    actual companion object {
        actual val client = HttpClient(OkHttp)
    }

    override suspend fun retrieveTimetable(localDate: LocalDate): Timetable {
        return retrieveTimetable(localDate.toJavaLocalDate().get(WeekFields.of(DayOfWeek.MONDAY, 7).weekOfYear()) - 34).apply {
            courseList = courseList.filter { it.date.date == localDate }
        }
    }
}