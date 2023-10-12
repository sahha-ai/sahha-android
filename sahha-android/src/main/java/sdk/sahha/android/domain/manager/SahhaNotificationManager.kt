package sdk.sahha.android.domain.manager

import android.app.Notification
import android.app.NotificationManager
import sdk.sahha.android.R

interface SahhaNotificationManager {
    var notification: Notification
    fun setSahhaNotification(_notification: Notification)
    fun startDataCollectionService(
        _icon: Int? = null,
        _title: String? = null,
        _shortDescription: String? = null,
        callback: ((error: String?, success: Boolean) -> Unit)? = null
    )

    fun setNewPersistent(
        icon: Int? = null, title: String? = null, shortDescription: String? = null,
    )

    fun notifyWithSettingsIntent(title: String?, shortDescription: String?)

    fun <T> setNewNotification(
        title: String,
        channelId: String,
        channelName: String,
        serviceClass: Class<T>,
        descriptionText: String = "",
        importance: Int = NotificationManager.IMPORTANCE_MIN,
        isOngoing: Boolean = false,
        icon: Int = R.drawable.ic_sahha_no_bg
    ): Notification

    fun startHealthConnectPostService()
}