package sdk.sahha.android.framework.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sdk.sahha.android.R
import sdk.sahha.android.common.Constants
import sdk.sahha.android.common.Constants.NOTIFICATION_DATA_COLLECTION
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaReceiversAndListeners
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.common.Session
import sdk.sahha.android.domain.model.config.SahhaConfiguration
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensor
import sdk.sahha.android.source.SahhaSensorStatus

internal class DataCollectionService : Service() {
    private val tag by lazy { "DataCollectionService" }
    private lateinit var config: SahhaConfiguration

    private val scope by lazy { CoroutineScope(Dispatchers.Default + SupervisorJob()) }
    private val sensors by lazy { Sahha.sim.sensor }

    private var killswitched = false

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_DATA_COLLECTION, createBasicNotification())

        scope.launch {
            try {
                SahhaReconfigure(this@DataCollectionService.applicationContext)
                startForegroundNotification()

                config = Sahha.di.sahhaConfigRepo.getConfig() ?: return@launch

                startTimeZoneChangedReceiver()
                startDataCollectors(this@DataCollectionService)

                Session.handlerThread = HandlerThread("DataCollectionServiceHandlerThread")
                Session.handlerThread.start()
                Session.serviceHandler = Handler(Session.handlerThread.looper)
                Session.serviceHandler.post(Sahha.di.dataCollectionPeriodicTask)
            } catch (e: Exception) {
                stopService()
                Log.w(tag, e.message ?: "Something went wrong")
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        checkAndKillService(intent)
        checkAndRestartService(intent)
        scope.launch {
            SahhaReconfigure(this@DataCollectionService)
            config = Sahha.di.sahhaConfigRepo.getConfig() ?: return@launch
            checkAndStartCollectingScreenLockData()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        sensors.unregisterExistingReceiversAndListeners(this)

        try {
            Session.handlerThread.quitSafely()
        } catch (e: Exception) {
            Log.d(tag, e.message ?: "Handler thread is not yet initialized")
        }
        Session.handlerRunning = false

        if (killswitched) {
            println("Turning off main service")
            return
        }
    }

    private fun createBasicNotification(): Notification {
        val channelId = "ForegroundServiceChannel"
        val channel = NotificationChannel(
            channelId,
            "Foreground Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Starting Service")
            .setContentText("Preparing to start...")
            .setSmallIcon(R.drawable.ic_sahha_no_bg)
            .build()
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
        if (config.sensorArray.contains(SahhaSensor.step_count.ordinal)) {
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

        if (config.sensorArray.contains(SahhaSensor.device_lock.ordinal)) {
            Sahha.sim.sensor.startCollectingPhoneScreenLockDataUseCase(
                this@DataCollectionService.applicationContext,
            )
            return
        }
    }

    private fun startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_DATA_COLLECTION,
                Sahha.di.sahhaNotificationManager.notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_DATA_COLLECTION,
                Sahha.di.sahhaNotificationManager.notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else startForeground(
            NOTIFICATION_DATA_COLLECTION,
            Sahha.di.sahhaNotificationManager.notification
        )
    }
}
