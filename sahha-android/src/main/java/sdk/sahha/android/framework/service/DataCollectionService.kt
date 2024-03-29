package sdk.sahha.android.framework.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sdk.sahha.android.common.Constants
import sdk.sahha.android.common.Constants.NOTIFICATION_DATA_COLLECTION
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaReceiversAndListeners
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.domain.model.config.SahhaConfiguration
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensor
import sdk.sahha.android.source.SahhaSensorStatus

@RequiresApi(Build.VERSION_CODES.O)
internal class DataCollectionService : Service() {
    private val tag by lazy { "DataCollectionService" }
    private lateinit var config: SahhaConfiguration

    private val scope by lazy { CoroutineScope(Dispatchers.Default) }
    private val sensors by lazy { Sahha.sim.sensor }

    private var killswitched = false

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        scope.launch {
            try {
                SahhaReconfigure(this@DataCollectionService.applicationContext)
                startForegroundNotification()

                config = Sahha.di.configurationDao.getConfig() ?: return@launch

                checkAndKillService(intent)
                startTimeZoneChangedReceiver()
                startDataCollectors(this@DataCollectionService)

                checkAndRestartService(intent)
            } catch (e: Exception) {
                stopService()
                Log.w(tag, e.message ?: "Something went wrong")
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        sensors.unregisterExistingReceiversAndListeners(this)

        if (killswitched) {
            println("Turning off main service")
            return
        }

        startForegroundService(
            Intent(
                this@DataCollectionService.applicationContext,
                DataCollectionService::class.java
            )
        )
    }

    private fun checkAndKillService(intent: Intent?) {
        intent?.also {
            if (it.action == Constants.ACTION_KILL_SERVICE) {
                killswitched = true
                stopService()
            }
        }
    }

    private fun checkAndRestartService(intent: Intent?) {
        intent?.also {
            if (it.action == Constants.ACTION_RESTART_SERVICE) {
                stopService()
            }
        }
    }

    private suspend fun startDataCollectors(context: Context) {
        checkAndStartCollectingScreenLockData()

        Sahha.di.permissionManager.getNativeSensorStatus(context) { status ->
            scope.launch {
                if (status == SahhaSensorStatus.enabled)
                    checkAndStartCollectingPedometerData()
            }
        }
    }

    private suspend fun startForegroundNotification() {
        withContext(Dispatchers.Default) {
            val notificationConfig = Sahha.di.configurationDao.getNotificationConfig()
            Sahha.di.sahhaNotificationManager.setNewPersistent(
                notificationConfig.icon,
                notificationConfig.title,
                notificationConfig.shortDescription
            )
            startForegroundService()
        }
    }

    private fun stopService() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startTimeZoneChangedReceiver() {
        Sahha.di.receiverManager.startTimeZoneChangedReceiver(this)
    }

    private suspend fun checkAndStartCollectingPedometerData() {
        if (config.sensorArray.contains(SahhaSensor.activity.ordinal)) {
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
        try {
            unregisterReceiver(SahhaReceiversAndListeners.screenLocks)
        } catch (e: Exception) {
            Log.w(tag, e.message ?: "Could not unregister receiver or listener")
        }

        if (config.sensorArray.contains(SahhaSensor.device.ordinal)) {
            Sahha.sim.sensor.startCollectingPhoneScreenLockDataUseCase(
                this@DataCollectionService.applicationContext,
            )
            return
        }
    }

    private fun startForegroundService() {
        startForeground(
            NOTIFICATION_DATA_COLLECTION,
            Sahha.di.sahhaNotificationManager.notification
        )
    }
}
