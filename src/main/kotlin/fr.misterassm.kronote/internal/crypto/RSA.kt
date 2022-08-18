package fr.misterassm.kronote.internal.crypto

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.soywiz.krypto.encoding.Base64
import fr.misterassm.kronote.internal.crypto.key.types.RSAPrivateKey
import fr.misterassm.kronote.internal.crypto.key.types.RSAPublicKey

/**
 * Encrypts and decrypts messages using RSA.
 *
 * @author Lyzev
 * @param publicKey The public key to use for encryption.
 * @param privateKey The private key to use for decryption.
 */
class RSA(val publicKey: RSAPublicKey) {

    fun encrypt(ìn: String): String? = encrypt(ìn.encodeToByteArray())

    fun encrypt(`in`: ByteArray): String {
        val out = mutableListOf<BigInteger>()
        for (byte in `in`)
            out += BigInteger.fromLong(byte.toLong()).pow(publicKey.exp).mod(publicKey.mod)
        return Base64.encode(out.joinToString(" ").encodeToByteArray())
    }
}