package sdk.sahha.android.source

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.annotation.Keep
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import sdk.sahha.android.common.enums.HealthConnectSensor
import sdk.sahha.android.data.Constants
import sdk.sahha.android.di.ManualDependencies
import sdk.sahha.android.domain.model.categories.Motion
import sdk.sahha.android.domain.model.config.SahhaConfiguration
import sdk.sahha.android.domain.model.device_info.DeviceInformation
import sdk.sahha.android.domain.use_case.*
import java.time.LocalDateTime
import java.util.*

private val tag = "Sahha"

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
        try {
            if (!diInitialized())
                di = ManualDependencies(sahhaSettings.environment)
            di.setDependencies(application)

            di.mainScope.launch {
                config = di.configurationDao.getConfig()

                listOf(
                    async { saveConfiguration(sahhaSettings) },
                    async { saveNotificationConfig(sahhaSettings.notificationSettings) },
                    async { processAndPutDeviceInfo(application) }
                ).joinAll()

                start(callback)
            }
        } catch (e: Exception) {
            callback?.invoke(e.message, false)
        }
    }

    internal suspend fun processAndPutDeviceInfo(context: Context) {
        try {
            val lastDeviceInfo = di.configurationDao.getDeviceInformation()
            lastDeviceInfo?.also {
                if (!deviceInfoIsEqual(context, it))
                    saveAndPutDeviceInfo(context)
            } ?: saveAndPutDeviceInfo(context)
        } catch (e: Exception) {
            Log.w(tag, e.message ?: "Error sending device info")
        }
    }

    private suspend fun saveAndPutDeviceInfo(context: Context) {
        val framework = di.configurationDao.getConfig().framework
        val packageName = context.packageManager.getPackageInfo(context.packageName, 0).packageName
        val currentDeviceInfo = DeviceInformation(
            sdkId = framework,
            appId = packageName
        )
        di.configurationDao.saveDeviceInformation(currentDeviceInfo)
        di.remotePostRepo.putDeviceInformation(currentDeviceInfo)
    }

    private suspend fun deviceInfoIsEqual(
        context: Context,
        lastDeviceInfo: DeviceInformation
    ): Boolean {
        val framework = di.configurationDao.getConfig().framework
        val packageName = context.packageManager.getPackageInfo(context.packageName, 0).packageName
        val currentDeviceInfo = DeviceInformation(sdkId = framework, appId = packageName)

        if (currentDeviceInfo.deviceType != lastDeviceInfo.deviceType) return false
        if (currentDeviceInfo.deviceModel != lastDeviceInfo.deviceModel) return false
        if (currentDeviceInfo.appId != lastDeviceInfo.appId) return false
        if (currentDeviceInfo.sdkId != lastDeviceInfo.sdkId) return false
        if (currentDeviceInfo.sdkVersion != lastDeviceInfo.sdkVersion) return false
        if (currentDeviceInfo.system != lastDeviceInfo.system) return false
        if (currentDeviceInfo.systemVersion != lastDeviceInfo.systemVersion) return false
        if (currentDeviceInfo.timeZone != lastDeviceInfo.timeZone) return false
        return true
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
                    async { checkAndStartPostWorkers() },
                ).joinAll()
                callback?.invoke(null, true)
            }
        } catch (e: Exception) {
            callback?.invoke("Error: ${e.message}", false)
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
        callback: ((error: String?, success: Boolean) -> Unit)
    ) {
        di.mainScope.launch {
            di.postAllSensorDataUseCase(callback)
        }
    }

    fun postHealthConnectData(
        healthConnectSensors: Set<Enum<HealthConnectSensor>> = HealthConnectSensor.values().toSet(),
        callback: ((error: String?, successful: Boolean) -> Unit)
    ) {
        di.mainScope.launch {
            di.postHealthConnectDataUseCase(
                healthConnectSensors,
                callback
            )
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

    suspend fun getHealthConnectData(
        healthConnectSensor: HealthConnectSensor,
        callback: ((error: String?, success: String?) -> Unit)
    ) {
        di.getHealthConnectDataUseCase(
            healthConnectSensor,
            callback
        )
    }

    fun openAppSettings(context: Context) {
        di.openAppSettingsUseCase(context)
    }

    fun enableSensors(
        context: Context,
        callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)
    ) {
        di.permissionRepo.enableSensors(context, callback)
    }

    fun enableHealthConnect(
        context: Context,
        callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)
    ) {
        di.permissionRepo.enableHealthConnect(context, callback)
    }

    fun getSensorStatus(
        context: Context,
        callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)
    ) {
        di.permissionRepo.getSensorStatus(context, callback)
    }

    fun getHealthConnectStatus(
        context: Context,
        callback: suspend ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)
    ) {
        di.mainScope.launch {
            di.permissionRepo.getHealthConnectStatus(context, callback)
        }
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
        val sensorEnums = settings.sensors?.let {
            convertToEnums(it)
        } ?: convertToEnums(getDefaultSensors())

        di.configurationDao.saveConfig(
            SahhaConfiguration(
                settings.environment.ordinal,
                settings.framework.name,
                sensorEnums,
                settings.postSensorDataManually
            )
        )
    }

    internal fun getDefaultSensors(): Set<SahhaSensor> {
        val sensors = mutableSetOf<SahhaSensor>()
        val sensorsExcludingHealthConnect = SahhaSensor.values().size - 1
        for (i in 0 until sensorsExcludingHealthConnect) {
            sensors.add(SahhaSensor.values()[i])
        }

        return sensors
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