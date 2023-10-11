package sdk.sahha.android.data.service

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.data.Constants
import sdk.sahha.android.source.Sahha
import kotlin.coroutines.resume

private const val tag = "HealthConnectPostService"

class HealthConnectPostService : Service() {
    private val mainScope by lazy { CoroutineScope(Dispatchers.Main) }
    private val defaultScope by lazy { CoroutineScope(Dispatchers.Default) }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        println("HealthConnectPostService0001")
        defaultScope.launch {
            println("HealthConnectPostService0002")
            SahhaReconfigure(this@HealthConnectPostService)
            startNotification()
            if (stopOnNoAuth()) return@launch

            postWithMinimumDelay()
        }

        return START_NOT_STICKY
    }

    private suspend fun postWithMinimumDelay() {
        val query = defaultScope.launch {
            try {
                withTimeout(30000) {
                    awaitHealthConnectPost()
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(tag, "Task timed out after 30 seconds")
            }
        }

        val minimumTime = defaultScope.launch {
            delay(5000)
        }

        val minimumTimeOrQuery = listOf(query, minimumTime)
        minimumTimeOrQuery.joinAll()
        if (query.isActive) query.cancel()
        if (minimumTime.isActive) minimumTime.cancel()

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private suspend fun awaitHealthConnectPost() = suspendCancellableCoroutine { cont ->
        defaultScope.launch {
            Sahha.di.postHealthConnectDataUseCase { _, _ ->
                if (cont.isActive) cont.resume(Unit)
                this.cancel()
            }
        }
    }

    private fun stopOnNoAuth(): Boolean {
        if (!Sahha.isAuthenticated) {
            println("User has no auth, exiting HealthConnectPostService")
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        return !Sahha.isAuthenticated
    }

    private suspend fun startNotification() {
        println("HealthConnectPostService0004")
        val config = Sahha.di.sahhaConfigRepo.getNotificationConfig()
        val notification = Sahha.di.sahhaNotificationManager.setNewNotification(
            icon = config.icon,
            title = "Sending data for analysis...",
            channelId = Constants.HEALTH_CONNECT_NOTIFICATION_CHANNEL_ID,
            channelName = "Health Connect Sync",
            serviceClass = this::class.java,
            importance = NotificationManager.IMPORTANCE_DEFAULT
        )

        startForeground(
            Constants.NOTIFICATION_HEALTH_CONNECT,
            notification
        )
    }
}