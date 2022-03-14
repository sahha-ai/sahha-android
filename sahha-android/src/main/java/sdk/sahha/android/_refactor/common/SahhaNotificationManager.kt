package sdk.sahha.android._refactor.common

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import sdk.sahha.android.R

internal object SahhaNotificationManager {
    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotification(
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationForWorker(
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun getNewNotification(
        _context: Context,
        _channelId: String,
        _channelName: String,
        _importanceLevel: Int,
        _contentTitle: String,
        _contentText: String,
        _isOngoing: Boolean
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
            .setSmallIcon(R.drawable.ic_baseline_security_update_good_24)
            .build()

        return notification
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationForWorkerWithIntent(
        _context: Context,
        _channelId: String,
        _channelName: String,
        _importanceLevel: Int,
        _contentTitle: String,
        _contentText: String,
        _isOngoing: Boolean,
        _notificationId: Int,
        _intent: Intent
    ) {
        // App settings
        val intent = Intent(_intent)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val pendingIntent = PendingIntent.getActivity(
            _context.applicationContext,
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
            .build()

        manager.notify(_notificationId, notification)
    }
}
