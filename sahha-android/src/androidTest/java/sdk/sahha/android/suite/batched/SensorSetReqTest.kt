package sdk.sahha.android.suite.batched

import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaEnvironment
import sdk.sahha.android.source.SahhaSensor
import sdk.sahha.android.source.SahhaSettings
import kotlin.coroutines.resume

class SensorSetReqTest {
    companion object {
        @JvmStatic
        @BeforeClass
        fun before(): Unit = runTest {
            val settings = SahhaSettings(environment = SahhaEnvironment.sandbox)
            Sahha.configure(ApplicationProvider.getApplicationContext(), settings)
        }
    }

    @Test
    fun enableSensors_emptySet_returnsEmptySetError() = runTest {
        val err = suspendCancellableCoroutine { cont ->
            Sahha.enableSensors(
                ApplicationProvider.getApplicationContext(),
                emptySet()
            ) { error, status ->
                if (cont.isActive) cont.resume(error)
            }
        }

        Assert.assertEquals(SahhaErrors.sensorSetEmpty, err)
    }

    @Test
    fun enableSensors_singleSensor_noError() = runTest {
        val err = suspendCancellableCoroutine { cont ->
            Sahha.enableSensors(
                ApplicationProvider.getApplicationContext(),
                setOf(SahhaSensor.steps)
            ) { error, status ->
                if (cont.isActive) cont.resume(error)
            }
        }

        Assert.assertEquals(null, err)
    }

    @Test
    fun enableSensors_multipleSensors_noError() = runTest {
        val err = suspendCancellableCoroutine { cont ->
            Sahha.enableSensors(
                ApplicationProvider.getApplicationContext(),
                setOf(SahhaSensor.steps, SahhaSensor.sleep, SahhaSensor.heart_rate)
            ) { error, status ->
                if (cont.isActive) cont.resume(error)
            }
        }

        Assert.assertEquals(null, err)
    }

    @Test
    fun enableSensors_allSensors_noError() = runTest {
        val err = suspendCancellableCoroutine { cont ->
            Sahha.enableSensors(
                ApplicationProvider.getApplicationContext(),
                SahhaSensor.values().toSet()
            ) { error, status ->
                if (cont.isActive) cont.resume(error)
            }
        }

        Assert.assertEquals(null, err)
    }

    @Test
    fun getSensorStatus_emptySet_returnsEmptySetError() = runTest {
        val err = suspendCancellableCoroutine { cont ->
            Sahha.getSensorStatus(
                ApplicationProvider.getApplicationContext(),
                emptySet()
            ) { error, status ->
                if (cont.isActive) cont.resume(error)
            }
        }

        Assert.assertEquals(SahhaErrors.sensorSetEmpty, err)
    }

    @Test
    fun getSensorStatus_singleSensor_noError() = runTest {
        val err = suspendCancellableCoroutine { cont ->
            Sahha.getSensorStatus(
                ApplicationProvider.getApplicationContext(),
                setOf(SahhaSensor.steps)
            ) { error, status ->
                if (cont.isActive) cont.resume(error)
            }
        }

        Assert.assertEquals(null, err)
    }

    @Test
    fun getSensorStatus_multipleSensors_noError() = runTest {
        val err = suspendCancellableCoroutine { cont ->
            Sahha.getSensorStatus(
                ApplicationProvider.getApplicationContext(),
                setOf(SahhaSensor.steps, SahhaSensor.sleep, SahhaSensor.heart_rate)
            ) { error, status ->
                if (cont.isActive) cont.resume(error)
            }
        }

        Assert.assertEquals(null, err)
    }

    @Test
    fun getSensorStatus_allSensors_noError() = runTest {
        val err = suspendCancellableCoroutine { cont ->
            Sahha.getSensorStatus(
                ApplicationProvider.getApplicationContext(),
                SahhaSensor.values().toSet()
            ) { error, status ->
                if (cont.isActive) cont.resume(error)
            }
        }

        Assert.assertEquals(null, err)
    }
}