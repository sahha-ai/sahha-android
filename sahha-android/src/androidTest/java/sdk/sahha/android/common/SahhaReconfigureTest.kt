package sdk.sahha.android.common

import androidx.test.core.app.ApplicationProvider
import junit.framework.TestCase
import kotlinx.coroutines.test.runTest
import org.junit.Before
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaEnvironment
import sdk.sahha.android.source.SahhaSettings
import java.util.concurrent.CountDownLatch

class SahhaReconfigureTest : TestCase() {
    @Before
    override fun setUp() {
        super.setUp()
        val latch = CountDownLatch(1)
        Sahha.configure(
            ApplicationProvider.getApplicationContext(),
            SahhaSettings(
                SahhaEnvironment.sandbox
            )
        ) { _, _ -> latch.countDown() }
        latch.await()
    }

    fun test_reconfigure() = runTest {
        assertEquals(true, true)
    }
}