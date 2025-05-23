package sdk.sahha.android.framework.manager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sdk.sahha.android.BuildConfig
import sdk.sahha.android.R
import sdk.sahha.android.common.Constants
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaIntents
import sdk.sahha.android.di.DefaultScope
import sdk.sahha.android.domain.manager.SahhaNotificationManager
import sdk.sahha.android.domain.repository.SahhaConfigRepo
import sdk.sahha.android.framework.service.DataCollectionService
import sdk.sahha.android.source.Sahha

private val tag = "SahhaNotificationManagerImpl"

internal class SahhaNotificationManagerImpl(
    private val context: Context,
    private val sahhaErrorLogger: SahhaErrorLogger,
    private val notificationManager: NotificationManager,
    private val configRepo: SahhaConfigRepo,
    @DefaultScope private val defaultScope: CoroutineScope
) : SahhaNotificationManager {
    override lateinit var notification: Notification

    override fun setSahhaNotification(_notification: Notification) {
        notification = _notification
    }

    override fun <T> startForegroundService(
        serviceClass: Class<T>,
    ) {
        ContextCompat.startForegroundService(
            context,
            Intent(
                context.applicationContext,
                serviceClass
            )
        )
    }

    override fun <T> startForegroundService(
        context: Context,
        serviceClass: Class<T>,
        intent: Intent
    ) {
        ContextCompat.startForegroundService(
            context,
            intent
        )
    }

    override fun startDataCollectionService(
        _icon: Int?,
        _title: String?,
        _shortDescription: String?,
        callback: ((error: String?, success: Boolean) -> Unit)?
    ) {
        defaultScope.launch {
            val config = configRepo.getConfig()
            if (config.sensorArray.isEmpty()) return@launch

            val notificationConfig = Sahha.di.configurationDao.getNotificationConfig()

            setNewPersistent(
                notificationConfig.icon,
                notificationConfig.title,
                notificationConfig.shortDescription
            )

            try {
                withContext(Dispatchers.Main) {
                    ContextCompat.startForegroundService(
                        context,
                        Intent(context.applicationContext, DataCollectionService::class.java)
                    )
                }
                callback?.invoke(null, true)
            } catch (e: Exception) {
                callback?.also { it(e.message, false) }

                sahhaErrorLogger.application(
                    e.message ?: SahhaErrors.somethingWentWrong,
                    tag,
                    "startDataCollectionService",
                    "icon: $_icon, title: $_title, shortDescription: $_shortDescription"
                )
            }
        }
    }

    override fun setNewPersistent(
        icon: Int?, title: String?, shortDescription: String?,
    ) {
        val notification = getNewNotification(
            context,
            "insights",
            "Health Insights",
            NotificationManager.IMPORTANCE_MIN,
            title ?: Constants.NOTIFICATION_TITLE_DEFAULT,
            shortDescription ?: Constants.NOTIFICATION_DESC_DEFAULT,
            true,
            icon ?: R.drawable.ic_sahha_no_bg
        )

        setSahhaNotification(notification)
    }

    override fun getNewPersistent(
        icon: Int?, title: String?, shortDescription: String?,
    ): Notification {
        return getNewNotification(
            context,
            "insights",
            "Health Insights",
            NotificationManager.IMPORTANCE_MIN,
            title ?: Constants.NOTIFICATION_TITLE_DEFAULT,
            shortDescription ?: Constants.NOTIFICATION_DESC_DEFAULT,
            true,
            icon ?: R.drawable.ic_sahha_no_bg
        )
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

    override fun <T> setNewNotification(
        title: String,
        channelId: String,
        channelName: String,
        serviceClass: Class<T>,
        descriptionText: String,
        importance: Int,
        isOngoing: Boolean,
        icon: Int
    ): Notification {
        return getNewNotification(
            context,
            channelId,
            channelName,
            importance,
            title,
            descriptionText,
            isOngoing,
            icon
        )
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
        notificationManager.createNotificationChannel(chan)
        val notificationBuilder =
            NotificationCompat.Builder(_context, NOTIFICATION_CHANNEL_ID)
        val notification = notificationBuilder.setOngoing(_isOngoing)
            .setContentTitle(_contentTitle)
            .setContentText(_contentText)
            .setPriority(_importanceLevel)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setSmallIcon(_icon)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            notification
                .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
                .build()
        else notification.build()
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
        notificationManager.createNotificationChannel(chan)
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

        notificationManager.notify(_notificationId, notification)
    }
}