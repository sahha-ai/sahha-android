package sdk.sahha.android.framework.worker.post

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.common.Constants
import sdk.sahha.android.source.Sahha
import kotlin.coroutines.resume

private const val tag = "DevicePostWorker"

internal class DevicePostWorker(private val context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {
    private val scope = CoroutineScope(Dispatchers.IO)
    override suspend fun doWork(): Result {
        SahhaReconfigure(context)
        return postDeviceData()
    }

    internal suspend fun postDeviceData(lockTester: (() -> Unit)? = null): Result {
        // Guard: Return and do nothing if there is no auth data
        if (!Sahha.isAuthenticated) return Result.success()

        return if (Sahha.di.mutex.tryLock()) {
            lockTester?.invoke()
            try {
                suspendCancellableCoroutine { cont ->
                    scope.launch {
                        withTimeout(Constants.POST_TIMEOUT_LIMIT_MILLIS) {
                            Sahha.sim.sensor.postDeviceDataUseCase(Sahha.di.deviceUsageRepo.getUsages()) { error, success ->
                                if (cont.isActive) {
                                    cont.resume(Result.success())
                                }
                            }
                        }
                    }
                }
            } finally {
                try {
                    Sahha.di.mutex.unlock()
                } catch (e: Exception) {
                    Log.w(tag, e.message ?: "Failed to unlock mutex")
                    Result.retry()
                }
            }
        } else Result.retry()
    }
}