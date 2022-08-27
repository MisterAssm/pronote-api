package fr.misterassm.kronote.internal.impl

import fr.misterassm.kronote.api.Kronote
import fr.misterassm.kronote.api.adapter.EncryptionAdapter
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.spec.RSAPublicKeySpec
import java.util.*
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class JVMEncryptionImpl(private val kronote: Kronote) : EncryptionAdapter() {

    companion object {
        val keyFactory = KeyFactory.getInstance("RSA")!!
    }

    override fun decodeHexadecimal(data: CharArray): ByteArray {
        val out = ByteArray(data.size shr 1)
        val len = data.size

        if (len and 0x01 != 0) {
            throw ExceptionInInitializerError("Odd number of characters.")
        }

        val outLen = len shr 1
        if (out.size - 0 < outLen) {
            throw ExceptionInInitializerError("Output array is not large enough to accommodate decoded data.")
        }

        var i = 0
        var j = 0
        while (j < len) {
            var f: Int = Character.digit(data[j], 16) shl 4
            j++
            f = f or Character.digit(data[j], 16)
            j++
            out[i] = (f and 0xFF).toByte()
            i++
        }

        return out
    }

    override fun encryptionAES(plainTextByte: ByteArray, byteKey: ByteArray): String =
        String(encodeHexadecimal(Cipher.getInstance("AES/CBC/PKCS5Padding").apply {
            init(
                Cipher.ENCRYPT_MODE,
                SecretKeySpec(MessageDigest.getInstance("MD5").digest(byteKey), "AES"),
                IvParameterSpec(if (tempIv.contentEquals(this@JVMEncryptionImpl.iv)) MessageDigest.getInstance("MD5").digest(this@JVMEncryptionImpl.iv) else this@JVMEncryptionImpl.iv)
            )
        }.doFinal(plainTextByte)))

    override fun decryptionAES(plainText: String, key: ByteArray): ByteArray =
        Cipher.getInstance("AES/CBC/PKCS5Padding")!!.apply {
            init(
                Cipher.DECRYPT_MODE,
                SecretKeySpec(MessageDigest.getInstance("MD5").digest(key), "AES"),
                IvParameterSpec(MessageDigest.getInstance("MD5").digest(this@JVMEncryptionImpl.iv))
            )
        }.doFinal(decodeHexadecimal(plainText.toCharArray()))

    override fun retrieveUniqueID(modulo: String, exponent: String): String =
        RSAPublicKeySpec(modulo.toBigInteger(16), exponent.toBigInteger(16)).let {
            return Base64.getEncoder().encodeToString(Cipher.getInstance("RSA/ECB/PKCS1Padding")!!.apply {
                init(Cipher.ENCRYPT_MODE, keyFactory.generatePublic(it))
            }.doFinal(tempIv))
        }

    override suspend fun executeChallenge(username: String, password: String, alea: String, challenge: String): Boolean {
        val userKey = (username + String(
            encodeHexadecimal(
                MessageDigest.getInstance("SHA-256").apply {
                    update(alea.toByteArray())
                    update(password.toByteArray())
                }.digest(), DIGITS_UPPER
            )
        )).toByteArray()

        try {

            String(decryptionAES(challenge, userKey), StandardCharsets.UTF_8).also { decrypted ->
                kronote.callFunction(
                    "Authentification", mapOf(
                        "connexion" to 0,
                        "espace" to 3,
                        "challenge" to encryptionAES(StringBuilder().apply {
                            for (i in decrypted.indices) when {
                                i % 2 == 0 -> append(decrypted[i])
                            }
                        }.toString().toByteArray(), userKey),
                    )
                ).jsonObject["donneesSec"]
                    ?.jsonObject?.get("donnees")
                    ?.takeIf { !it.jsonObject.containsKey("Acces") }
                    ?.jsonObject?.get("cle")
                    ?.jsonPrimitive?.content?.let {

                        String(decryptionAES(it, userKey)).split(",".toRegex()).toTypedArray().let { arr ->
                            val out = ByteArray(arr.size)
                            arr.indices.forEach { i -> out[i] = arr[i].toInt().toByte() }
                            this.key = out
                        }

                        return true
                    }

            }

        } catch (exception: BadPaddingException) { // Incorrect credentials
            TODO("Invalid credentials")
        }

        return false
    }
}