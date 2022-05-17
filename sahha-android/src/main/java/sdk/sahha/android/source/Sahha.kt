package sdk.sahha.android.source

import android.app.Application
import android.content.Context
import androidx.annotation.Keep
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import kotlinx.coroutines.launch
import sdk.sahha.android.BuildConfig
import sdk.sahha.android.common.SahhaPermissions
import sdk.sahha.android.data.Constants
import sdk.sahha.android.di.ManualDependencies
import sdk.sahha.android.domain.model.categories.Motion
import sdk.sahha.android.domain.model.config.SahhaConfiguration
import java.time.LocalDateTime


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
            AppCenter.start(
                application,
                getCorrectAppCenterKey(sahhaSettings.environment),
                Analytics::class.java, Crashes::class.java
            )
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
        di.defaultScope.launch {
            config = di.configurationDao.getConfig()
            startDataCollection(callback)
            checkAndStartPostWorkers()
        }
    }

    fun analyze(
        dates: Pair<LocalDateTime, LocalDateTime>? = null,
        callback: ((error: String?, success: String?) -> Unit)?,
    ) {
        di.defaultScope.launch {
            di.analyzeProfileUseCase(dates, callback)
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
                //motion.activate(context, callback)
                SahhaPermissions.enableSensor(context, sensor) { sensorStatus ->
                    callback(null, sensorStatus)
                }
                return
            }
            SahhaSensor.sleep -> {
                //motion.activate(context, callback)
                SahhaPermissions.enableSensor(context, sensor) { sensorStatus ->
                    callback(null, sensorStatus)
                }
                return
            }
            SahhaSensor.device -> {
                callback(null, SahhaSensorStatus.enabled)
                return
            }
        }
    }

    fun getSensorStatus(
        context: Context,
        sensor: SahhaSensor,
        callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)
    ) {
        when (sensor) {
            SahhaSensor.pedometer -> {
                //callback(null, SahhaPermissions.activityRecognitionGranted(context))
                SahhaPermissions.getSensorStatus(context, sensor) { sensorStatus ->
                    callback(null, sensorStatus)
                }
            }
            SahhaSensor.sleep -> {
                //callback(null, SahhaPermissions.activityRecognitionGranted(context))
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
        val sensorEnums = convertToEnums(settings.sensors)
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

    private fun getCorrectAppCenterKey(environment: Enum<SahhaEnvironment>): String {
        if (environment == SahhaEnvironment.production) {
            return BuildConfig.APP_CENTER_PROD
        }
        return BuildConfig.APP_CENTER_DEV
    }
}