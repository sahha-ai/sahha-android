package sdk.sahha.android.data.worker.post.silver_format

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.domain.use_case.post.silver_format.PostSilverDeviceDataUseCase
import sdk.sahha.android.source.Sahha

class SilverDevicePostWorker(private val context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {

    internal lateinit var useCase: PostSilverDeviceDataUseCase
    override suspend fun doWork(): Result {
        SahhaReconfigure(context)
        useCase = Sahha.di.postSilverDeviceDataUseCase
        return useCase()
    }
}