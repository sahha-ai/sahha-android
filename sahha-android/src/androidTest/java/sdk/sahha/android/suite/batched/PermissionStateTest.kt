package sdk.sahha.android.suite.batched

import androidx.activity.ComponentActivity
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import sdk.sahha.android.common.SahhaSetupUtil
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaEnvironment
import sdk.sahha.android.source.SahhaSensor
import sdk.sahha.android.source.SahhaSensorStatus
import sdk.sahha.android.source.SahhaSettings

class PermissionStateTest {
    companion object {
        private lateinit var activity: ComponentActivity
        val permissions = setOf(
            SahhaSensor.device_lock,
            SahhaSensor.steps,
            SahhaSensor.sleep,
            SahhaSensor.heart_rate,
            SahhaSensor.heart_rate_variability_sdnn,
        )

        @JvmStatic
        @BeforeClass
        fun beforeClass() = runTest {
            activity = ApplicationProvider.getApplicationContext()
            val settings = SahhaSettings(environment = SahhaEnvironment.sandbox)
            SahhaSetupUtil.configureSahha(activity, settings)
            SahhaSetupUtil.enableSensors(activity, SahhaSensor.values().toSet())
        }
    }

    @Test
    fun device_lock() = runTest {
        val deferredResult = CompletableDeferred<Unit>()
        Sahha.sim.permission.getSensorStatus(
            activity,
            setOf(SahhaSensor.device_lock)
        ) { error, status ->
            error?.also { e -> Assert.fail(e) }
            Assert.assertEquals(SahhaSensorStatus.enabled, status)
            deferredResult.complete(Unit)
        }
        deferredResult.await()
    }

    @Test
    fun step_count() = runTest {
        val deferredResult = CompletableDeferred<Unit>()
        Sahha.sim.permission.getSensorStatus(
            activity,
            setOf(SahhaSensor.steps)
        ) { error, status ->
            error?.also { e -> Assert.fail(e) }
            Assert.assertEquals(
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) SahhaSensorStatus.unavailable
                else SahhaSensorStatus.enabled,
                status
            )
            deferredResult.complete(Unit)
        }
        deferredResult.await()
    }

    @Test
    fun sleep() = runTest {
        val deferredResult = CompletableDeferred<Unit>()
        Sahha.sim.permission.getSensorStatus(
            activity,
            setOf(SahhaSensor.sleep)
        ) { error, status ->
            error?.also { e -> Assert.fail(e) }
            Assert.assertEquals(
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) SahhaSensorStatus.unavailable
                else SahhaSensorStatus.enabled,
                status
            )
            deferredResult.complete(Unit)
        }
        deferredResult.await()
    }

    @Test
    fun heart_rate() = runTest {
        val deferredResult = CompletableDeferred<Unit>()
        Sahha.sim.permission.getSensorStatus(
            activity,
            setOf(SahhaSensor.heart_rate)
        ) { error, status ->
            error?.also { e -> Assert.fail(e) }
            Assert.assertEquals(SahhaSensorStatus.enabled, status)
            deferredResult.complete(Unit)
        }
        deferredResult.await()
    }

    @Test
    fun heart_rate_variability_sdnn() = runTest {
        val deferredResult = CompletableDeferred<Unit>()
        Sahha.sim.permission.getSensorStatus(
            activity,
            setOf(SahhaSensor.heart_rate_variability_sdnn)
        ) { error, status ->
            error?.also { e -> Assert.fail(e) }
            Assert.assertEquals(SahhaSensorStatus.enabled, status)
            deferredResult.complete(Unit)
        }
        deferredResult.await()
    }

    @Test
    fun grouped() = runTest {
        val deferredResult = CompletableDeferred<Unit>()
        Sahha.sim.permission.getSensorStatus(activity, permissions) { error, status ->
            error?.also { e -> Assert.fail(e) }
            Assert.assertEquals(SahhaSensorStatus.enabled, status)
            deferredResult.complete(Unit)
        }
        deferredResult.await()
    }
}