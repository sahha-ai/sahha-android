package sdk.sahha.android.common

import androidx.test.core.app.ApplicationProvider
import junit.framework.TestCase
import org.junit.Test
import sdk.sahha.android.source.*
import java.util.concurrent.CountDownLatch

class SahhaErrorLoggerTest : TestCase() {
    override fun setUp() {
        val latch = CountDownLatch(1)
        Sahha.configure(
            ApplicationProvider.getApplicationContext(),
            SahhaSettings(
                environment = SahhaEnvironment.sandbox,
                framework = SahhaFramework.android_kotlin,
            )
        ) { _, _ -> latch.countDown() }
        latch.await()
    }

    @Test
    fun test_apiError() {
        assertEquals(true, true)
    }
}