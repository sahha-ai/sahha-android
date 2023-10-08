package sdk.sahha.android

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import sdk.sahha.android.source.Sahha
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testQueryTimer() = runTest {
        val defaultScope = CoroutineScope(Dispatchers.Default)
        var result = 0
        suspendCoroutine { cont ->
            defaultScope.launch {
                val j1 = launch {
                    delay(3000)
                    result = 3
                }

                val j2 = launch {
                    delay(2000)
                    result = 2
                }
                val jobs = listOf(j1, j2)
                jobs.joinAll()
                assertEquals(3, result)
                cont.resume(Unit)
            }
        }
    }
}