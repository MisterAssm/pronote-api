package fr.misterassm.kronote.internal.factories

import fr.misterassm.kronote.api.Kronote
import fr.misterassm.kronote.api.adapter.EncryptionAdapter
import fr.misterassm.kronote.internal.impl.JVMEncryptionImpl

actual object EncryptionFactory {

    actual fun createEncryption(kronote: Kronote): EncryptionAdapter = JVMEncryptionImpl(kronote)

}