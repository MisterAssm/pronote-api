package fr.misterassm.kronote.api.builder

import fr.misterassm.kronote.api.adapter.KronoteSessionAdapter
import fr.misterassm.kronote.internal.KronoteSessionImpl

suspend fun connectKronote(init: KronoteBuilder.() -> Unit): Result<KronoteSessionAdapter> = with(KronoteBuilder(init).build()) {
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
    var autoReconnect: Boolean = false

    fun build(): KronoteSessionAdapter =
        KronoteSessionImpl(username, password, indexUrl, autoReconnect)

}