package sdk.sahha.android.data.worker.post

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.launch
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.domain.model.steps.StepData
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensor
import sdk.sahha.android.source.SahhaSensorStatus
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val tag = "StepPostWorker"

class StepPostWorker(private val context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        SahhaReconfigure(context)
        return postStepData()
    }

    internal suspend fun performChunking(data: List<StepData>): Boolean =
        suspendCoroutine<Boolean> { outerCont ->
            Log.d(tag, "perform chunking")
            Sahha.di.defaultScope.launch {
                val chunkResult = Sahha.di.postChunkManager.sendDataInChunks(
                    data
                ) { chunk ->
                    suspendCoroutine<Boolean> { cont ->
                        launch {
                            Log.d(tag, "postStepData count: ${Sahha.di.movementDao.getAllStepData()}")
                            Sahha.sim.sensor.postStepDataUseCase(chunk) { _, success ->
                                Log.d(tag, "postStepData success: $success")
                                if (success) {
                                    launch {
                                        Sahha.di.movementDao.clearStepData(chunk)
                                        Log.d(tag, "postStepData count: ${Sahha.di.movementDao.getAllStepData()}")
                                        cont.resume(true)
                                    }
                                } else {
                                    cont.resume(false)
                                }
                            }
                        }
                    }
                }
                outerCont.resume(chunkResult)
            }
        }

    internal suspend fun postStepData(lockTester: (() -> Unit)? = null): Result {
        val mutex = Sahha.di.sensorMutexMap[SahhaSensor.pedometer] ?: return Result.success()

        if (mutex.tryLock()) {
            lockTester?.invoke()
            try {
                Sahha.getSensorStatus(context) { _, status ->
                    Sahha.di.ioScope.launch {
                        if (status == SahhaSensorStatus.enabled) {
                            val data = Sahha.di.movementDao.getAllStepData()
                            performChunking(data)
                        }
                    }
                }
            } finally {
                mutex.unlock()
            }
        }
        return Result.success()
    }
}

