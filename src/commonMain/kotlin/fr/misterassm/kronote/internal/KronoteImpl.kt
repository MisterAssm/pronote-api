package fr.misterassm.kronote.internal

import fr.misterassm.kronote.api.Kronote
import kotlinx.serialization.json.Json

internal expect class KronoteImpl(
    username: String,
    password: String,
    indexUrl: String,
    autoReconnect: Boolean,
) : Kronote {

    val username: String
    val password: String
    val indexUrl: String
    val autoReconnect: Boolean

    companion object {
        val json: Json
    }

}


internal fun KronoteImpl.isHelloWorld() = autoReconnect