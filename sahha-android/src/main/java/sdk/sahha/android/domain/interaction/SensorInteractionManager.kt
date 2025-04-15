package sdk.sahha.android.domain.interaction

import android.content.Context
import android.content.Intent
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import sdk.sahha.android.common.Constants
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaReceiversAndListeners
import sdk.sahha.android.common.Session
import sdk.sahha.android.domain.manager.ConnectionStateManager
import sdk.sahha.android.domain.manager.PermissionManager
import sdk.sahha.android.domain.manager.SahhaNotificationManager
import sdk.sahha.android.domain.model.dto.QueryTime
import sdk.sahha.android.domain.model.dto.toQueryTime
import sdk.sahha.android.domain.repository.BatchedDataRepo
import sdk.sahha.android.domain.repository.HealthConnectRepo
import sdk.sahha.android.domain.repository.SahhaConfigRepo
import sdk.sahha.android.domain.repository.SensorRepo
import sdk.sahha.android.domain.use_case.CalculateBatchLimit
import sdk.sahha.android.domain.use_case.GetSensorDataUseCase
import sdk.sahha.android.domain.use_case.background.BatchAggregateLogs
import sdk.sahha.android.domain.use_case.background.BatchDataLogs
import sdk.sahha.android.domain.use_case.background.StartCollectingPhoneScreenLockDataUseCase
import sdk.sahha.android.domain.use_case.background.StartCollectingStepDetectorData
import sdk.sahha.android.domain.use_case.background.StartDataCollectionServiceUseCase
import sdk.sahha.android.domain.use_case.metadata.AddMetadata
import sdk.sahha.android.domain.use_case.post.PostAllSensorDataUseCase
import sdk.sahha.android.domain.use_case.post.PostBatchData
import sdk.sahha.android.domain.use_case.post.PostDeviceDataUseCase
import sdk.sahha.android.domain.use_case.post.PostSleepDataUseCase
import sdk.sahha.android.domain.use_case.post.PostStepDataUseCase
import sdk.sahha.android.domain.use_case.post.StartPostWorkersUseCase
import sdk.sahha.android.framework.service.DataCollectionService
import sdk.sahha.android.source.SahhaSensor
import sdk.sahha.android.source.SahhaSensorStatus
import sdk.sahha.android.source.SahhaStatInterval
import java.time.ZonedDateTime
import javax.inject.Inject

private const val tag = "SensorInteractionManager"

