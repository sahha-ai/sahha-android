package sdk.sahha.android.data.listeners

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener2
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sdk.sahha.android.data.Constants
import sdk.sahha.android.domain.model.steps.StepData
import sdk.sahha.android.domain.model.steps.StepSession
import sdk.sahha.android.source.Sahha

class StepDetectorListener : SensorEventListener2 {
    internal val steps = mutableListOf<Long>()
    internal var sessionJob: Job? = null
    private var timestampEpoch = 0L
    override fun onSensorChanged(sensorEvent: SensorEvent?) {
        timestampEpoch = Sahha.di.timeManager.nowInEpoch()
        incrementSessionSteps()
        processSession()

        // For processing silver layer data
        storeSingleStep()
    }

    private fun storeSingleStep() {
        Sahha.di.ioScope.launch {
            Sahha.di.sensorRepo.saveStepData(
                StepData(
                    Constants.STEP_DETECTOR_DATA_SOURCE,
                    1,
                    Sahha.di.timeManager.nowInISO()
                )
            )
        }
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

    internal suspend fun storeSessionSteps() {
        if (steps.isNotEmpty()) {
            Sahha.di.sensorRepo.saveStepSession(
                StepSession(
                    count = steps.count(),
                    startDateTime = Sahha.di.timeManager.epochMillisToISO(steps.first()),
                    endDateTime = Sahha.di.timeManager.epochMillisToISO(steps.last())
                )
            )
        }
    }

    internal fun resetSessionSteps() {
        steps.clear()
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
    override fun onFlushCompleted(p0: Sensor?) {}
}