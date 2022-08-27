package fr.misterassm.kronote.internal.tools

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import java.time.format.DateTimeFormatter

actual fun String.toKronoteDate(): LocalDateTime =
    java.time.LocalDateTime.parse(this, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")).toKotlinLocalDateTime()
