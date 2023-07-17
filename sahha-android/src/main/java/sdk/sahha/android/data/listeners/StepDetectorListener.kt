package sdk.sahha.android.data.listeners

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener2
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sdk.sahha.android.data.Constants
import sdk.sahha.android.domain.model.steps.StepSession
import sdk.sahha.android.source.Sahha

class StepDetectorListener : SensorEventListener2 {
    private val steps = mutableListOf<Long>()
    private var timestampEpoch = 0L
    private var sessionJob: Job? = null
    override fun onSensorChanged(sensorEvent: SensorEvent?) {
        timestampEpoch = Sahha.di.timeManager.nowInEpoch()
        incrementSessionSteps()
        processSession()
    }

    private fun incrementSessionSteps() {
        steps.add(timestampEpoch)
    }

    private fun processSession() {
        sessionJob?.cancel()
        sessionJob = Sahha.di.ioScope.launch {
            delay(Constants.STEP_SESSION_COOLDOWN_MILLIS)
            storeSessionSteps()
            resetSessionSteps()
        }
    }

    private suspend fun storeSessionSteps() {
        if (steps.isNotEmpty()) {
            Sahha.di.sensorRepo.storeStepSession(
                StepSession(
                    count = steps.count(),
                    startDateTime = Sahha.di.timeManager.epochMillisToISO(steps.first()),
                    endDateTime = Sahha.di.timeManager.epochMillisToISO(steps.last())
                )
            )
        }
    }

    private fun resetSessionSteps() {
        steps.clear()
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
    override fun onFlushCompleted(p0: Sensor?) {}
}