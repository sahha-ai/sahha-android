package sdk.sahha.android.domain.worker

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.SleepSegmentRequest
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.launch
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.data.Constants.SLEEP_DATA_REQUEST
import sdk.sahha.android.domain.receiver.SleepReceiver

@RequiresApi(Build.VERSION_CODES.Q)
class SleepCollectionWorker (private val context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {

    private val tag by lazy { "SleepCollectionWorker" }

    override fun doWork(): Result {
        CoroutineScope(Default).launch {
            SahhaReconfigure(context)

            val sleepIntent = Intent(context, SleepReceiver::class.java)
            val sleepPendingIntent = getSleepPendingIntent(sleepIntent)
            val task = getSleepSegmentTask(sleepPendingIntent)
            addLogListeners(task)
        }
        return Result.success()
    }

    private fun addLogListeners(task: Task<Void>) {
        task.addOnSuccessListener {
            Log.d(tag, "Request sleep segment updates successful")
        }
        task.addOnFailureListener {
            Log.w(tag, "Request sleep segment updates unsuccessful: ${it.message}")
        }
    }

    @SuppressLint("MissingPermission") //Code seems to believe it's missing permissions for this but it's not
    private fun getSleepSegmentTask(sleepPendingIntent: PendingIntent): Task<Void> {
        return ActivityRecognition.getClient(context).requestSleepSegmentUpdates(
            sleepPendingIntent,
            SleepSegmentRequest.getDefaultSleepSegmentRequest()
        )
    }

    private fun getSleepPendingIntent(sleepIntent: Intent): PendingIntent {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return PendingIntent.getBroadcast(
                context,
                SLEEP_DATA_REQUEST,
                sleepIntent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        }

        return PendingIntent.getBroadcast(
            context,
            SLEEP_DATA_REQUEST,
            sleepIntent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
    }
}
