package sdk.sahha.android.framework.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.common.Constants
import sdk.sahha.android.source.Sahha
import java.time.ZonedDateTime

private const val tag = "HealthConnectPostService"

class HealthConnectPostService : Service() {
    private val defaultScope by lazy { CoroutineScope(Dispatchers.Default) }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        defaultScope.launch {
            SahhaReconfigure(this@HealthConnectPostService)
            startNotification()
            if (stopOnNoAuth()) return@launch

            Sahha.di
                .sahhaInteractionManager
                .sensor
                .postWithMinimumDelay { error, _ ->
                    error?.also { e ->
                        Sahha.di.sahhaErrorLogger.application(
                            e, tag, "onStartCommand"
                        )
                    }

                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Sahha.di.sahhaAlarmManager.setAlarm(
            Sahha.di.sahhaAlarmManager.getHealthConnectQueryPendingIntent(this),
            ZonedDateTime.now()
                .plusMinutes(Constants.DEFAULT_ALARM_INTERVAL_MINS)
                .toInstant()
                .toEpochMilli()
        )
    }

    private fun stopOnNoAuth(): Boolean {
        if (!Sahha.isAuthenticated) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        return !Sahha.isAuthenticated
    }

    private suspend fun startNotification() {
        val config = Sahha.di.sahhaConfigRepo.getNotificationConfig()
        val notification = Sahha.di.sahhaNotificationManager.setNewNotification(
            icon = config.icon,
            title = "Synchronizing health connect...",
            channelId = Constants.HEALTH_CONNECT_NOTIFICATION_CHANNEL_ID,
            channelName = "Health Connect Sync",
            serviceClass = this::class.java,
        )

        startForeground(
            Constants.NOTIFICATION_HEALTH_CONNECT,
            notification
        )
    }
}