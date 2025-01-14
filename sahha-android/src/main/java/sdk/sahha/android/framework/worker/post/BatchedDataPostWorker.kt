package sdk.sahha.android.framework.worker.post

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import sdk.sahha.android.common.Constants
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.source.Sahha
import kotlin.coroutines.resume

private const val tag = "BatchedDataPostWorker"

internal class BatchedDataPostWorker(
    private val context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {
    private val postSupervisorJob = SupervisorJob()
    private val postScope = CoroutineScope(Dispatchers.Default + postSupervisorJob)
    private val errorLogger = Sahha.di.sahhaErrorLogger
    override suspend fun doWork(): Result {
        SahhaReconfigure(context)
        return postDataLogs()
    }

    private suspend fun postDataLogs(): Result {
        // Guard: Return and do nothing if there is no auth data
        if (!Sahha.isAuthenticated) return Result.success()

        return suspendCancellableCoroutine { cont ->
            postScope.launch {
                val batchedData = try {
                    Sahha.di.batchedDataRepo.getBatchedData()
                } catch (e: Exception) {
                    errorLogger.application(
                        e.message ?: "Something went wrong reading batched data",
                        tag,
                        "postDataLogs"
                    )
                    if (cont.isActive) cont.resume(Result.retry())
                    emptyList()
                }

                withTimeout(Constants.POST_TIMEOUT_LIMIT_MILLIS) {
                    Sahha.sim.sensor.postBatchData(
                        batchedData
                    ) { _, successful ->
                        if (successful) {
                            if (cont.isActive) cont.resume(Result.success())
                        } else {
                            if (cont.isActive) cont.resume(Result.retry())
                        }
                    }
                }
            }
        }
    }
}