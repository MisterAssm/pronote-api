package fr.misterassm.kronote.internal

import fr.misterassm.kronote.api.KronoteSession
import fr.misterassm.kronote.api.builder.KronoteBuilder
import io.ktor.client.*

internal expect class KronoteSessionImpl : KronoteSession {

    companion object {
        val client: HttpClient
        internal fun constructMultiplatformAbstract(builder: KronoteBuilder): KronoteSessionImpl
    }

}
