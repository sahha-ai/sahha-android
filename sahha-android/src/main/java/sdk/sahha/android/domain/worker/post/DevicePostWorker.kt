package sdk.sahha.android.domain.worker.post

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.launch
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.common.SahhaReconfigure

class DevicePostWorker(private val context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {
    override fun doWork(): Result {
        CoroutineScope(Default).launch {
            SahhaReconfigure(context.applicationContext)
            Sahha.di.postDeviceDataUseCase(null)
        }
        return Result.success()
    }
}