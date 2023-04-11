package sdk.sahha.android.data.listeners

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener2
import kotlinx.coroutines.launch
import sdk.sahha.android.data.Constants
import sdk.sahha.android.domain.model.steps.StepData
import sdk.sahha.android.source.Sahha

class StepDetectorListener : SensorEventListener2 {
    override fun onSensorChanged(sensorEvent: SensorEvent?) {
        sensorEvent?.also { event ->
            Sahha.di.ioScope.launch {
                val step = event.values[0].toInt()
                val detectedDateTime = Sahha.di.timeManager.nowInISO()

                Sahha.di.movementDao.saveStepData(
                    StepData(
                        Constants.STEP_DETECTOR_DATA_SOURCE,
                        step,
                        detectedDateTime
                    )
                )
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
    override fun onFlushCompleted(p0: Sensor?) {}
}