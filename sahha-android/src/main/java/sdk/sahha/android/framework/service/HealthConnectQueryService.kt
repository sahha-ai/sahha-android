package sdk.sahha.android.framework.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import sdk.sahha.android.common.Constants
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.common.Session
import sdk.sahha.android.source.Sahha
import java.time.ZonedDateTime

private const val tag = "HealthConnectQueryService"

internal class HealthConnectQueryService : Service() {
    private val scope by lazy {
        CoroutineScope(Dispatchers.Default + Job())
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        scope.launch {
            SahhaReconfigure(this@HealthConnectQueryService)
            startNotification()
            if (stopOnNoAuth()) return@launch

            if (!Session.healthConnectServiceLaunched)
                launch {
                    Session.healthConnectServiceLaunched = true
                    Sahha.di
                        .sahhaInteractionManager
                        .sensor
                        .queryWithMinimumDelay(
                            afterTimer = {
                                stopForeground(STOP_FOREGROUND_REMOVE)
                                stopSelf()
                            }
                        ) { error, successful ->
                            Session.healthConnectPostCallback?.invoke(error, successful)
                            Session.healthConnectPostCallback = null

                            error?.also { e ->
                                Sahha.di.sahhaErrorLogger.application(
                                    e, tag, "onStartCommand"
                                )
                            }
                        }
                }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Session.healthConnectServiceLaunched = false
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
            title = "Syncing health data...",
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