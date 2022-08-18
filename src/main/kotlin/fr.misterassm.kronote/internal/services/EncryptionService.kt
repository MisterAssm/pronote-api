package fr.misterassm.kronote.internal.services

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.soywiz.krypto.*
import com.soywiz.krypto.encoding.Base64
import fr.misterassm.kronote.api.Kronote
import fr.misterassm.kronote.internal.crypto.key.types.RSAPublicKey
import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.random.Random

class EncryptionService(
    private val kronote: Kronote
) {

    var iv = ByteArray(16)
    var tempIv = ByteArray(16)
    var key = buildString {}.encodeToByteArray()

    companion object {
        private val DIGITS_LOWER = charArrayOf(
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
            'e', 'f'
        )
        private val DIGITS_UPPER = charArrayOf(
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
            'E', 'F'
        )
    }

    fun encodeHex(
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

    fun String.decodeHex(): ByteArray {
        //check(length and 0x01 != 0) { "Must have an even length" }

        return chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

    fun encryptionAES(plainTextByte: ByteArray, byteKey: ByteArray = key): String {
        return encodeHex(
            AES.encryptAesCbc(
                plainTextByte,
                MD5.digest(byteKey).bytes,
                if (tempIv.contentEquals(iv)) MD5.digest(iv).bytes else iv,
                CipherPadding.PKCS7Padding
            )
        ).concatToString()
    }

    fun decryptionAES(
        plainText: String,
        key: ByteArray
    ): ByteArray = AES.decryptAesCbc(
        plainText.decodeHex(),
        MD5.digest(key).bytes,
        MD5.digest(iv).bytes,
        CipherPadding.PKCS7Padding
    )

    private fun padV15(var1: ByteArray, var2: Int = 0, var3: Int = 16): ByteArray {
        val var4 = ByteArray(128)

        var1.copyInto(var4, startIndex = var2, destinationOffset = 128 - var3)

        var var5: Int = 128 - 3 - var3
        var var6 = 0
        var4[var6++] = 0
        var4[var6++] = 2.toByte()

        val var7 = ByteArray(64)
        var var9: Int
        var var8 = -1
        while (var5-- > 0) {
            do {
                if (var8 < 0) {
                    Random.nextBytes(var7)
                    var8 = var7.size - 1
                }
                var9 = var7[var8--].toInt() and 255
            } while (var9 == 0)
            var4[var6++] = var9.toByte()
        }

        return var4
    }

    suspend fun retrieveUUID(mr: String, er: String): String {
        println("MR: $mr")
        println("ER: $er")

        // BigInteger.fromLong(byte.toLong()).pow(publicKey.exp).mod(publicKey.mod)

        return Base64.encode(with(RSAPublicKey(BigInteger.parseString(mr, 16), er.toInt(16))) {
            padV15(tempIv).apply { println("size = $size") }.asIterable().asFlow()
                .map { BigInteger.fromLong(it.toLong())/*.pow(exp).mod(mod)*/ } //TODO: #fromByte
                .toList() // TODO: to set ?
                .joinToString(" ")
                .encodeToByteArray()
        })
    }

    suspend fun executeChallenge(
        username: String,
        password: String,
        alea: String,
        challenge: String
    ): Boolean {
        val userKey = (username + encodeHex(
            SHA256.create().also {
                it.update(alea.encodeToByteArray())
                it.update(password.encodeToByteArray())
            }.digest().bytes, DIGITS_UPPER
        ).concatToString()).encodeToByteArray()

        try {

            decryptionAES(challenge, userKey).decodeToString().also { decrypted ->
                kronote.callFunction(
                    "Authentification", mapOf(
                        "connexion" to 0,
                        "espace" to 3,
                        "challenge" to encryptionAES(StringBuilder().apply {
                            for (i in decrypted.indices) when {
                                i % 2 == 0 -> append(decrypted[i])
                            }
                        }.toString().encodeToByteArray(), userKey),
                    )
                )?.jsonObject?.get("donneesSec")
                    ?.jsonObject?.get("donnees")
                    ?.takeIf { !it.jsonObject.containsKey("Acces") }
                    ?.jsonObject?.get("cle")
                    ?.jsonPrimitive?.content?.let {

                        val arr = decryptionAES(
                            it, userKey
                        ).decodeToString().split(",".toRegex()).toTypedArray()
                        val out = ByteArray(arr.size)
                        arr.indices.forEach { i -> out[i] = arr[i].toInt().toByte() }

                        this.key = out
                        return true
                    }

            }

        } catch (exception: Exception) { // Incorrect credentials
            TODO("Invalid credentials")
        }

        return false
    }

}


