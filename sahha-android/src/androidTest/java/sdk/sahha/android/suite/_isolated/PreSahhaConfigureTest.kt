package sdk.sahha.android.suite._isolated

import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaDemographic
import sdk.sahha.android.source.SahhaFramework
import sdk.sahha.android.source.SahhaScoreType
import sdk.sahha.android.source.SahhaSensor
import java.time.LocalDateTime
import java.util.Date
import kotlin.coroutines.resume

class PreSahhaConfigureTest {
    companion object {
        private lateinit var activity: ComponentActivity

        @JvmStatic
        @BeforeClass
        fun beforeClass() = runTest {
            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                activity = ComponentActivity()
            }
        }
    }

    @Test
    fun getSensorStatus_beforeConfigure_returnsCallbackError() = runTest {
        suspendCancellableCoroutine { cont ->
            Sahha.getSensorStatus(activity, setOf(SahhaSensor.sleep)) { error, _ ->
                logErrorSegment(error)
                Assert.assertEquals(SahhaErrors.sahhaNotConfigured, error)
                if (cont.isActive) cont.resume(Unit)
            }
        }
    }

    @Test
    fun authenticateWithIds_beforeConfigure_returnsCallbackError() = runTest {
        suspendCancellableCoroutine { cont ->
            Sahha.authenticate("", "", "") { error, _ ->
                logErrorSegment(error)
                Assert.assertEquals(SahhaErrors.sahhaNotConfigured, error)
                if (cont.isActive) cont.resume(Unit)
            }
        }
    }

    @Test
    fun authenticateWithTokens_beforeConfigure_returnsCallbackError() = runTest {
        suspendCancellableCoroutine { cont ->
            Sahha.authenticate("", "") { error, _ ->
                logErrorSegment(error)
                Assert.assertEquals(SahhaErrors.sahhaNotConfigured, error)
                if (cont.isActive) cont.resume(Unit)
            }
        }
    }

    @Test
    fun deAuthenticate_beforeConfigure_returnsCallbackError() = runTest {
        suspendCancellableCoroutine { cont ->
            Sahha.deauthenticate { error, _ ->
                logErrorSegment(error)
                Assert.assertEquals(SahhaErrors.sahhaNotConfigured, error)
                if (cont.isActive) cont.resume(Unit)
            }
        }
    }

    @Test
    fun analyze_beforeConfigure_returnsCallbackError() = runTest {
        suspendCancellableCoroutine { cont ->
            Sahha.getScores(setOf(SahhaScoreType.activity)) { error, _ ->
                logErrorSegment(error)
                Assert.assertEquals(SahhaErrors.sahhaNotConfigured, error)
                if (cont.isActive) cont.resume(Unit)
            }
        }
    }

    @Test
    fun analyzeDate_beforeConfigure_returnsCallbackError() = runTest {
        suspendCancellableCoroutine { cont ->
            Sahha.getScores(
                setOf(SahhaScoreType.activity),
                Pair(Date(), Date())
            ) { error, _ ->
                logErrorSegment(error)
                Assert.assertEquals(SahhaErrors.sahhaNotConfigured, error)
                if (cont.isActive) cont.resume(Unit)
            }
        }
    }

    @Test
    fun analyzeLocalDateTime_beforeConfigure_returnsCallbackError() = runTest {
        suspendCancellableCoroutine { cont ->
            Sahha.getScores(
                setOf(SahhaScoreType.activity),
                Pair(LocalDateTime.now(), LocalDateTime.now())
            ) { error, _ ->
                logErrorSegment(error)
                Assert.assertEquals(SahhaErrors.sahhaNotConfigured, error)
                if (cont.isActive) cont.resume(Unit)
            }
        }
    }

    @Test
    fun postDemographic_beforeConfigure_returnsCallbackError() = runTest {
        suspendCancellableCoroutine { cont ->
            Sahha.postDemographic(SahhaDemographic()) { error, _ ->
                logErrorSegment(error)
                Assert.assertEquals(SahhaErrors.sahhaNotConfigured, error)
                if (cont.isActive) cont.resume(Unit)
            }
        }
    }

    @Test
    fun enableSensors_beforeConfigure_returnsCallbackError() = runTest {
        suspendCancellableCoroutine { cont ->
            Sahha.enableSensors(activity, setOf(SahhaSensor.sleep)) { error, _ ->
                logErrorSegment(error)
                Assert.assertEquals(SahhaErrors.sahhaNotConfigured, error)
                if (cont.isActive) cont.resume(Unit)
            }
        }
    }

    @Test
    fun postError_beforeConfigure_returnsCallbackError() = runTest {
        suspendCancellableCoroutine { cont ->
            Sahha.postError(SahhaFramework.android_kotlin, "", "", "") { error, _ ->
                logErrorSegment(error)
                Assert.assertEquals(SahhaErrors.sahhaNotConfigured, error)
                if (cont.isActive) cont.resume(Unit)
            }
        }
    }

    private fun logErrorSegment(error: String?) {
        println("*****************************")
        println(error ?: "No error message found")
        println("*****************************")
    }
}