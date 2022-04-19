package sdk.sahha.android.domain.repository

import android.app.Notification
import android.content.Context

interface BackgroundRepo {
    var notification: Notification
    fun setSahhaNotification(_notification: Notification)
    fun startDataCollectionService(
        icon: Int?,
        title: String?,
        shortDescription: String?,
        callback: ((error: String?, success: String?) -> Unit)?
    )

    fun startActivityRecognitionReceiver()
    fun startPhoneScreenReceivers(serviceContext: Context, receiverRegistered: Boolean): Boolean
    fun startStepWorker(repeatIntervalMinutes: Long, workerTag: String)
    fun startSleepWorker(repeatIntervalMinutes: Long, workerTag: String)
    fun startPostWorkersAsync()
    fun stopWorkerByTag(workerTag: String)
    fun stopAllWorkers()
}