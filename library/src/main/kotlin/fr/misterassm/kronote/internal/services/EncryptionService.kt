package fr.misterassm.kronote.internal.services

import fr.misterassm.kronote.api.Kronote
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
import kotlin.random.Random

class EncryptionService(
    private val kronote: Kronote
) {

    var iv = ByteArray(16)
    var tempIv = ByteArray(16)
    var key = buildString {}.toByteArray()

    companion object {
        val keyFactory = KeyFactory.getInstance("RSA")!!
        private val DIGITS_LOWER = charArrayOf(
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
            'e', 'f'
        )
        private val DIGITS_UPPER = charArrayOf(
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
            'E', 'F'
        )
    }

    private fun encodeHex(
        data: ByteArray,
        toDigits: CharArray = DIGITS_LOWER
    ): CharArray {
        val out = CharArray(data.size shl 1)
        var i = 0
        var j = 0

        while (i < 0 + data.size) {
            out[j++] = toDigits[0xF0 and data[i].toInt() ushr 4]
            out[j++] = toDigits[0x0F and data[i].toInt()]
            i++
        }

        return out
    }

    private fun decodeHex(
        data: CharArray
    ): ByteArray {
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

    fun encryptionAES(plainTextByte: ByteArray, byteKey: ByteArray = key): String {
        val secretKeySpec = SecretKeySpec(MessageDigest.getInstance("MD5").digest(byteKey), "AES")
        val ivParameterSpec =
            IvParameterSpec(if (tempIv.contentEquals(iv)) MessageDigest.getInstance("MD5").digest(iv) else iv)

        return String(encodeHex(Cipher.getInstance("AES/CBC/PKCS5Padding").apply {
            init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)
        }.doFinal(plainTextByte)))
    }

    private fun decryptionAES(
        plainText: String,
        key: ByteArray
    ): ByteArray {

        val secretKeySpec = SecretKeySpec(MessageDigest.getInstance("MD5").digest(key), "AES")
        val ivParameterSpec = IvParameterSpec(MessageDigest.getInstance("MD5").digest(iv))

        return Cipher.getInstance("AES/CBC/PKCS5Padding")!!.apply {
            init(
                Cipher.DECRYPT_MODE,
                secretKeySpec,
                ivParameterSpec
            )
        }.doFinal(decodeHex(plainText.toCharArray()))
    }

    fun retrieveUUID(mr: String, er: String): String {
        RSAPublicKeySpec(mr.toBigInteger(16), er.toBigInteger(16)).let {
            Random.nextBytes(tempIv)

            return Base64.getEncoder().encodeToString(Cipher.getInstance("RSA/ECB/PKCS1Padding")!!.apply {
                init(Cipher.ENCRYPT_MODE, keyFactory.generatePublic(it))
            }.doFinal(tempIv))
        }
    }

    fun executeChallenge(
        username: String,
        password: String,
        alea: String,
        challenge: String
    ): Boolean {
        val userKey = (username + String(
            encodeHex(
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
                )?.jsonObject?.get("donneesSec")
                    ?.jsonObject?.get("donnees")
                    ?.takeIf { !it.jsonObject.containsKey("Acces") }
                    ?.jsonObject?.get("cle")
                    ?.jsonPrimitive?.content?.let {

                        val arr = String(
                            decryptionAES(
                                it, userKey
                            )
                        ).split(",".toRegex()).toTypedArray()
                        val out = ByteArray(arr.size)
                        arr.indices.forEach { i -> out[i] = arr[i].toInt().toByte() }

                        this.key = out
                        return true
                    }

            }

        } catch (exception: BadPaddingException) { // Incorrect credentials
            TODO("Invalid credentials")
        }

        return false
    }

}
