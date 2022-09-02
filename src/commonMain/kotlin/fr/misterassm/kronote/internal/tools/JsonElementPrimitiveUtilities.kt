package fr.misterassm.kronote.internal.tools

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

val JsonPrimitive.quotedString: String get() = content.substring(1, content.length - 1)
val JsonPrimitive.longQuoted: Long get() = quotedString.toLong()
val JsonPrimitive.intQuoted: Int get() = quotedString.toInt()
val JsonElement.quotedString: String get() = jsonPrimitive.quotedString

val JsonElement.longQuoted: Long get() = jsonPrimitive.longQuoted

val JsonElement.intQuoted: Int get() = jsonPrimitive.intQuoted

expect fun String.toKronoteDate(): LocalDateTime
