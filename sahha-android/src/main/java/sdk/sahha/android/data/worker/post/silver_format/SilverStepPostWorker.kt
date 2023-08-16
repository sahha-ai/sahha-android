package sdk.sahha.android.data.worker.post.silver_format

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.domain.model.steps.StepSession
import sdk.sahha.android.domain.use_case.post.silver_format.PostSilverStepDataUseCase
import sdk.sahha.android.source.Sahha

class SilverStepPostWorker(private val context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {
    internal lateinit var useCase: PostSilverStepDataUseCase
    override suspend fun doWork(): Result {
        SahhaReconfigure(context)
        useCase = Sahha.di.postSilverStepDataUseCase
        return useCase()
    }
}