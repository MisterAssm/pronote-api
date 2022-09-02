package fr.misterassm.kronote.internal

import fr.misterassm.kronote.api.KronoteSession
import io.ktor.client.*

internal expect class KronoteSessionImpl : KronoteSession {

    companion object {
        val client: HttpClient
    }

}
