package sdk.sahha.android.domain.worker.post

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaConverterUtility
import sdk.sahha.android.source.SahhaSensor
import sdk.sahha.android.source.SahhaSensorStatus

class StepPostWorker(private val context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {

    override fun doWork(): Result {
        CoroutineScope(IO).launch {
            SahhaReconfigure(context)
            Sahha.getSensorStatus(
                context,
            ) { _, status ->
                if (status == SahhaSensorStatus.enabled) {
                    launch {
                        Sahha.di.postStepDataUseCase(
                            SahhaConverterUtility.stepDataToStepDto(
                                Sahha.di.movementDao.getAllStepData()
                            ),
                            null
                        )
                    }
                }
            }
        }

        return Result.success()
    }
}
