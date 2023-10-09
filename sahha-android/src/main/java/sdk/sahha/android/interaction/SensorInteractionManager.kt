package sdk.sahha.android.interaction

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sdk.sahha.android.di.IoScope
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
import sdk.sahha.android.domain.use_case.post.StartHealthConnectPostWorkerUseCase
import sdk.sahha.android.domain.use_case.post.StartPostWorkersUseCase
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensor
import javax.inject.Inject

class SensorInteractionManager @Inject constructor(
    @IoScope private val ioScope: CoroutineScope,
    private val repository: SensorRepo,
    private val healthConnectRepo: HealthConnectRepo,
    private val startPostWorkersUseCase: StartPostWorkersUseCase,
    private val startCollectingSleepDataUseCase: StartCollectingSleepDataUseCase,
    private val startDataCollectionServiceUseCase: StartDataCollectionServiceUseCase,
    private val postAllSensorDataUseCase: PostAllSensorDataUseCase,
    private val getSensorDataUseCase: GetSensorDataUseCase,
    private val startHealthConnectPostWorkerUseCase: StartHealthConnectPostWorkerUseCase,
    private val postHealthConnectDataUseCase: PostHealthConnectDataUseCase,
    internal val postSleepDataUseCase: PostSleepDataUseCase,
    internal val postDeviceDataUseCase: PostDeviceDataUseCase,
    internal val postStepDataUseCase: PostStepDataUseCase,
    internal val startCollectingStepCounterData: StartCollectingStepCounterData,
    internal val startCollectingStepDetectorData: StartCollectingStepDetectorData,
    internal val startCollectingPhoneScreenLockDataUseCase: StartCollectingPhoneScreenLockDataUseCase,
) {

    fun postSensorData(
        callback: ((error: String?, success: Boolean) -> Unit)
    ) {
        ioScope.launch {
            postAllSensorDataUseCase(callback)
        }
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
        if (!Sahha.config.postSensorDataManually) startPostWorkersUseCase()
    }

    internal fun startHealthConnectPostSchedule(
        callback: ((error: String?, success: Boolean) -> Unit)? = null
    ) {
        healthConnectRepo.startHcPostServiceSchedule(callback)
    }

    internal fun startDataCollection(callback: ((error: String?, success: Boolean) -> Unit)? = null) {
        if (Sahha.config.sensorArray.contains(SahhaSensor.sleep.ordinal)) {
            startCollectingSleepDataUseCase()
        }

        // Pedometer/device checkers are in the service
        startDataCollectionService(callback = callback)
    }

    internal suspend fun postStepSessions(
        callback: (suspend (error: String?, success: Boolean) -> Unit)?
    ) {
        repository.postStepSessions(repository.getAllStepSessions(), callback)
    }

    private fun startDataCollectionService(
        icon: Int? = null,
        title: String? = null,
        shortDescription: String? = null,
        callback: ((error: String?, success: Boolean) -> Unit)? = null
    ) {
        startDataCollectionServiceUseCase(icon, title, shortDescription, callback)
    }
}