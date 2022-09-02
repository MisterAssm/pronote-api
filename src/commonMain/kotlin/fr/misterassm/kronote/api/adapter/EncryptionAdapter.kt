package fr.misterassm.kronote.api.adapter

import kotlin.random.Random

abstract class EncryptionAdapter {

    var iv = ByteArray(16)
    var tempIv = Random.nextBytes(16)
    var key = buildString {}.encodeToByteArray()

    companion object {
        val DIGITS_LOWER = charArrayOf(
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
            'e', 'f'
        )

        val DIGITS_UPPER = charArrayOf(
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
            'E', 'F'
        )

        internal const val ENCRYPTION_PATTERN = "(onload=\"try . Start )\\((.+)\\) . catch"
    }

    fun encodeHexadecimal(data: ByteArray, toDigits: CharArray = DIGITS_LOWER): CharArray {
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

    abstract fun decodeHexadecimal(data: CharArray): ByteArray

    abstract fun encryptionAES(plainTextByte: ByteArray, byteKey: ByteArray = key): String

    abstract fun decryptionAES(plainText: String, key: ByteArray): ByteArray

    abstract fun retrieveUniqueID(modulo: String, exponent: String): String

    abstract suspend fun executeChallenge(username: String, password: String, alea: String, challenge: String): Boolean

}