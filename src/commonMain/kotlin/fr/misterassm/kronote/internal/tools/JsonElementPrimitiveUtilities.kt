package fr.misterassm.kronote.internal.tools

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.JsonPrimitive

val JsonPrimitive.quotedString: String get() = content.substring(1, content.length - 1)
val JsonPrimitive.longQuoted: Long get() = quotedString.toLong()
val JsonPrimitive.intQuoted: Long get() = quotedString.toLong()

expect fun String.toKronoteDate(): LocalDateTime
