package sdk.sahha.android.domain.manager

import android.content.Context

internal interface ReceiverManager {
    fun startActivityRecognitionReceiver(callback: ((error: String?, success: Boolean) -> Unit)? = null)
    fun startPhoneScreenReceivers(
        serviceContext: Context,
    )
    fun startTimeZoneChangedReceiver(context: Context)
}