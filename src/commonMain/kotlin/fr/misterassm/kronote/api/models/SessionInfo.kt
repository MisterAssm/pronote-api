package fr.misterassm.kronote.api.models

import fr.misterassm.kronote.api.Kronote
import fr.misterassm.kronote.api.adapter.EncryptionAdapter

data class SessionInfo(
    private val kronote: Kronote,
    val sessionId: Long,
) {

    private var functionOrder = -1

    fun findFunctionSessionOrder(encryptionAdapter: EncryptionAdapter): String {
        functionOrder += 2
        return encryptionAdapter.encryptionAES(functionOrder.toString().encodeToByteArray())
    }

}
