package sdk.sahha.android.framework.worker

import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import sdk.sahha.android.common.Constants
import sdk.sahha.android.framework.service.DataCollectionService

@Deprecated("No longer used", level = DeprecationLevel.WARNING)

internal class BackgroundTaskRestarterWorker(context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        val serviceIntent = Intent(applicationContext, DataCollectionService::class.java)
        serviceIntent.putExtra(Constants.INTENT_ACTION, Constants.IntentAction.RESTART_BACKGROUND_TASKS)
        applicationContext.startService(serviceIntent)

        return Result.success()
    }

}