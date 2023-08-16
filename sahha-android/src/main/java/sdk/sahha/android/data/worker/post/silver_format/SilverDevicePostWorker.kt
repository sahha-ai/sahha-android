package sdk.sahha.android.data.worker.post.silver_format

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.source.Sahha

class SilverDevicePostWorker(private val context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        SahhaReconfigure(context)
        return Sahha.di.postSilverDeviceDataUseCase()
    }
}