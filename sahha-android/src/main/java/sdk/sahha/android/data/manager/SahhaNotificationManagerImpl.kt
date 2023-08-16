package sdk.sahha.android.data.manager

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.runBlocking
import sdk.sahha.android.R
import sdk.sahha.android.common.SahhaIntents
import sdk.sahha.android.data.Constants
import sdk.sahha.android.data.service.DataCollectionService
import sdk.sahha.android.domain.manager.SahhaNotificationManager
import sdk.sahha.android.source.Sahha

class SahhaNotificationManagerImpl(
    private val context: Context
) : SahhaNotificationManager {
    override lateinit var notification: Notification

    override fun setSahhaNotification(_notification: Notification) {
        notification = _notification
    }

    override fun startDataCollectionService(
        _icon: Int?,
        _title: String?,
        _shortDescription: String?,
        callback: ((error: String?, success: Boolean) -> Unit)?
    ) {
        val notificationConfig = runBlocking {
            Sahha.di.configurationDao.getNotificationConfig()
        }

        setNewPersistent(
            notificationConfig.icon,
            notificationConfig.title,
            notificationConfig.shortDescription
        )

        try {
            context.startForegroundService(
                Intent(context.applicationContext, DataCollectionService::class.java)
                    .setAction(Constants.ACTION_RESTART_SERVICE)
            )
        } catch (e: Exception) {
            callback?.invoke(e.message, false)
        }
    }

    override fun setNewPersistent(
        icon: Int?, title: String?, shortDescription: String?,
    ) {
        val notification = getNewNotification(
            context,
            "analytics",
            "Analytics",
            NotificationManager.IMPORTANCE_MIN,
            title ?: "Analytics are running",
            shortDescription ?: "Swipe for options to hide this notification.",
            true,
            icon ?: R.drawable.ic_sahha_no_bg
        )

        setSahhaNotification(notification)
    }

    override fun notifyWithSettingsIntent(title: String?, shortDescription: String?) {
        createNotificationWithIntent(
            context,
            "permissions",
            "Permissions",
            NotificationManager.IMPORTANCE_HIGH,
            title ?: "Permissions",
            shortDescription ?: "Please tap here to re-enable permissions.",
            true,
            Constants.NOTIFICATION_PERMISSION_SETTINGS,
            SahhaIntents.settings(context)
        )
    }

    private fun createNotification(
        _service: Service,
        _channelId: String,
        _channelName: String,
        _importanceLevel: Int,
        _contentTitle: String,
        _contentText: String,
        _isOngoing: Boolean,
        _notificationId: Int
    ) {
        val NOTIFICATION_CHANNEL_ID = "sahha.$_channelId"
        val channelName = _channelName
        val chan =
            NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                channelName,
                _importanceLevel
            )
        chan.lightColor = Color.parseColor("#FF333242")
        chan.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        val manager =
            (_service.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(chan)
        val notificationBuilder =
            NotificationCompat.Builder(_service, NOTIFICATION_CHANNEL_ID)
        val notification = notificationBuilder.setOngoing(_isOngoing)
            .setContentTitle(_contentTitle)
            .setContentText(_contentText)
            .setPriority(_importanceLevel)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        _service.startForeground(_notificationId, notification)
    }

    private fun createNotificationForWorker(
        _context: Context,
        _channelId: String,
        _channelName: String,
        _importanceLevel: Int,
        _contentTitle: String,
        _contentText: String,
        _isOngoing: Boolean,
        _notificationId: Int
    ) {
        val NOTIFICATION_CHANNEL_ID = "sahha.$_channelId"
        val channelName = _channelName
        val chan =
            NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                channelName,
                _importanceLevel
            )
        chan.lightColor = Color.parseColor("#FF333242")
        chan.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        val manager =
            (_context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(chan)
        val notificationBuilder =
            NotificationCompat.Builder(_context, NOTIFICATION_CHANNEL_ID)
        val notification = notificationBuilder.setOngoing(_isOngoing)
            .setContentTitle(_contentTitle)
            .setContentText(_contentText)
            .setPriority(_importanceLevel)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()

        manager.notify(_notificationId, notification)
    }

    private fun getNewNotification(
        _context: Context,
        _channelId: String,
        _channelName: String,
        _importanceLevel: Int,
        _contentTitle: String,
        _contentText: String,
        _isOngoing: Boolean,
        _icon: Int
    ): Notification {
        val NOTIFICATION_CHANNEL_ID = "sahha.$_channelId"
        val channelName = _channelName
        val chan =
            NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                channelName,
                _importanceLevel
            )
        chan.lightColor = Color.parseColor("#FF333242")
        chan.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        val manager =
            (_context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(chan)
        val notificationBuilder =
            NotificationCompat.Builder(_context, NOTIFICATION_CHANNEL_ID)
        val notification = notificationBuilder.setOngoing(_isOngoing)
            .setContentTitle(_contentTitle)
            .setContentText(_contentText)
            .setPriority(_importanceLevel)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setSmallIcon(_icon)
            .build()

        return notification
    }

    private fun createNotificationWithIntent(
        _context: Context,
        _channelId: String,
        _channelName: String,
        _importanceLevel: Int,
        _contentTitle: String,
        _contentText: String,
        _isOngoing: Boolean,
        _notificationId: Int,
        _intent: Intent,
        _icon: Int = R.drawable.ic_sahha_no_bg
    ) {
        // App settings
        val intent = Intent(_intent)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val pendingIntent = PendingIntent.getActivity(
            _context,
            _notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val NOTIFICATION_CHANNEL_ID = "sahha.$_channelId"
        val channelName = _channelName
        val chan =
            NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                channelName,
                _importanceLevel
            )
        chan.lightColor = Color.parseColor("#FF333242")
        chan.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        val manager =
            (_context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(chan)
        val notificationBuilder =
            NotificationCompat.Builder(_context, NOTIFICATION_CHANNEL_ID)
        val notification = notificationBuilder.setOngoing(_isOngoing)
            .setContentTitle(_contentTitle)
            .setContentText(_contentText)
            .setPriority(_importanceLevel)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSmallIcon(_icon)
            .build()

        manager.notify(_notificationId, notification)
    }
}