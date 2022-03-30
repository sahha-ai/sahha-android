package sdk.sahha.android.domain.repository

import android.app.Notification

interface BackgroundRepo {
    var notification: Notification
    fun setSahhaNotification(_notification: Notification)
    fun startDataCollectionService(
        icon: Int?,
        title: String?,
        shortDescription: String?
    )

    fun startActivityRecognitionReceiver()
    fun startPhoneScreenReceiver()
    fun startStepWorker(repeatIntervalMinutes: Long, workerTag: String)
    fun startSleepWorker(repeatIntervalMinutes: Long, workerTag: String)
    fun startPostWorkers()
    fun stopWorkerByTag(workerTag: String)
    fun stopAllWorkers()
}