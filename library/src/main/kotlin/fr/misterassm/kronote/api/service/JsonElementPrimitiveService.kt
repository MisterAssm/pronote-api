package fr.misterassm.kronote.api.service

import kotlinx.serialization.json.JsonPrimitive
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val JsonPrimitive.quotedString: String get() = content.substring(1, content.length - 1)
val JsonPrimitive.longQuoted: Long get() = quotedString.toLong()
val JsonPrimitive.intQuoted: Long get() = quotedString.toLong()

fun String.toKronoteDate(): LocalDateTime =
    LocalDateTime.parse(this, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
