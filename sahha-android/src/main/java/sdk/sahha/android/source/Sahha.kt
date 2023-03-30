package sdk.sahha.android.source

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.annotation.Keep
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import sdk.sahha.android.data.Constants.UERT
import sdk.sahha.android.data.Constants.UET
import sdk.sahha.android.di.ManualDependencies
import sdk.sahha.android.domain.model.categories.Motion
import sdk.sahha.android.domain.model.config.SahhaConfiguration
import sdk.sahha.android.domain.model.device_info.DeviceInformation
import sdk.sahha.android.domain.model.security.EncryptUtility
import java.time.LocalDateTime
import java.util.*

private val tag = "Sahha"

@Keep
object Sahha {
    private lateinit var config: SahhaConfiguration
    internal lateinit var di: ManualDependencies
    internal val notificationManager by lazy { di.notificationManager }
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

        runBlocking {
            di.setDatabase(application)
            saveConfiguration(sahhaSettings)
            di.setDependencies(application)

            migrateDataIfNeeded { error, success ->
                if (!success) {
                    callback?.invoke(error, false)
                    return@migrateDataIfNeeded
                }

                launch {
                    config = di.configurationDao.getConfig()

                    listOf(
                        async { saveNotificationConfig(sahhaSettings.notificationSettings) },
                        async { processAndPutDeviceInfo(application) }
                    ).joinAll()

                    start(callback)
                }
            }
        }
    }

    internal suspend fun migrateDataIfNeeded(callback: (error: String?, success: Boolean) -> Unit) {
        val oldData = getOldDataFromEncryptUtilityTable()

        if (oldData.isEmpty()) {
            callback(null, true)
            return
        }

        val oldToken: String? = decryptOldData(UET)
        val oldRefreshToken: String? = decryptOldData(UERT)

        val bothTokensAreNull = setOf(oldToken, oldRefreshToken).all { it == null }
        if (bothTokensAreNull) {
            callback(null, true)
            return
        }

        saveDataToEncryptedSharedPreferences(
            setOf(
                oldToken!!,
                oldRefreshToken!!
            )
        ) { error, success ->
            if (success) {
                di.ioScope.launch {
                    deleteOldDataFromEncryptUtilityTable()
                    callback(null, true)
                }
            } else {
                callback(error, false)
            }
        }
    }

    private suspend fun getOldDataFromEncryptUtilityTable(): Set<EncryptUtility> {
        return setOf(
            di.securityDao.getEncryptUtility(UET),
            di.securityDao.getEncryptUtility(UERT),
        )
    }

    private suspend fun decryptOldData(alias: String): String? {
        val data = di.securityDao.getEncryptUtility(alias)
        return when (data) {
            null -> null
            else -> di.decryptor.decrypt(alias)
        }
    }

    private fun saveDataToEncryptedSharedPreferences(
        decryptedData: Set<String>,
        callback: (error: String?, success: Boolean) -> Unit
    ) {
        di.authRepo.saveEncryptedTokens(
            decryptedData.elementAt(0), decryptedData.elementAt(1)
        ) { error, successful ->
            callback(error, successful)
        }
    }

    private suspend fun deleteOldDataFromEncryptUtilityTable() {
        di.securityDao.deleteAllEncryptedData()
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
        di.deviceInfoRepo.putDeviceInformation(currentDeviceInfo)
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
        appId: String,
        appSecret: String,
        externalId: String,
        callback: ((error: String?, success: Boolean) -> Unit)
    ) {
        di.mainScope.launch {
            di.saveTokensUseCase(appId, appSecret, externalId, callback)
        }
    }

    internal fun start(callback: ((error: String?, success: Boolean) -> Unit)? = null) {
        try {
            runBlocking {
                di.sensorRepo.stopAllWorkers()
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
        callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)
    ) {
        di.permissionRepo.enableSensors(context, callback)
    }

    fun getSensorStatus(
        context: Context,
        callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)
    ) {
        di.permissionRepo.getSensorStatus(context, callback)
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
        } ?: convertToEnums(SahhaSensor.values().toSet())

        di.configurationDao.saveConfig(
            SahhaConfiguration(
                settings.environment.ordinal,
                settings.framework.name,
                sensorEnums,
                false
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