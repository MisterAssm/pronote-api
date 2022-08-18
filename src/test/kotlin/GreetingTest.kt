import fr.misterassm.kronote.internal.KronoteImpl
import kotlinx.coroutines.*
import kotlinx.coroutines.internal.ThreadSafeHeap
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.suspendCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GreetingTest {

    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    @Test
    fun testGreeting() = runTest {
        val result = promise {
            KronoteImpl(
                "demonstration",
                "pronotevs",
                "https://demo.index-education.net/pronote/eleve.html?login=true"
            ).connection()
        }.await()

        assertTrue(result)
    }
}
