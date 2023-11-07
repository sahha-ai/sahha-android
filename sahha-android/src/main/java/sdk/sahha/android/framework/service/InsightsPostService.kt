package sdk.sahha.android.framework.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sdk.sahha.android.common.Constants
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.source.Sahha
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

private const val tag = "InsightsPostService"

class InsightsPostService : Service() {
    private val scope by lazy { CoroutineScope(Dispatchers.IO) }
    private val insights by lazy { Sahha.sim.insights }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("InsightsPostService0001")
        scope.launch {
            println("InsightsPostService0002")
            SahhaReconfigure(this@InsightsPostService)
            startNotification()
            if (stopOnNoAuth()) return@launch

            insights.postWithMinimumDelay()

            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        println("InsightsPostService0010")
        scheduleNextAlarm()
    }

    private fun scheduleNextAlarm() {
        println("InsightsPostService0011")
        Sahha.di.sahhaAlarmManager.setAlarm(
            Sahha.di.sahhaAlarmManager.getInsightsQueryPendingIntent(this.applicationContext),
            ZonedDateTime.of(
                LocalDate.now().plusDays(1),
                LocalTime.of(Constants.INSIGHTS_SLEEP_ALARM_HOUR, 5),
                ZonedDateTime.now().offset
            ).toInstant().toEpochMilli()
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
            title = "Synchronizing insights...",
            channelId = Constants.INSIGHTS_NOTIFICATION_CHANNEL_ID,
            channelName = "Insights",
            serviceClass = this::class.java,
        )

        startForeground(
            Constants.NOTIFICATION_INSIGHTS,
            notification
        )
    }
}