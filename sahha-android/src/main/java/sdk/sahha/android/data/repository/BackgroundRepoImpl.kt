package sdk.sahha.android.data.repository

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.widget.Toast
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.location.ActivityRecognitionClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sdk.sahha.android.Sahha
import sdk.sahha.android.data.Constants
import sdk.sahha.android.data.Constants.ACTIVITY_RECOGNITION_UPDATE_INTERVAL_MILLIS
import sdk.sahha.android.domain.receiver.ActivityRecognitionReceiver
import sdk.sahha.android.domain.receiver.PhoneScreenOn
import sdk.sahha.android.domain.repository.BackgroundRepo
import sdk.sahha.android.domain.service.DataCollectionService
import sdk.sahha.android.domain.worker.StepWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class BackgroundRepoImpl @Inject constructor(
    private val context: Context,
    private val defaultScope: CoroutineScope
) : BackgroundRepo {
    override lateinit var notification: Notification

    private val tag by lazy { "BackgroundRepoImpl" }
    private val workManager by lazy { WorkManager.getInstance(context) }
    private val activityRecognitionIntent by lazy {
        Intent(
            context,
            ActivityRecognitionReceiver::class.java
        )
    }

    private var activityRecognitionReceiverRegistered = false

    private lateinit var activityRecognitionClient: ActivityRecognitionClient
    private lateinit var activityRecognitionPendingIntent: PendingIntent

    override fun setSahhaNotification(_notification: Notification) {
        notification = _notification
    }

    override fun startDataCollectionService(
        icon: Int?,
        title: String?,
        shortDescription: String?
    ) {
        Sahha.notifications.setNewPersistent(icon, title, shortDescription)
        context.startForegroundService(Intent(context, DataCollectionService::class.java))
    }

    override fun startActivityRecognitionReceiver() {
        if (activityRecognitionReceiverRegistered) return

        defaultScope.launch {
            setActivityRecognitionClient()
            setActivityRecognitionPendingIntent()
            requestActivityRecognitionUpdates()
        }
    }

    override fun startPhoneScreenReceiver() {
        context.registerReceiver(
            PhoneScreenOn(),
            IntentFilter().apply {
                addAction(Intent.ACTION_USER_PRESENT)
            }
        )
    }

    override fun startStepWorker(repeatIntervalMinutes: Long, workerTag: String) {
        val checkedIntervalMinutes = getCheckedIntervalMinutes(repeatIntervalMinutes)
        val workRequest: PeriodicWorkRequest =
            getStepWorkerWorkRequest(checkedIntervalMinutes, workerTag)
        startWorkManager(workRequest, workerTag)
    }

    override fun stopWorkerByTag(workerTag: String) {
        workManager.cancelAllWorkByTag(workerTag)
    }

    override fun stopAllWorkers() {
        workManager.cancelAllWork()
    }

    // Force default minimum value of 15 minutes
    private fun getCheckedIntervalMinutes(interval: Long): Long {
        return if (interval < 15) 15 else interval
    }

    private fun getStepWorkerWorkRequest(
        repeatIntervalMinutes: Long,
        workerTag: String
    ): PeriodicWorkRequest {
        return PeriodicWorkRequestBuilder<StepWorker>(repeatIntervalMinutes, TimeUnit.MINUTES)
            .addTag(workerTag)
            .build()
    }

    private fun startWorkManager(workRequest: PeriodicWorkRequest, workerTag: String) {
        workManager.enqueueUniquePeriodicWork(
            workerTag,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }


    private fun setActivityRecognitionClient() {
        activityRecognitionClient = ActivityRecognitionClient(context)
    }

    private fun setActivityRecognitionPendingIntent() {
        val isAndroid12AndAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        activityRecognitionPendingIntent =
            if (isAndroid12AndAbove) {
                getPendingIntentWithMutableFlag()
            } else {
                getPendingIntent()
            }
    }

    private fun requestActivityRecognitionUpdates() {
        activityRecognitionClient.requestActivityUpdates(
            ACTIVITY_RECOGNITION_UPDATE_INTERVAL_MILLIS,
            activityRecognitionPendingIntent
        ).addOnSuccessListener {
            activityRecognitionReceiverRegistered = true
        }.addOnFailureListener {
            displayErrorToast(it)
        }
    }

    private fun displayErrorToast(e: Exception) {
        Toast.makeText(
            context,
            "Activity recognition request failed: ${e.message}",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun getPendingIntentWithMutableFlag(): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            Constants.ACTIVITY_RECOGNITION_RECEIVER,
            activityRecognitionIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    private fun getPendingIntent(): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            Constants.ACTIVITY_RECOGNITION_RECEIVER,
            activityRecognitionIntent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
    }
}