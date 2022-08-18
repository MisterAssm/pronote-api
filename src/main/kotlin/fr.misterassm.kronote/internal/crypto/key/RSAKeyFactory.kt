package fr.misterassm.kronote.internal.crypto.key

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.soywiz.krypto.SecureRandom
import fr.misterassm.kronote.internal.crypto.key.types.RSAPrivateKey
import fr.misterassm.kronote.internal.crypto.key.types.RSAPublicKey

fun sieveOfEratosthenes(n: Int): List<Int> {
    val prime = BooleanArray(n + 1) { true }
    var p = 2
    while (p * p <= n) {
        if (prime[p]) {
            var i = p * 2
            while (i <= n) {
                prime[i] = false
                i += p
            }
        }
        p++
    }
    val primeNumbers = mutableListOf<Int>()
    for (i in 2..n)
        if (prime[i])
            primeNumbers.add(i)
    return primeNumbers
}

fun gcd(a: Int, b: Int): Int {
    return if (a == 0) b else gcd(b % a, a)
}

/**
 * A factory for RSA keys.
 *
 * @author Lyzev
 * @see RSAPrivateKey
 * @see RSAPublicKey
 * @see RSAKey
 */
object RSAKeyFactory {

    private val primes = sieveOfEratosthenes(2000) // list of primes up to 2000

    /**
     * Generates a new RSA key pair.
     *
     * @return a new RSA key pair
     */
    fun genKeyPair(): Pair<RSAPublicKey, RSAPrivateKey> {
        val random1 = SecureRandom.nextInt(primes.size)
        val random2 = if (SecureRandom.nextBoolean())
            random1 - 1 - SecureRandom.nextInt(random1)
        else
            random1 + 1 + SecureRandom.nextInt(primes.size - random1 - 1)

        val q = primes[random1]
        val p = primes[random2]

        val n = q * p
        val phi = (p - 1) * (q - 1)

        var e = 2
        for (i in 2 until n) {
            e = i
            if (gcd(e, phi) == 1)
                break
        }

        var d = 0
        for (i in 0..9) {
            val multiOfPhi: Int = 1 + i * phi
            if (multiOfPhi % e == 0) {
                d = multiOfPhi / e
                break
            }
        }

        return RSAPublicKey(BigInteger.fromLong(n.toLong()), e) to RSAPrivateKey(BigInteger.fromLong(n.toLong()), d)
    }

    /**
     * Generates key from encoded format.
     *
     * @return a RSA key
     */
    fun genKey(`in`: String): RSAKey {
        return if (`in`.startsWith("RSA-PUBLIC-KEY"))
            genPublicKey(`in`)
        else if (`in`.startsWith("RSA-PUBLIC-KEY"))
            genPrivateKey(`in`)
        else
            throw IllegalArgumentException("Invalid key format")
    }

    /**
     * Generates public key from encoded format.
     *
     * @return a RSA public key
     */
    private fun genPublicKey(`in`: String): RSAPublicKey {
        val n = BigInteger.fromLong(`in`.split(":")[1].toLong())
        val e = `in`.split(":")[2].toInt()
        return RSAPublicKey(n, e)
    }

    /**
     * Generates private key from encoded format.
     *
     * @return a RSA private key
     */
    private fun genPrivateKey(`in`: String): RSAPrivateKey {
        val n = BigInteger.fromLong(`in`.split(":")[1].toLong())
        val d = `in`.split(":")[2].toInt()
        return RSAPrivateKey(n, d)
    }
}