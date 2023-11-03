package sdk.sahha.android.framework.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.data.Constants
import sdk.sahha.android.source.Sahha
import java.time.ZonedDateTime

private const val tag = "InsightsPostService"
class InsightsPostService : Service() {
    private val scope by lazy { CoroutineScope(Dispatchers.IO) }
    private val insights by lazy { Sahha.sim.insights }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        scope.launch {
            SahhaReconfigure(this@InsightsPostService)
            startNotification()
            if(stopOnNoAuth()) return@launch

            insights.postInsightsData { error, successful ->
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
        scheduleNextAlarm()
    }

    private fun scheduleNextAlarm() {
        Sahha.di.sahhaAlarmManager.setAlarm(
            Sahha.di.sahhaAlarmManager.pendingIntent,
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
            title = "Synchronizing insight data...",
            channelId = Constants.INSIGHTS_NOTIFICATION_CHANNEL_ID,
            channelName = "Insights",
            serviceClass = this::class.java,
        )

        startForeground(
            Constants.NOTIFICATION_HEALTH_CONNECT,
            notification
        )
    }
}