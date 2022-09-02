package fr.misterassm.kronote.internal.factories

import fr.misterassm.kronote.api.KronoteSession
import fr.misterassm.kronote.api.adapter.EncryptionAdapter

expect object EncryptionFactory {

    fun createEncryption(kronote: KronoteSession): EncryptionAdapter

}