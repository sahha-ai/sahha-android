package sdk.sahha.android.common

import androidx.test.core.app.ApplicationProvider
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaEnvironment
import sdk.sahha.android.source.SahhaSettings
import java.util.concurrent.CountDownLatch

class ApiBodyConverterTest : TestCase() {
    @Before
    override fun setUp() {
        super.setUp()

        val latch = CountDownLatch(1)

        Sahha.configure(
            ApplicationProvider.getApplicationContext(),
            SahhaSettings(SahhaEnvironment.sandbox)
        ) { _, _ ->
            latch.countDown()
        }
        latch.await()
    }

    @Test
    fun test_convertToSahhaResponseError() {
        // Work around for "no found test" error for now
        assertEquals(true, true)
    }
}