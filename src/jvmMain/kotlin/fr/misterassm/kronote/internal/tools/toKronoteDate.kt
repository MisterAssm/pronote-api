package fr.misterassm.kronote.internal.tools

import kotlinx.datetime.LocalDateTime
import java.time.format.DateTimeFormatter

actual fun String.toKronoteDate(): LocalDateTime =
    with(java.time.LocalDateTime.parse(this, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))) {
        LocalDateTime(year, monthValue, dayOfMonth, hour, minute, second)
    }
