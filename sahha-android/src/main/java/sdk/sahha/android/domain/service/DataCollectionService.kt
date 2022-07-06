package sdk.sahha.android.domain.service

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sdk.sahha.android.common.SahhaReceiversAndListeners
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.data.Constants
import sdk.sahha.android.data.Constants.NOTIFICATION_DATA_COLLECTION
import sdk.sahha.android.domain.model.config.SahhaConfiguration
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensor

@RequiresApi(Build.VERSION_CODES.O)
class DataCollectionService : Service() {
    private val tag by lazy { "DataCollectionService" }
    private var stepCounterRegistered = false
    private var stepDetectorRegistered = false
    private lateinit var config: SahhaConfiguration

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        Sahha.di.defaultScope.launch {
            unregisterExistingReceiversAndListeners()
        }

        startForegroundService(
            Intent(this@DataCollectionService, DataCollectionService::class.java)
        )
    }

    private fun unregisterExistingReceiversAndListeners() {
        try {
            unregisterReceiver(SahhaReceiversAndListeners.screenLocks)
            Sahha.di.sensorManager.unregisterListener(SahhaReceiversAndListeners.stepDetector)
            Sahha.di.sensorManager.unregisterListener(SahhaReceiversAndListeners.stepCounter)
        } catch (e: Exception) {
            Log.w(tag, e.message ?: "Could not unregister receiver or listener", e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        CoroutineScope(Main).launch {
            SahhaReconfigure(this@DataCollectionService.applicationContext)

            val notificationConfig = Sahha.di.configurationDao.getNotificationConfig()
            Sahha.di.notifications.setNewPersistent(
                notificationConfig.icon,
                notificationConfig.title,
                notificationConfig.shortDescription
            )

            startForegroundService()
            config = Sahha.di.configurationDao.getConfig() ?: return@launch
            checkAndStartCollectingScreenLockData()
            checkAndStartCollectingPedometerData()

            intent?.also {
                if (it.action == Constants.ACTION_RESTART_SERVICE) {
                    stopForeground(true)
                    stopSelf()
                    return@also
                }
            }
        }

        return START_STICKY
    }

    private suspend fun checkAndStartCollectingPedometerData() {
        if (config.sensorArray.contains(SahhaSensor.pedometer.ordinal)) {
            stepCounterRegistered =
                Sahha.di.startCollectingStepCounterData(
                    this,
                    Sahha.di.movementDao,
                    stepCounterRegistered
                )
            stepDetectorRegistered = Sahha.di.startCollectingStepDetectorData(
                this,
                Sahha.di.movementDao,
                stepDetectorRegistered
            )
        }
    }

    private fun checkAndStartCollectingScreenLockData() {
        if (config.sensorArray.contains(SahhaSensor.device.ordinal)) {
            Sahha.di.startCollectingPhoneScreenLockDataUseCase(
                this@DataCollectionService.applicationContext,
            )
        }
    }

    private suspend fun startForegroundService() {
        withContext(Main) {
            startForeground(NOTIFICATION_DATA_COLLECTION, Sahha.di.backgroundRepo.notification)
        }
    }
}
