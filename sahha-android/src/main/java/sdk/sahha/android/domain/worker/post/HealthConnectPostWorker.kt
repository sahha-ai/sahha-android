package sdk.sahha.android.domain.worker.post

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.source.Sahha

private const val tag = "HealthConnectPostWorker"
class HealthConnectPostWorker(private val context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {
    override fun doWork(): Result {
        runBlocking {
            SahhaReconfigure(context)
            Sahha.di.postHealthConnectDataUseCase()
        }

        return Result.success()
    }
}