internal class SensorInteractionManager @Inject constructor(
    private val healthConnectRepo: HealthConnectRepo,
    private val configRepo: SahhaConfigRepo,
    private val sensorRepo: SensorRepo,
    private val batchedDataRepo: BatchedDataRepo,
    private val permissionManager: PermissionManager,
    private val notificationManager: SahhaNotificationManager,
    private val sensorManager: SensorManager,
    private val connectionStateManager: ConnectionStateManager,
    private val startPostWorkersUseCase: StartPostWorkersUseCase,
    private val startDataCollectionServiceUseCase: StartDataCollectionServiceUseCase,
    private val postAllSensorDataUseCase: PostAllSensorDataUseCase,
    private val getSensorDataUseCase: GetSensorDataUseCase,
    private val calculateBatchLimit: CalculateBatchLimit,
    private val batchAggregateLogs: BatchAggregateLogs,
    internal val batchDataLogs: BatchDataLogs,
    internal val postBatchData: PostBatchData,
    internal val postSleepDataUseCase: PostSleepDataUseCase,
    internal val postDeviceDataUseCase: PostDeviceDataUseCase,
    internal val postStepDataUseCase: PostStepDataUseCase,
    internal val startCollectingStepDetectorData: StartCollectingStepDetectorData,
    internal val startCollectingPhoneScreenLockDataUseCase: StartCollectingPhoneScreenLockDataUseCase,
    internal val addMetadata: AddMetadata,
    internal val sahhaErrorLogger: SahhaErrorLogger,
) {
    suspend fun postSensorData(
        context: Context,
        callback: ((error: String?, success: Boolean) -> Unit)
    ) = coroutineScope {
        permissionManager.getHealthConnectSensorStatus(
            context = context,
            Session.sensors ?: setOf()
        ) { _, status ->
            val statusEnabled = status == SahhaSensorStatus.enabled
            val statusDisabled = status == SahhaSensorStatus.disabled

            if (statusEnabled) {
                Session.healthConnectPostCallback = null
                Session.healthConnectPostCallback = callback

                postAllSensorDataUseCase()
                sensorRepo.startHealthConnectQueryWorker(
                    Constants.FIFTEEN_MINUTES,
                    Constants.HEALTH_CONNECT_QUERY_WORKER_TAG
                )
                return@getHealthConnectSensorStatus
            }
            if (statusDisabled) sensorRepo.startHealthConnectQueryWorker(
                Constants.FIFTEEN_MINUTES,
                Constants.HEALTH_CONNECT_QUERY_WORKER_TAG
            )

            postAllSensorDataUseCase(callback)
        }
    }

    internal fun stopAllBackgroundTasks(context: Context) {
        sensorRepo.stopAllWorkers()
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

    internal suspend fun getSensorData(
        sensor: SahhaSensor,
        callback: (suspend (error: String?, success: String?) -> Unit)
    ) = coroutineScope {
        getSensorDataUseCase(sensor, callback)
    }

    internal suspend fun checkAndStartPostWorkers(context: Context) = coroutineScope {
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
        // Pedometer/device checkers are in the service
        startDataCollectionServiceUseCase(callback = callback)
    }

    internal suspend fun postStepSessions(
        callback: (suspend (error: String?, success: Boolean) -> Unit)?
    ) = coroutineScope {
        val sessions = sensorRepo.getAllStepSessions()
        val metadataAdded = addMetadata(
            dataList = sessions,
            saveData = sensorRepo::saveStepSessions
        )
        sensorRepo.postStepSessions(metadataAdded, callback)
    }

    internal suspend fun queryWithMinimumDelay(
        afterTimer: () -> Unit,
        callback: (error: String?, successful: Boolean) -> Unit
    ) = coroutineScope {
        var result: Pair<String?, Boolean> = Pair(SahhaErrors.failedToPostAllData, false)
        try {
            result = awaitHealthConnectQuery()
//            batchDailyAndHourlyAggregatesAsync() //TODO: Re-enable when back-end is ready for aggregates
            tryStartPostWorker()
        } catch (e: Exception) {
            result = Pair(e.message, false)
            Log.e(tag, e.message ?: "Something went wrong querying Health Connect data")
        }
        callback(result.first, result.second)

        withContext(Dispatchers.IO) {
            Thread.sleep(Constants.TEMP_FOREGROUND_NOTIFICATION_DURATION_MILLIS)
            afterTimer()
        }
    }

    private fun tryStartPostWorker() {
        if (connectionStateManager.isInternetAvailable())
            sensorRepo.startOneTimeBatchedDataPostWorker(
                Constants.SAHHA_DATA_LOG_WORKER_TAG
            )
    }

    private fun batchLimitReached(
        batchCount: Int = runBlocking { batchedDataRepo.getBatchedData().count() }
    ): Boolean {
        return (batchCount / calculateBatchLimit()) >= 1
    }

    private suspend fun awaitHealthConnectQuery(): Pair<String?, Boolean> = coroutineScope {
        do {
            val hasMore = batchDataLogs()
        } while (hasMore)
        Pair(null, true)
    }

    private suspend fun batchDailyAndHourlyAggregatesAsync() = coroutineScope {
        val tasks = listOf(
            launch(Dispatchers.IO) { batchAggregateLogsAllSensors(SahhaStatInterval.hour) },
            launch(Dispatchers.IO) { batchAggregateLogsAllSensors(SahhaStatInterval.day) }
        )

        tasks.joinAll()
    }

    private suspend fun batchAggregateLogsAllSensors(interval: SahhaStatInterval) = coroutineScope {
        val last30DaysEpochMilli = ZonedDateTime.now().minusDays(30).toInstant().toEpochMilli()
        val queryTime =
            if (interval == SahhaStatInterval.hour)
                healthConnectRepo.getLastCustomQuery(Constants.AGGREGATE_QUERY_ID_HOUR)
                    ?.toQueryTime()
                    ?: QueryTime(
                        id = Constants.AGGREGATE_QUERY_ID_HOUR,
                        timeEpochMilli = last30DaysEpochMilli
                    )
            else
                healthConnectRepo.getLastCustomQuery(Constants.AGGREGATE_QUERY_ID_DAY)
                    ?.toQueryTime() ?: QueryTime(
                    id = Constants.AGGREGATE_QUERY_ID_DAY,
                    timeEpochMilli = last30DaysEpochMilli
                )

        val jobs = mutableListOf<Job>()
        SahhaSensor.values().forEach { sensor ->
            jobs += launch(Dispatchers.IO) {
                val result = batchAggregateLogs(
                    sensor,
                    interval,
                    queryTime,
                )

                result.second?.also { logs ->
                    batchedDataRepo.saveBatchedData(logs)
                }
            }
        }
        jobs.joinAll()
    }
}