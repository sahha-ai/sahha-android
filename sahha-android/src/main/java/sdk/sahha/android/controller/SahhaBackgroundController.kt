package sdk.sahha.android.controller

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.location.ActivityRecognitionClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.launch
import sdk.sahha.android._refactor.domain.receiver.ActivityRecognitionReceiver
import sdk.sahha.android._refactor.domain.receiver.PhoneScreenOn
import sdk.sahha.android._refactor.domain.service.DataCollectionService
import sdk.sahha.android._refactor.domain.worker.StepWorker
import sdk.sahha.android.data.Constants.ACTIVITY_RECOGNITION_RECEIVER
import sdk.sahha.android.data.Constants.ACTIVITY_RECOGNITION_UPDATE_INTERVAL_MILLIS
import java.util.concurrent.TimeUnit

class SahhaBackgroundController(private val context: Context) {
    private val tag by lazy { "BackgroundController" }
    private val defaultScope by lazy { CoroutineScope(Default) }
    private val workManager by lazy { WorkManager.getInstance(context) }

    private var activityRecognitionReceiverRegistered = false

    @RequiresApi(Build.VERSION_CODES.O)
    fun startDataCollectionService() {
        Log.w(tag, "startDataCollectionService")
        context.startForegroundService(Intent(context, DataCollectionService::class.java))
        Log.w(tag, "startDataCollectionService end")
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun startActivityRecognitionReceiver() {
        Log.w(tag, "startActivityRecognitionReceiver")
        if (activityRecognitionReceiverRegistered) return

        defaultScope.launch {
            Log.w(tag, "defaultScope")
            val client = ActivityRecognitionClient(context)
            val intent = Intent(context, ActivityRecognitionReceiver::class.java)
            val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getBroadcast(
                    context,
                    ACTIVITY_RECOGNITION_RECEIVER,
                    intent,
                    PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE
                )
            } else {
                PendingIntent.getBroadcast(
                    context,
                    ACTIVITY_RECOGNITION_RECEIVER,
                    intent,
                    PendingIntent.FLAG_CANCEL_CURRENT
                )
            }

            client.requestActivityUpdates(
                ACTIVITY_RECOGNITION_UPDATE_INTERVAL_MILLIS,
                pendingIntent
            )
                .addOnSuccessListener {
                    Log.w(tag, "requestActivityUpdates success!")
                    activityRecognitionReceiverRegistered = true
                }.addOnFailureListener { e ->
                    Log.e(tag, e.message, e)
                }
        }
    }

    fun startPhoneScreenReceiver() {
        Log.w(tag, "Start phone screen receiver")
        context.registerReceiver(
            PhoneScreenOn(),
            IntentFilter().apply { addAction(Intent.ACTION_USER_PRESENT) }
        )
    }

    fun startStepsWorker(repeatIntervalMinutes: Long, tag: String) {
        val workRequest: PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<StepWorker>(repeatIntervalMinutes, TimeUnit.MINUTES)
                .addTag(tag)
                .build()

        workManager.enqueueUniquePeriodicWork(
            tag,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun stopWorkerByTag(tag: String) {
        workManager.cancelAllWorkByTag(tag)
    }

    fun stopAllWorkers() {
        workManager.cancelAllWork()
    }
}
