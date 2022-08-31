package sdk.sahha.android.domain.repository

import android.app.Notification
import android.content.Context
import sdk.sahha.android.data.local.dao.MovementDao
import sdk.sahha.android.source.SahhaSensor

interface BackgroundRepo {
    var notification: Notification
    fun setSahhaNotification(_notification: Notification)
    fun startDataCollectionService(
        _icon: Int?,
        _title: String?,
        _shortDescription: String?,
        callback: ((error: String?, success: Boolean) -> Unit)?
    )

    fun startActivityRecognitionReceiver(callback: ((error: String?, success: Boolean) -> Unit)? = null)
    fun startPhoneScreenReceivers(
        serviceContext: Context,
    )
    fun startTimeZoneChangedReceiver(context: Context)

    fun startSleepWorker(repeatIntervalMinutes: Long, workerTag: String)
    fun startPostWorkersAsync()
    fun stopWorkerByTag(workerTag: String)
    fun stopAllWorkers()
    suspend fun startStepDetectorAsync(
        context: Context,
        movementDao: MovementDao,
    )

    suspend fun startStepCounterAsync(
        context: Context,
        movementDao: MovementDao,
    )
    suspend fun getSensorData(
        sensor: SahhaSensor,
        callback: ((error: String?, successful: String?) -> Unit)
    )
}