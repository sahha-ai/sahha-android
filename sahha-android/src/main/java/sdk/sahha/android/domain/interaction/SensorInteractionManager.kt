package sdk.sahha.android.domain.interaction

import android.content.Context
import android.content.Intent
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import sdk.sahha.android.common.Constants
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaReceiversAndListeners
import sdk.sahha.android.common.Session
import sdk.sahha.android.di.IoScope
import sdk.sahha.android.domain.manager.PermissionManager
import sdk.sahha.android.domain.manager.SahhaAlarmManager
import sdk.sahha.android.domain.manager.SahhaNotificationManager
import sdk.sahha.android.domain.repository.HealthConnectRepo
import sdk.sahha.android.domain.repository.SahhaConfigRepo
import sdk.sahha.android.domain.repository.SensorRepo
import sdk.sahha.android.domain.use_case.GetSensorDataUseCase
import sdk.sahha.android.domain.use_case.background.BatchDataLogs
import sdk.sahha.android.domain.use_case.background.StartCollectingPhoneScreenLockDataUseCase
import sdk.sahha.android.domain.use_case.background.StartCollectingSleepDataUseCase
import sdk.sahha.android.domain.use_case.background.StartCollectingStepCounterData
import sdk.sahha.android.domain.use_case.background.StartCollectingStepDetectorData
import sdk.sahha.android.domain.use_case.background.StartDataCollectionServiceUseCase
import sdk.sahha.android.domain.use_case.post.PostAllSensorDataUseCase
import sdk.sahha.android.domain.use_case.post.PostBatchData
import sdk.sahha.android.domain.use_case.post.PostDeviceDataUseCase
import sdk.sahha.android.domain.use_case.post.PostHealthConnectDataUseCase
import sdk.sahha.android.domain.use_case.post.PostSleepDataUseCase
import sdk.sahha.android.domain.use_case.post.PostStepDataUseCase
import sdk.sahha.android.domain.use_case.post.StartHealthConnectBackgroundTasksUseCase
import sdk.sahha.android.domain.use_case.post.StartPostWorkersUseCase
import sdk.sahha.android.framework.service.DataCollectionService
import sdk.sahha.android.framework.service.HealthConnectPostService
import sdk.sahha.android.source.SahhaSensor
import sdk.sahha.android.source.SahhaSensorStatus
import javax.inject.Inject
import kotlin.coroutines.resume

private const val tag = "SensorInteractionManager"

internal class SensorInteractionManager @Inject constructor(
    @IoScope private val ioScope: CoroutineScope,
    private val repository: SensorRepo,
    private val healthConnectRepo: HealthConnectRepo,
    private val configRepo: SahhaConfigRepo,
    private val permissionManager: PermissionManager,
    private val notificationManager: SahhaNotificationManager,
    private val alarms: SahhaAlarmManager,
    private val sensorManager: SensorManager,
    private val startPostWorkersUseCase: StartPostWorkersUseCase,
    private val startCollectingSleepDataUseCase: StartCollectingSleepDataUseCase,
    private val startDataCollectionServiceUseCase: StartDataCollectionServiceUseCase,
    private val postAllSensorDataUseCase: PostAllSensorDataUseCase,
    private val getSensorDataUseCase: GetSensorDataUseCase,
    private val startHealthConnectBackgroundTasksUseCase: StartHealthConnectBackgroundTasksUseCase,
    private val postHealthConnectDataUseCase: PostHealthConnectDataUseCase,
    internal val batchDataLogs: BatchDataLogs,
    internal val postBatchData: PostBatchData,
    internal val postSleepDataUseCase: PostSleepDataUseCase,
    internal val postDeviceDataUseCase: PostDeviceDataUseCase,
    internal val postStepDataUseCase: PostStepDataUseCase,
    internal val startCollectingStepCounterData: StartCollectingStepCounterData,
    internal val startCollectingStepDetectorData: StartCollectingStepDetectorData,
    internal val startCollectingPhoneScreenLockDataUseCase: StartCollectingPhoneScreenLockDataUseCase,
    internal val sahhaErrorLogger: SahhaErrorLogger,
) {
    fun postSensorData(
        callback: ((error: String?, success: Boolean) -> Unit)
    ) {
        permissionManager.getHealthConnectSensorStatus { status ->
            ioScope.launch {
                val statusEnabled = status == SahhaSensorStatus.enabled
                val statusDisabled = status == SahhaSensorStatus.disabled

                if (statusEnabled) {
                    Session.healthConnectPostCallback = null
                    Session.healthConnectPostCallback = callback

                    notificationManager.startForegroundService(HealthConnectPostService::class.java)
                    postAllSensorDataUseCase()
                    return@launch
                }
                if (statusDisabled)
                    notificationManager.startForegroundService(HealthConnectPostService::class.java)

                postAllSensorDataUseCase(callback)
            }
        }
    }

    internal fun stopAllBackgroundTasks(context: Context) {
        alarms.stopAllAlarms(context)
        repository.stopAllWorkers()
        unregisterExistingReceiversAndListeners(context.applicationContext)
    }

    internal fun killMainService(context: Context) {
        notificationManager.startForegroundService(
            context,
            DataCollectionService::class.java,
            Intent(context.applicationContext, DataCollectionService::class.java).setAction(
                Constants.ACTION_KILL_SERVICE
            )
        )
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

    internal suspend fun checkAndStartPostWorkers(context: Context) {
        if (!configRepo.getConfig().postSensorDataManually) {
            startPostWorkersUseCase(context)
        }
    }

    internal fun checkAndStartDevicePostWorker(
        callback: ((error: String?, success: Boolean) -> Unit)? = null
    ) {
        healthConnectRepo.startDevicePostWorker(callback)
    }

    internal suspend fun startDataCollection(
        context: Context,
        callback: ((error: String?, success: Boolean) -> Unit)? = null
    ) {
        if (configRepo.getConfig().sensorArray.contains(SahhaSensor.sleep.ordinal)) {
            startCollectingSleepDataUseCase(context)
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
        val query = ioScope.launch {
            try {
                withTimeout(Constants.POST_TIMEOUT_LIMIT_MILLIS) {
                    result = awaitHealthConnectPost()
                }
            } catch (e: TimeoutCancellationException) {
                result = Pair(e.message, false)
                val minutesFromMillis = Constants.POST_TIMEOUT_LIMIT_MILLIS / 1000 / 60
                Log.e(tag, "Task timed out after $minutesFromMillis minutes")
            }
        }

        val minimumTime = ioScope.launch {
            delay(Constants.TEMP_FOREGROUND_NOTIFICATION_DURATION_MILLIS)
        }

        val minimumTimeOrQuery = listOf(query, minimumTime)
        minimumTimeOrQuery.joinAll()
        if (query.isActive) query.cancel()
        if (minimumTime.isActive) minimumTime.cancel()

        callback(result.first, result.second)
    }

    private suspend fun awaitHealthConnectPost() = suspendCancellableCoroutine { cont ->
        ioScope.launch {
            println("awaitHealthConnectPost")
//            postHealthConnectDataUseCase { error, successful ->
//                if (cont.isActive) cont.resume(Pair(error, successful))
//            }
            batchDataLogs()
            if (cont.isActive) cont.resume(Pair(null, true))
        }
    }
}