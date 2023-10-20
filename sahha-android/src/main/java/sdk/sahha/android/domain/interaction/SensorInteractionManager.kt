package sdk.sahha.android.domain.interaction

import android.content.Context
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaReceiversAndListeners
import sdk.sahha.android.di.IoScope
import sdk.sahha.android.domain.manager.PermissionManager
import sdk.sahha.android.domain.manager.SahhaNotificationManager
import sdk.sahha.android.domain.repository.HealthConnectRepo
import sdk.sahha.android.domain.repository.SensorRepo
import sdk.sahha.android.domain.use_case.GetSensorDataUseCase
import sdk.sahha.android.domain.use_case.background.StartCollectingPhoneScreenLockDataUseCase
import sdk.sahha.android.domain.use_case.background.StartCollectingSleepDataUseCase
import sdk.sahha.android.domain.use_case.background.StartCollectingStepCounterData
import sdk.sahha.android.domain.use_case.background.StartCollectingStepDetectorData
import sdk.sahha.android.domain.use_case.background.StartDataCollectionServiceUseCase
import sdk.sahha.android.domain.use_case.post.PostAllSensorDataUseCase
import sdk.sahha.android.domain.use_case.post.PostDeviceDataUseCase
import sdk.sahha.android.domain.use_case.post.PostHealthConnectDataUseCase
import sdk.sahha.android.domain.use_case.post.PostSleepDataUseCase
import sdk.sahha.android.domain.use_case.post.PostStepDataUseCase
import sdk.sahha.android.domain.use_case.post.StartHealthConnectBackgroundTasksUseCase
import sdk.sahha.android.domain.use_case.post.StartPostWorkersUseCase
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensor
import javax.inject.Inject
import kotlin.coroutines.resume

private const val tag = "SensorInteractionManager"

class SensorInteractionManager @Inject constructor(
    private val context: Context,
    @IoScope private val ioScope: CoroutineScope,
    private val repository: SensorRepo,
    private val healthConnectRepo: HealthConnectRepo,
    private val permissionManager: PermissionManager,
    private val notificationManager: SahhaNotificationManager,
    private val sensorManager: SensorManager,
    private val startPostWorkersUseCase: StartPostWorkersUseCase,
    private val startCollectingSleepDataUseCase: StartCollectingSleepDataUseCase,
    private val startDataCollectionServiceUseCase: StartDataCollectionServiceUseCase,
    private val postAllSensorDataUseCase: PostAllSensorDataUseCase,
    private val getSensorDataUseCase: GetSensorDataUseCase,
    private val startHealthConnectBackgroundTasksUseCase: StartHealthConnectBackgroundTasksUseCase,
    private val postHealthConnectDataUseCase: PostHealthConnectDataUseCase,
    internal val postSleepDataUseCase: PostSleepDataUseCase,
    internal val postDeviceDataUseCase: PostDeviceDataUseCase,
    internal val postStepDataUseCase: PostStepDataUseCase,
    internal val startCollectingStepCounterData: StartCollectingStepCounterData,
    internal val startCollectingStepDetectorData: StartCollectingStepDetectorData,
    internal val startCollectingPhoneScreenLockDataUseCase: StartCollectingPhoneScreenLockDataUseCase,
    internal val sahhaErrorLogger: SahhaErrorLogger
) {

    fun postSensorData(
        callback: ((error: String?, success: Boolean) -> Unit)
    ) {
        ioScope.launch {
            if (permissionManager.shouldUseHealthConnect()) {
                notificationManager.startHealthConnectPostService()
                callback.invoke(null, true)
                return@launch
            }

            postAllSensorDataUseCase(callback)
        }
    }

    internal fun unregisterExistingReceiversAndListeners(context: Context) {
        SahhaErrors.wrapMultipleFunctionTryCatch(
            tag, "Could not unregister listener", listOf(
                { context.unregisterReceiver(SahhaReceiversAndListeners.screenLocks) },
                { sensorManager.unregisterListener(SahhaReceiversAndListeners.stepDetector) },
                { sensorManager.unregisterListener(SahhaReceiversAndListeners.stepCounter) },
                { context.unregisterReceiver(SahhaReceiversAndListeners.timezoneDetector) }
            ))
    }

    internal fun getSensorData(
        sensor: SahhaSensor,
        callback: ((error: String?, success: String?) -> Unit)
    ) {
        ioScope.launch {
            getSensorDataUseCase(sensor, callback)
        }
    }

    internal fun checkAndStartPostWorkers() {
        if (!Sahha.config.postSensorDataManually) {
            startPostWorkersUseCase()
            startDataCollection()
        }
    }

    internal fun checkAndStartDevicePostWorker(
        callback: ((error: String?, success: Boolean) -> Unit)? = null
    ) {
        healthConnectRepo.startDevicePostWorker(callback)
    }

    internal fun startDataCollection(callback: ((error: String?, success: Boolean) -> Unit)? = null) {
        if (Sahha.config.sensorArray.contains(SahhaSensor.sleep.ordinal)) {
            startCollectingSleepDataUseCase()
        }

        // Pedometer/device checkers are in the service
        startDataCollectionServiceUseCase(callback = callback)
    }

    internal suspend fun postStepSessions(
        callback: (suspend (error: String?, success: Boolean) -> Unit)?
    ) {
        repository.postStepSessions(repository.getAllStepSessions(), callback)
    }

    internal suspend fun postWithMinimumDelay(callback: (error: String?, successful: Boolean) -> Unit) {
        var result: Pair<String?, Boolean> = Pair(SahhaErrors.failedToPostAllData, false)
        val postScope = CoroutineScope(Dispatchers.IO)
        println("postWithMinimumDelay0001")
        val query = postScope.launch {
            try {
                println("postWithMinimumDelay0002")
                withTimeout(300000) {
                    println("postWithMinimumDelay0003")
                    result = awaitHealthConnectPost()
                }
            } catch (e: TimeoutCancellationException) {
                result = Pair(e.message, false)
                Log.e(tag, "Task timed out after 30 seconds")
            }
        }

        val minimumTime = postScope.launch {
            println("postWithMinimumDelay0004")
            delay(5000)
        }

        println("postWithMinimumDelay0005")
        val minimumTimeOrQuery = listOf(query, minimumTime)
        minimumTimeOrQuery.joinAll()
        if (query.isActive) query.cancel()
        if (minimumTime.isActive) minimumTime.cancel()

        callback(result.first, result.second)
    }

    private suspend fun awaitHealthConnectPost() = suspendCancellableCoroutine { cont ->
        ioScope.launch {
            postHealthConnectDataUseCase { error, successful ->
                if (cont.isActive) cont.resume(Pair(error, successful))
                //this.cancel()
            }
        }
    }
}