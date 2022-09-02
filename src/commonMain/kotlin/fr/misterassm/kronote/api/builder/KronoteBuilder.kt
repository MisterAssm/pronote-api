package fr.misterassm.kronote.api.builder

import fr.misterassm.kronote.api.adapter.KronoteSessionAdapter
import fr.misterassm.kronote.internal.KronoteSessionImpl
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

suspend fun connectKronote(init: KronoteBuilder.() -> Unit): Result<KronoteSessionAdapter> =
    with(KronoteBuilder(init).build()) {
        if (connection()) Result.success(this) else Result.failure(TODO("Login exception"))
    }

fun kronote(init: KronoteBuilder.() -> Unit): KronoteSessionAdapter {
    return KronoteBuilder(init).build()
}

class KronoteBuilder() {

    constructor(init: KronoteBuilder.() -> Unit) : this() {
        init()
    }

    var username: String = ""
    var password: String = ""
    var indexUrl: String = ""
    var keepSessionAlive: Pair<Boolean, Duration> = false to 120.seconds

    fun enableKeepAlive() {
        keepSessionAlive = true to 120.seconds
    }

    fun build(): KronoteSessionAdapter =
        KronoteSessionImpl(username, password, indexUrl, keepSessionAlive)

}