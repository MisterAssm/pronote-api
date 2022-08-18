package fr.misterassm.kronote.internal.crypto.key.types

import com.ionspin.kotlin.bignum.integer.BigInteger
import fr.misterassm.kronote.internal.crypto.key.RSAKey

/**
 * Represents a public RSA key.
 */
class RSAPublicKey(mod: BigInteger, exp: Int) : RSAKey(mod, exp) {

    /**
     * 1. The exponent should be smaller than the modulus
     * 2. The exponent should be at least 3
     */
    override fun isValid(): Boolean = exp >= 3 && exp < mod.longValue(false)

    override fun encodeToString(): String = "RSA-PUBLIC-KEY:$mod:$exp"
}