package fr.misterassm.kronote.internal.impl

import fr.misterassm.kronote.api.KronoteSession
import fr.misterassm.kronote.api.adapter.EncryptionAdapter

internal class JSEncryptionImpl(private val kronote: KronoteSession) : EncryptionAdapter() {

    override fun decodeHexadecimal(data: CharArray): ByteArray {
        TODO("Not yet implemented")
    }

    override fun encryptionAES(plainTextByte: ByteArray, byteKey: ByteArray): String {
        val cipherIv = tempIv.contentEquals(iv)
        val stringKey = byteKey.decodeToString()
        val jsIv = iv
        val jsIvString = iv.decodeToString()

        return encodeHexadecimal(
            js(
                "var md5 = require('md5');\n" +
                        "var aesjs = require('aes-js');\n" +
                        "" +
                        "var aesCbc = new aesjs.ModeOfOperation.cbc(md5(stringKey), cipherIv ? md5(jsIvString) : jsIv);\n" +
                        "return aesCbc.encrypt(plainTextByte)"
            ) as ByteArray
        ).concatToString()
    }

    override fun decryptionAES(plainText: String, key: ByteArray): ByteArray {
        val stringKey = key.decodeToString()
        val jsIvString = iv.decodeToString()
        val plaintTextByte = decodeHexadecimal(plainText.toCharArray())

        return js(
            "var md5 = require('md5');\n" +
                    "var aesjs = require('aes-js');\n" +
                    "" +
                    "var aesCbc = new aesjs.ModeOfOperation.cbc(md5(stringKey), md5(jsIvString));\n" +
                    "return aesCbc.decrypt(plaintTextByte)"
        ) as ByteArray
    }

    override fun retrieveUniqueID(modulo: String, exponent: String): String {
        TODO("Not yet implemented")
    }

    override suspend fun executeChallenge(
        username: String,
        password: String,
        alea: String,
        challenge: String
    ): Boolean {
        TODO("Not yet implemented")
    }
}