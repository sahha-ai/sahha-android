package sdk.sahha.android.common

import android.app.Service
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener2
import android.hardware.SensorManager
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.launch
import sdk.sahha.android.domain.model.steps.StepData
import sdk.sahha.android.domain.model.steps.StepDataSource
import sdk.sahha.android.domain.receiver.PhoneScreenStateReceiver
import sdk.sahha.android.source.Sahha

object SahhaReceiversAndListeners {
    //Receivers
    @RequiresApi(Build.VERSION_CODES.O)
    val screenLocks = PhoneScreenStateReceiver()

    //Listeners
    val stepDetector = object : SensorEventListener2 {
        override fun onSensorChanged(sensorEvent: SensorEvent?) {
            sensorEvent?.also { event ->
                Sahha.di.ioScope.launch {
                    val step = event.values[0].toInt()
                    val detectedDateTime = Sahha.timeManager.nowInISO()

                    Sahha.di.movementDao.saveStepData(
                        StepData(
                            StepDataSource.AndroidStepDetector.name,
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

    val stepCounter = object : SensorEventListener2 {
        override fun onSensorChanged(sensorEvent: SensorEvent?) {
            sensorEvent?.also { event ->
                Sahha.di.ioScope.launch {
                    val totalSteps = event.values[0].toInt()
                    val detectedDateTime = Sahha.timeManager.nowInISO()

                    Sahha.di.movementDao.saveStepData(
                        StepData(
                            StepDataSource.AndroidStepCounter.name,
                            totalSteps,
                            detectedDateTime
                        )
                    )
                }
            }
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
        override fun onFlushCompleted(p0: Sensor?) {}
    }
}