package sdk.sahha.android.framework.worker

import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import sdk.sahha.android.common.Constants
import sdk.sahha.android.framework.service.DataCollectionService

internal class HealthConnectQueryWorker(context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        val serviceIntent = Intent(applicationContext, DataCollectionService::class.java)
        serviceIntent.putExtra(Constants.INTENT_ACTION, Constants.IntentAction.QUERY_HEALTH_CONNECT)
        applicationContext.startService(serviceIntent)

        return Result.success()
    }
}