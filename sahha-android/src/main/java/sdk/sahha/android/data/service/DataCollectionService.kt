package sdk.sahha.android.data.service

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import sdk.sahha.android.common.SahhaErrors
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

    private val ioScope by lazy { CoroutineScope(Dispatchers.IO) }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        if(ioScope.isActive) ioScope.cancel()
        unregisterExistingReceiversAndListeners()
        startForegroundService(
            Intent(this@DataCollectionService.applicationContext, DataCollectionService::class.java)
        )
    }

    private fun unregisterExistingReceiversAndListeners() {
        SahhaErrors.wrapMultipleFunctionTryCatch(tag, "Could not unregister listener", listOf(
            { unregisterReceiver(SahhaReceiversAndListeners.screenLocks) },
            { Sahha.di.sensorManager.unregisterListener(SahhaReceiversAndListeners.stepDetector) },
            { Sahha.di.sensorManager.unregisterListener(SahhaReceiversAndListeners.stepCounter) },
            { unregisterReceiver(SahhaReceiversAndListeners.timezoneDetector) }
        ))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            ioScope.launch {
                SahhaReconfigure(this@DataCollectionService.applicationContext)

                val notificationConfig = Sahha.di.configurationDao.getNotificationConfig()
                Sahha.di.notificationManager.setNewPersistent(
                    notificationConfig.icon,
                    notificationConfig.title,
                    notificationConfig.shortDescription
                )

                startForegroundService()
                config = Sahha.di.configurationDao.getConfig() ?: return@launch
                checkAndStartCollectingScreenLockData()
                checkAndStartCollectingPedometerData()
                startTimeZoneChangedReceiver()

                intent?.also {
                    if (it.action == Constants.ACTION_RESTART_SERVICE) {
                        stopService()
                        return@launch
                    }
                }
            }
        } catch (e: Exception) {
            stopService()
            Log.w(tag, e.message, e)
        }

        return START_STICKY
    }

    private fun stopService() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startTimeZoneChangedReceiver() {
        Sahha.di.receiverManager.startTimeZoneChangedReceiver(this)
    }

    private suspend fun checkAndStartCollectingPedometerData() {
        if (config.sensorArray.contains(SahhaSensor.pedometer.ordinal)) {
            Sahha.sim.sensor.startCollectingStepDetectorData(
                this,
                Sahha.di.movementDao,
            )
            return
        }

        SahhaErrors.wrapMultipleFunctionTryCatch(tag, "Could not unregister listener", listOf(
            { Sahha.di.sensorManager.unregisterListener(SahhaReceiversAndListeners.stepCounter) },
            { Sahha.di.sensorManager.unregisterListener(SahhaReceiversAndListeners.stepDetector) }
        ))
    }

    private fun checkAndStartCollectingScreenLockData() {
        if (config.sensorArray.contains(SahhaSensor.device.ordinal)) {
            Sahha.sim.sensor.startCollectingPhoneScreenLockDataUseCase(
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

    private fun startForegroundService() {
        startForeground(NOTIFICATION_DATA_COLLECTION, Sahha.di.notificationManager.notification)
    }
}
