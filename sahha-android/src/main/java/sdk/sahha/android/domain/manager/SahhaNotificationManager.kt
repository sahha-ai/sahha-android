package sdk.sahha.android.domain.manager

import android.app.Notification

interface SahhaNotificationManager {
    var notification: Notification
    fun setSahhaNotification(_notification: Notification)
    fun startDataCollectionService(
        _icon: Int?,
        _title: String?,
        _shortDescription: String?,
        callback: ((error: String?, success: Boolean) -> Unit)?
    )

    fun setNewPersistent(
        icon: Int? = null, title: String? = null, shortDescription: String? = null,
    )

    fun notifyWithSettingsIntent(title: String?, shortDescription: String?)
}