package sdk.sahha.android.source

import android.app.Application
import android.content.Context
import androidx.annotation.Keep
import kotlinx.coroutines.launch
import sdk.sahha.android.BuildConfig
import sdk.sahha.android.common.SahhaPermissions
import sdk.sahha.android.data.Constants
import sdk.sahha.android.di.ManualDependencies
import sdk.sahha.android.domain.model.categories.Motion
import sdk.sahha.android.domain.model.config.SahhaConfiguration
import java.time.LocalDateTime
import java.util.*


@Keep
object Sahha {
    private lateinit var config: SahhaConfiguration
    internal lateinit var di: ManualDependencies
    internal val notifications by lazy { di.notifications }
    internal val motion by lazy {
        Motion(
            di.openAppSettingsUseCase,
            di.setPermissionLogicUseCase,
            di.configurationDao,
            di.ioScope,
            di.activateUseCase,
        )
    }

    val timeManager by lazy { di.timeManager }

    fun configure(
        application: Application,
        sahhaSettings: SahhaSettings
    ) {
        di = ManualDependencies(sahhaSettings.environment)
        di.setDependencies(application)
        di.ioScope.launch {
            saveConfiguration(sahhaSettings)
            start()
        }
    }

    fun authenticate(
        profileToken: String,
        refreshToken: String,
        callback: ((error: String?, success: Boolean) -> Unit)? = null
    ) {
        di.ioScope.launch {
            di.saveTokensUseCase(profileToken, refreshToken, callback)
        }
    }

    fun start(callback: ((error: String?, success: Boolean) -> Unit)? = null) {
            try {
                di.defaultScope.launch {
                    config = di.configurationDao.getConfig()
                    startDataCollection(callback)
                    checkAndStartPostWorkers()
                }
            } catch (e: Exception) {
                callback?.also { it("Error: ${e.message}", false) }
                di.sahhaErrorLogger.application(e.message, "start", null)
            }
    }

    fun analyze(
        includeSourceData: Boolean = false,
        callback: ((error: String?, success: String?) -> Unit)?
    ) {
        di.defaultScope.launch {
            di.analyzeProfileUseCase(includeSourceData, callback)
        }
    }


    @JvmName("analyzeDate")
    fun analyze(
        includeSourceData: Boolean = false,
        dates: Pair<Date, Date>,
        callback: ((error: String?, success: String?) -> Unit)?,
    ) {
        di.defaultScope.launch {
            di.analyzeProfileUseCase(includeSourceData, dates, callback)
        }
    }

    @JvmName("analyzeLocalDateTime")
    fun analyze(
        includeSourceData: Boolean = false,
        dates: Pair<LocalDateTime, LocalDateTime>,
        callback: ((error: String?, success: String?) -> Unit)?,
    ) {
        di.defaultScope.launch {
            di.analyzeProfileUseCase(includeSourceData, dates, callback)
        }
    }

    fun getDemographic(callback: ((error: String?, demographic: SahhaDemographic?) -> Unit)?) {
        di.defaultScope.launch {
            di.getDemographicUseCase(callback)
        }
    }

    fun postDemographic(
        sahhaDemographic: SahhaDemographic,
        callback: ((error: String?, success: Boolean) -> Unit)?
    ) {
        di.defaultScope.launch {
            di.postDemographicUseCase(sahhaDemographic, callback)
        }
    }

    fun postSensorData(
        sensors: Set<Enum<SahhaSensor>>? = null,
        callback: ((error: String?, success: Boolean) -> Unit)
    ) {
        di.ioScope.launch {
            di.postAllSensorDataUseCase(sensors, callback)
        }
    }

    fun openAppSettings(context: Context) {
        di.openAppSettingsUseCase(context)
    }

    fun enableSensor(
        context: Context,
        sensor: SahhaSensor,
        callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)
    ) {
        when (sensor) {
            SahhaSensor.pedometer -> {
                SahhaPermissions.enableSensor(context, sensor) { sensorStatus ->
                    callback(null, sensorStatus)
                }
            }
            SahhaSensor.sleep -> {
                SahhaPermissions.enableSensor(context, sensor) { sensorStatus ->
                    callback(null, sensorStatus)
                }
            }
            SahhaSensor.device -> {
                callback(null, SahhaSensorStatus.enabled)
            }
        }
        start()
    }

    fun getSensorStatus(
        context: Context,
        sensor: SahhaSensor,
        callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)
    ) {
        when (sensor) {
            SahhaSensor.pedometer -> {
                SahhaPermissions.getSensorStatus(context, sensor) { sensorStatus ->
                    callback(null, sensorStatus)
                }
            }
            SahhaSensor.sleep -> {
                SahhaPermissions.getSensorStatus(context, sensor) { sensorStatus ->
                    callback(null, sensorStatus)
                }
            }
            SahhaSensor.device -> {
                callback(null, SahhaSensorStatus.enabled)
            }
            else -> {
                callback(
                    null,
                    SahhaSensorStatus.enabled
                )
            }
        }
    }

    private fun checkAndStartPostWorkers() {
        if (config.postSensorDataManually) {
            di.backgroundRepo.stopWorkerByTag(Constants.SLEEP_POST_WORKER_TAG)
            di.backgroundRepo.stopWorkerByTag(Constants.DEVICE_POST_WORKER_TAG)
            di.backgroundRepo.stopWorkerByTag(Constants.STEP_POST_WORKER_TAG)
        } else {
            di.startPostWorkersUseCase()
        }
    }

    private fun startDataCollection(callback: ((error: String?, success: Boolean) -> Unit)?) {
        if (config.sensorArray.contains(SahhaSensor.sleep.ordinal)) {
            di.startCollectingSleepDataUseCase()
        }

        // Pedometer/device checkers are in the service
        startDataCollectionService(callback = callback)
    }

    private fun startDataCollectionService(
        icon: Int? = null,
        title: String? = null,
        shortDescription: String? = null,
        callback: ((error: String?, success: Boolean) -> Unit)? = null
    ) {
        di.startDataCollectionServiceUseCase(icon, title, shortDescription, callback)
    }

    private suspend fun saveConfiguration(
        settings: SahhaSettings
    ) {
        val sensorEnums = settings.sensors?.let {
            convertToEnums(it)
        } ?: convertToEnums(SahhaSensor.values().toSet())

        di.configurationDao.saveConfig(
            SahhaConfiguration(
                settings.environment.ordinal,
                settings.framework.name,
                sensorEnums,
                settings.postSensorDataManually
            )
        )
    }

    private fun convertToEnums(sensorSet: Set<Enum<SahhaSensor>>): ArrayList<Int> {
        val sensorEnums = arrayListOf<Int>()
        sensorSet.forEach {
            sensorEnums.add(it.ordinal)
        }
        return sensorEnums
    }
}