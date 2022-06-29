package sdk.sahha.android.domain.service

import android.app.Service
import android.content.Context
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
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.data.Constants.NOTIFICATION_DATA_COLLECTION
import sdk.sahha.android.data.local.dao.MovementDao
import sdk.sahha.android.domain.model.config.SahhaConfiguration
import sdk.sahha.android.domain.model.steps.StepData
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensor

@RequiresApi(Build.VERSION_CODES.O)
class DataCollectionService : Service() {
    private val tag by lazy { "DataCollectionService" }
    private var stepCounterRegistered = false
    private var stepDetectorRegistered = false
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
            stepCounterRegistered = Sahha.di.startCollectingStepCounterData(Sahha.di.movementDao, stepCounterRegistered)
            stepDetectorRegistered = Sahha.di.startCollectingStepDetectorData(Sahha.di.movementDao, stepDetectorRegistered)
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
}
