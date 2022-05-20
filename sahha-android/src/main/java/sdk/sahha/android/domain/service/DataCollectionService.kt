package sdk.sahha.android.domain.service

import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener2
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.data.Constants.AVG_STEP_DISTANCE
import sdk.sahha.android.data.Constants.NOTIFICATION_DATA_COLLECTION
import sdk.sahha.android.data.local.dao.MovementDao
import sdk.sahha.android.domain.model.config.SahhaConfiguration
import sdk.sahha.android.source.SahhaSensor
import sdk.sahha.android.domain.model.steps.DetectedSteps
import sdk.sahha.android.domain.model.steps.LastDetectedSteps

@RequiresApi(Build.VERSION_CODES.O)
class DataCollectionService : Service() {
    private val tag by lazy { "DataCollectionService" }
    private var pedometerRegistered = false
    private var screenLocksRegistered = false
    private lateinit var config: SahhaConfiguration

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        CoroutineScope(Default).launch {
            SahhaReconfigure(this@DataCollectionService.applicationContext)
            config = Sahha.di.configurationDao.getConfig() ?: return@launch

            val notificationConfig = Sahha.di.configurationDao.getNotificationConfig()
            Sahha.di.notifications.setNewPersistent(
                notificationConfig.icon,
                notificationConfig.title,
                notificationConfig.shortDescription
            )

            startForegroundService()
            checkAndStartCollectingScreenLockData()
            checkAndStartCollectingPedometerData()
        }

        return START_STICKY
    }

    private suspend fun checkAndStartCollectingPedometerData() {
        if (config.sensorArray.contains(SahhaSensor.pedometer.ordinal)) {
            // TODO: Refactor this
            // Checks sensor is activated first
            runPedometerAsync(
                (applicationContext.getSystemService(SENSOR_SERVICE) as SensorManager),
                Sahha.di.movementDao
            )
        }
    }

    private fun checkAndStartCollectingScreenLockData() {
        if (config.sensorArray.contains(SahhaSensor.device.ordinal)) {
            screenLocksRegistered =
                Sahha.di.startCollectingPhoneScreenLockDataUseCase(
                    this@DataCollectionService.applicationContext,
                    screenLocksRegistered
                )
        }
    }

    private suspend fun startForegroundService() {
        withContext(Main) {
            startForeground(NOTIFICATION_DATA_COLLECTION, Sahha.di.backgroundRepo.notification)
        }
    }

    private suspend fun runPedometerAsync(sensorManager: SensorManager, movementDao: MovementDao) {
        // TODO: Get user's gender
        var steps = 0f
        var lastSteps = 0
        var distance: Double = 0.0
        var elapsedTimeInMillis = 0L
        var startOfSteps = 0L
        var startDateTime = ""
        var endDateTime = ""
        var createdAt = ""

        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        val sensorListener2 = object : SensorEventListener2 {
            override fun onSensorChanged(sensorEvent: SensorEvent?) {
                Sahha.di.ioScope.launch {
                    sensorEvent?.let { event ->
                        val distancePerStepMetres = AVG_STEP_DISTANCE

                        val totalSteps = event.values[0]

                        // Check if last steps null
                        val lastDetectedSteps = movementDao.getLastDetectedSteps() ?: null

                        fun setDataFromTotalSteps() {
                            // Add steps to both last and current detected steps
                            steps = event.values[0]
                            // Distance of these steps
                            distance = steps * distancePerStepMetres
                            // Set start time to be start of device boot
                            elapsedTimeInMillis =
                                Sahha.timeManager!!.convertNanosToMillis(event.timestamp)
                            startOfSteps = Sahha.timeManager!!.nowInEpoch() - elapsedTimeInMillis
                            startDateTime = Sahha.timeManager!!.epochMillisToISO(startOfSteps)
                            // end time to manual time stamp
                            endDateTime = Sahha.timeManager!!.nowInISO()
                            // created at to manual time stamp
                            createdAt = Sahha.timeManager!!.nowInISO()
                        }

                        fun setDataFromLastSteps() {
                            lastSteps = lastDetectedSteps!!.steps
                            // current steps minus last steps
                            steps = event.values[0] - lastSteps
                            // distance of steps
                            distance = steps * distancePerStepMetres
                            // start time is end of last steps end time
                            startDateTime = lastDetectedSteps.endDateTime
                            // end time to manual time stamp
                            endDateTime = Sahha.timeManager!!.nowInISO()
                            // created at to manual time stamp
                            createdAt = Sahha.timeManager!!.nowInISO()
                        }

                        when {
                            lastDetectedSteps == null -> {
                                setDataFromTotalSteps()
                            }
                            totalSteps < lastDetectedSteps.steps -> {
                                setDataFromTotalSteps()
                            }
                            else -> {
                                setDataFromLastSteps()
                            }
                        }

                        // Last steps must always be the total so we can get the correct differences
                        val lastStepsInt = event.values[0].toInt()
                        val stepsInt = steps.toInt()
                        val distanceInt = distance.toInt()

                        movementDao.saveDetectedSteps(
                            DetectedSteps(
                                stepsInt,
                                distanceInt,
                                startDateTime,
                                endDateTime,
                                createdAt
                            )
                        )
                        movementDao.saveLastDetectedSteps(
                            LastDetectedSteps(
                                lastStepsInt,
                                distanceInt,
                                startDateTime,
                                endDateTime,
                                createdAt
                            )
                        )
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            override fun onFlushCompleted(sensor: Sensor?) {}
        }

        sensor?.let { _sensor ->
            if (pedometerRegistered) return

            sensorManager.registerListener(
                sensorListener2,
                _sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            pedometerRegistered = true
        }
    }
}
