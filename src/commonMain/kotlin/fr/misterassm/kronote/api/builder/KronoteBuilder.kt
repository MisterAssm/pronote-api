package fr.misterassm.kronote.api.builder

import fr.misterassm.kronote.api.Kronote
import fr.misterassm.kronote.internal.KronoteImpl

suspend fun connectKronote(init: KronoteBuilder.() -> Unit): Result<Kronote> = with(KronoteBuilder(init).build()) {
    if (connection()) Result.success(this) else Result.failure(TODO("Login exception"))
}

fun kronote(init: KronoteBuilder.() -> Unit): Kronote {
    return KronoteBuilder(init).build()
}

class KronoteBuilder() {

    constructor(init: KronoteBuilder.() -> Unit) : this() {
        init()
    }

    var username: String = ""
    var password: String = ""
    var indexUrl: String = ""
    var autoReconnect: Boolean = false

    fun build(): Kronote = KronoteImpl(username, password, indexUrl, autoReconnect)

}