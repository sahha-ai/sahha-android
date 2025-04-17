package sdk.sahha.android.suite.batched

import androidx.activity.ComponentActivity
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import sdk.sahha.android.common.SahhaReceiversAndListeners
import sdk.sahha.android.framework.listeners.StepDetectorListener
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaEnvironment
import sdk.sahha.android.source.SahhaSettings

@OptIn(ExperimentalCoroutinesApi::class)
class StepSessionTest {
    companion object {
        lateinit var activity: ComponentActivity
        internal lateinit var stepDetector: StepDetectorListener

        @BeforeClass
        @JvmStatic
        fun beforeClass() {
            activity = ApplicationProvider.getApplicationContext()
            val settings = SahhaSettings(
                environment = SahhaEnvironment.sandbox
            )
            Sahha.configure(activity, settings)
            stepDetector = SahhaReceiversAndListeners.stepDetector
        }
    }

    private fun triggerEvent(amount: Int) {
        for (i in 0 until amount) {
            stepDetector.onSensorChanged(null)
        }
    }

    @Test
    fun stepSessions_isNotProcessed_isTriggeredInMemory50Times() = runTest {
        Sahha.di.sensorRepo.clearAllStepSessions()
        stepDetector.resetSessionSteps()

        triggerEvent(50)
        Assert.assertEquals(50, stepDetector.steps.count())
    }

    @Test
    fun stepSessions_isNotProcessed_hasNoRecordsInLocalDb() = runTest {
        Sahha.di.sensorRepo.clearAllStepSessions()
        stepDetector.resetSessionSteps()

        triggerEvent(50)
        Assert.assertEquals(0, Sahha.di.sensorRepo.getAllStepSessions().count())
    }

    @Test
    fun stepSessions_isProcessed_isStoredLocally() = runTest {
        Sahha.di.sensorRepo.clearAllStepSessions()
        stepDetector.resetSessionSteps()

        triggerEvent(50)
        stepDetector.storeSessionSteps()
        stepDetector.resetSessionSteps()

        Assert.assertEquals(50, Sahha.di.sensorRepo.getAllStepSessions().last().count)
    }

    @Test
    fun stepSessions_isProcessed_isReset() = runTest {
        Sahha.di.sensorRepo.clearAllStepSessions()
        stepDetector.resetSessionSteps()

        triggerEvent(50)
        stepDetector.storeSessionSteps()
        stepDetector.resetSessionSteps()

        Assert.assertEquals(true, stepDetector.steps.isEmpty())
    }
}