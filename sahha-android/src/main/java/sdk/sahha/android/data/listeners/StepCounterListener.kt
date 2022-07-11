package sdk.sahha.android.data.listeners

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener2
import kotlinx.coroutines.launch
import sdk.sahha.android.domain.model.steps.StepData
import sdk.sahha.android.domain.model.steps.StepDataSource
import sdk.sahha.android.source.Sahha

class StepCounterListener : SensorEventListener2 {
    override fun onSensorChanged(sensorEvent: SensorEvent?) {
        sensorEvent?.also { event ->
            Sahha.di.ioScope.launch {
                val totalSteps = event.values[0].toInt()
                val detectedDateTime = Sahha.timeManager.nowInISO()

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
            StepDataSource.AndroidStepCounter.name,
            totalSteps,
            detectedDateTime
        )

        return null
    }
}