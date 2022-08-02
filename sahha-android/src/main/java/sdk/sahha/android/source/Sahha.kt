package sdk.sahha.android.source

import android.app.Application
import android.content.Context
import androidx.annotation.Keep
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
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

    fun diInitialized(): Boolean {
        return ::di.isInitialized
    }

    fun configure(
        application: Application,
        sahhaSettings: SahhaSettings,
        callback: ((error: String?, success: Boolean) -> Unit)? = null
    ) {
        if (!diInitialized())
            di = ManualDependencies(sahhaSettings.environment)
        di.setDependencies(application)

        di.mainScope.launch {
            config = di.configurationDao.getConfig()

            listOf(
                async { saveConfiguration(sahhaSettings) },
                async { saveNotificationConfig(sahhaSettings.notificationSettings) }
            ).joinAll()

            start(callback)
        }
    }

    fun authenticate(
        profileToken: String,
        refreshToken: String,
        callback: ((error: String?, success: Boolean) -> Unit)? = null
    ) {
        di.mainScope.launch {
            di.saveTokensUseCase(profileToken, refreshToken, callback)
        }
    }

    internal fun start(callback: ((error: String?, success: Boolean) -> Unit)? = null) {
        try {
            di.mainScope.launch {
                di.backgroundRepo.stopAllWorkers()
                config = di.configurationDao.getConfig()
                listOf(
                    async { startDataCollection(callback) },
                    async { checkAndStartPostWorkers() }
                ).joinAll()
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
        di.mainScope.launch {
            di.analyzeProfileUseCase(includeSourceData, callback)
        }
    }


    @JvmName("analyzeDate")
    fun analyze(
        includeSourceData: Boolean = false,
        dates: Pair<Date, Date>,
        callback: ((error: String?, success: String?) -> Unit)?,
    ) {
        di.mainScope.launch {
            di.analyzeProfileUseCase(includeSourceData, dates, callback)
        }
    }

    @JvmName("analyzeLocalDateTime")
    fun analyze(
        includeSourceData: Boolean = false,
        dates: Pair<LocalDateTime, LocalDateTime>,
        callback: ((error: String?, success: String?) -> Unit)?,
    ) {
        di.mainScope.launch {
            di.analyzeProfileUseCase(includeSourceData, dates, callback)
        }
    }

    fun getDemographic(callback: ((error: String?, demographic: SahhaDemographic?) -> Unit)?) {
        di.mainScope.launch {
            di.getDemographicUseCase(callback)
        }
    }

    fun postDemographic(
        sahhaDemographic: SahhaDemographic,
        callback: ((error: String?, success: Boolean) -> Unit)?
    ) {
        di.mainScope.launch {
            di.postDemographicUseCase(sahhaDemographic, callback)
        }
    }

    fun postSensorData(
        sensors: Set<Enum<SahhaSensor>>? = null,
        callback: ((error: String?, success: Boolean) -> Unit)
    ) {
        di.mainScope.launch {
            di.postAllSensorDataUseCase(sensors, callback)
        }
    }

    internal fun getSensorData(
        sensor: SahhaSensor,
        callback: ((error: String?, success: String?) -> Unit)
    ) {
        di.mainScope.launch {
            di.getSensorDataUseCase(sensor, callback)
        }
    }

    fun openAppSettings(context: Context) {
        di.openAppSettingsUseCase(context)
    }

    fun enableSensors(
        context: Context,
        sensors: Set<SahhaSensor> = SahhaSensor.values().toSet(),
        callback: ((error: String?, statuses: Map<Enum<SahhaSensor>, Enum<SahhaSensorStatus>>) -> Unit)
    ) {
        di.permissionRepo.enableSensors(context, sensors, callback)
    }

    fun getSensorStatuses(
        context: Context,
        sensors: Set<SahhaSensor> = SahhaSensor.values().toSet(),
        callback: ((error: String?, statuses: Map<Enum<SahhaSensor>, Enum<SahhaSensorStatus>>) -> Unit)
    ) {
        di.permissionRepo.getSensorStatuses(context, sensors, callback)
    }

    private fun checkAndStartPostWorkers() {
        if (!config.postSensorDataManually) di.startPostWorkersUseCase()
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
        val sensorEnums = if (::config.isInitialized) config.sensorArray else arrayListOf()

        di.configurationDao.saveConfig(
            SahhaConfiguration(
                settings.environment.ordinal,
                settings.framework.name,
                sensorEnums,
                settings.postSensorDataManually
            )
        )
    }

    private suspend fun saveNotificationConfig(config: SahhaNotificationConfiguration?) {
        di.configurationDao.saveNotificationConfig(
            config ?: SahhaNotificationConfiguration()
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