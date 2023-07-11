package sdk.sahha.android.data.listeners

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener2
import kotlinx.coroutines.launch
import sdk.sahha.android.data.Constants
import sdk.sahha.android.domain.model.dto.StepDto
import sdk.sahha.android.source.Sahha

class StepDetectorListener : SensorEventListener2 {
    private val steps = hashMapOf<Long, Int>()
    private var sessionSteps = 0
    override fun onSensorChanged(sensorEvent: SensorEvent?) {
        sensorEvent?.also { event ->
            val step = event.values[0].toInt()
            val timestampEpoch = Sahha.di.timeManager.nowInEpoch()

            val inSession = checkInSession(timestampEpoch)
            processSteps(inSession)
        }
    }

    private fun processSteps(inSession: Boolean) {
        if (inSession) incrementSessionSteps()
        else processSession()
    }

    private fun incrementSessionSteps() {
        ++sessionSteps
    }

    private fun processSession() {
        Sahha.di.ioScope.launch {
            storeSessionSteps()
            resetSessionSteps()
            incrementSessionSteps()
        }
    }

    private suspend fun storeSessionSteps() {
        Sahha.di.sensorRepo.storeStepDto(
            StepDto(
                dataType = Constants.CUSTOM_STEP_SESSION_DATA_TYPE,
                source = Constants.STEP_DETECTOR_DATA_SOURCE,
                count = steps.count(),
                manuallyEntered = false,
                startDateTime = Sahha.di.timeManager.epochMillisToISO(steps.keys.first()),
                endDateTime = Sahha.di.timeManager.epochMillisToISO(steps.keys.last())
            )
        )
    }

    private fun resetSessionSteps() {
        steps.clear()
        sessionSteps = 0
    }

    private fun checkInSession(timestampEpoch: Long): Boolean {
        val differenceFromLastStepMillis = timestampEpoch - steps.keys.last()
        return differenceFromLastStepMillis < Constants.STEP_SESSION_COOLDOWN_MILLIS
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
    override fun onFlushCompleted(p0: Sensor?) {}
}