package fr.misterassm.kronote.internal

import fr.misterassm.kronote.api.KronoteSession
import fr.misterassm.kronote.api.builder.KronoteBuilder
import fr.misterassm.kronote.api.models.retrieve.Timetable
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.DayOfWeek
import java.time.temporal.WeekFields
import kotlin.time.Duration

internal actual class KronoteSessionImpl(
    username: String,
    password: String,
    indexUrl: String,
    autoReconnect: Pair<Boolean, Duration>
) : KronoteSession(username, password, indexUrl, autoReconnect) {

    actual companion object {
        actual val client = HttpClient(OkHttp)
        internal actual fun constructMultiplatformAbstract(builder: KronoteBuilder): KronoteSessionImpl =
            KronoteSessionImpl(builder.username, builder.password, builder.indexUrl, builder.keepSessionAlive)
    }

    override suspend fun retrieveTimetable(localDate: LocalDate): Timetable {
        return retrieveTimetable(
            localDate.toJavaLocalDate().get(WeekFields.of(DayOfWeek.MONDAY, 7).weekOfYear()) - 34
        ).apply {
            courseList = courseList.filter { it.date.date == localDate }
        }
    }
}