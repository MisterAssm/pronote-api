package fr.misterassm.kronote.internal.factories

import fr.misterassm.kronote.api.KronoteSession
import fr.misterassm.kronote.api.adapter.EncryptionAdapter
import fr.misterassm.kronote.internal.impl.JVMEncryptionImpl

actual object EncryptionFactory {

    actual fun createEncryption(kronote: KronoteSession): EncryptionAdapter = JVMEncryptionImpl(kronote)

}