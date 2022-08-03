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
    private lateinit var config: SahhaConfiguration

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        Sahha.di.mainScope.launch {
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
            try {
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
                        stopService()
                        return@also
                    }
                }
            } catch (e: Exception) {
                stopService()
                Log.w(tag, e.message, e)
            }
        }

        return START_STICKY
    }

    private fun stopService() {
        stopForeground(true)
        stopSelf()
    }

    private suspend fun checkAndStartCollectingPedometerData() {
        if (config.sensorArray.contains(SahhaSensor.pedometer.ordinal)) {
            Sahha.di.startCollectingStepCounterData(
                this,
                Sahha.di.movementDao,
            )
            Sahha.di.startCollectingStepDetectorData(
                this,
                Sahha.di.movementDao,
            )
            return
        }

        try {
            Sahha.di.sensorManager.unregisterListener(SahhaReceiversAndListeners.stepCounter)
            Sahha.di.sensorManager.unregisterListener(SahhaReceiversAndListeners.stepDetector)
        } catch (e: Exception) {
            Log.w(tag, e.message ?: "Could not unregister receiver or listener", e)
        }
    }

    private fun checkAndStartCollectingScreenLockData() {
        if (config.sensorArray.contains(SahhaSensor.device.ordinal)) {
            Sahha.di.startCollectingPhoneScreenLockDataUseCase(
                this@DataCollectionService.applicationContext,
            )
            return
        }

        try {
            unregisterReceiver(SahhaReceiversAndListeners.screenLocks)
        } catch (e: Exception) {
            Log.w(tag, e.message ?: "Could not unregister receiver or listener", e)
        }
    }

    private suspend fun startForegroundService() {
        withContext(Main) {
            startForeground(NOTIFICATION_DATA_COLLECTION, Sahha.di.backgroundRepo.notification)
        }
    }
}
