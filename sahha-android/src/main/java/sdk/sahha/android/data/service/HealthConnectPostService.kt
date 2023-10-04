package sdk.sahha.android.data.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.data.Constants
import sdk.sahha.android.source.Sahha

class HealthConnectPostService : Service() {
    val ioScope = CoroutineScope(Dispatchers.IO)
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        ioScope.launch {
            SahhaReconfigure(this@HealthConnectPostService)
            startNotification(intent)
            if (stopOnNoAuth()) return@launch

            Sahha.di.postHealthConnectDataUseCase { error, successful ->
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    private fun stopOnNoAuth(): Boolean {
        if (!Sahha.isAuthenticated) {
            println("User has no auth, exiting HealthConnectPostService")
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        return !Sahha.isAuthenticated
    }

    private fun startNotification(intent: Intent) {
        val notification = Sahha.di.sahhaNotificationManager.setNewNotification(
            title = intent.getStringExtra("title") ?: "Sending data for analysis...",
            channelId = Constants.HEALTH_CONNECT_NOTIFICATION_CHANNEL_ID,
            "Health Connect Sync",
            HealthConnectPostService::class.java
        )

        startForeground(
            Constants.NOTIFICATION_HEALTH_CONNECT,
            notification
        )
    }
}