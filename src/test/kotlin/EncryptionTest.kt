import fr.misterassm.kronote.internal.KronoteImpl
import fr.misterassm.kronote.internal.services.EncryptionService
import io.ktor.utils.io.core.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test

class EncryptionTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testGreeting() = with(
        EncryptionService(
            KronoteImpl(
                "demonstration",
                "pronotevs",
                "https://demo.index-education.net/pronote/eleve.html?login=true"
            )
        )
    ) {

        iv = "5183666c72eec9e4".toByteArray()
        tempIv = "5183666c72eec9e4".toByteArray()

//        assertEquals(encodeHex("Hello world!".encodeToByteArray()).concatToString(), "48656c6c6f20776f726c6421")
//        assertEquals("48656c6c6f20776f726c6421".decodeHex().decodeToString(), "Hello world!")
        val mr =
            "B99B77A3D72D3A29B4271FC7B7300E2F791EB8948174BE7B8024667E915446D4EEA0C2424B8D1EBF7E2DDFF94691C6E994E839225C627D140A8F1146D1B0B5F18A09BBD3D8F421CA1E3E4796B301EEBCCF80D81A32A1580121B8294433C38377083C5517D5921E8A078CDC019B15775292EFDA2C30251B1CCABE812386C893E5"
        val er =
            "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000010001"

        runTest {
            println(
                "retieve : " +
                        retrieveUUID(
                            "B99B77A3D72D3A29B4271FC7B7300E2F791EB8948174BE7B8024667E915446D4EEA0C2424B8D1EBF7E2DDFF94691C6E994E839225C627D140A8F1146D1B0B5F18A09BBD3D8F421CA1E3E4796B301EEBCCF80D81A32A1580121B8294433C38377083C5517D5921E8A078CDC019B15775292EFDA2C30251B1CCABE812386C893E5",
                            "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000010001"
                        )
            )
        }
    }
}
