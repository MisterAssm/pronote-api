package fr.misterassm.kronote.internal

import fr.misterassm.kronote.api.Kronote
import kotlinx.serialization.json.Json

expect class KronoteImpl(
    username: String,
    password: String,
    indexUrl: String,
) : Kronote {

    val username: String
    val password: String
    val indexUrl: String

    companion object {
        val json: Json
    }

}