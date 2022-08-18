package fr.misterassm.kronote.api.models

import fr.misterassm.kronote.api.Kronote
import fr.misterassm.kronote.internal.services.EncryptionService

data class SessionInfo(
    private val kronote: Kronote,
    val sessionId: Long,
) {

    private var functionOrder = -1

    fun findFunctionSessionOrder(encryptionService: EncryptionService): String {
        functionOrder += 2

        return encryptionService.encryptionAES(functionOrder.toString().encodeToByteArray())
    }

}
