package sdk.sahha.android.framework.listeners

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener2
import kotlinx.coroutines.launch
import sdk.sahha.android.common.Constants
import sdk.sahha.android.domain.model.steps.StepData
import sdk.sahha.android.source.Sahha

internal class StepCounterListener : SensorEventListener2 {
    override fun onSensorChanged(sensorEvent: SensorEvent?) {
        sensorEvent?.also { event ->
            Sahha.di.ioScope.launch {
                val totalSteps = event.values[0].toInt()
                val detectedDateTime = Sahha.di.timeManager.nowInISO()

                val stepData = checkStepCountDuplicate(
                    totalSteps,
                    Sahha.di.movementDao.getExistingStepCount(totalSteps),
                    detectedDateTime
                )
                stepData?.also { Sahha.di.movementDao.saveStepData(it) }
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
    override fun onFlushCompleted(p0: Sensor?) {}

    fun checkStepCountDuplicate(
        totalSteps: Int,
        existingStepCount: Int?,
        detectedDateTime: String
    ): StepData? {
        existingStepCount ?: return StepData(
            Constants.STEP_COUNTER_DATA_SOURCE,
            totalSteps,
            detectedDateTime
        )

        return null
    }
}