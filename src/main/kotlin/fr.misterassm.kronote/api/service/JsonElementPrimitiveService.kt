package fr.misterassm.kronote.api.service

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.JsonPrimitive

val JsonPrimitive.quotedString: String get() = content.substring(1, content.length - 1)
val JsonPrimitive.longQuoted: Long get() = quotedString.toLong()
val JsonPrimitive.intQuoted: Long get() = quotedString.toLong()

fun String.toKronoteDate(): LocalDateTime = LocalDateTime(2022, 1, 1, 1, 1, 1)//TODO: DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").parse(this)
