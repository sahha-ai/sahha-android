package sdk.sahha.android.interaction

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sdk.sahha.android.data.local.dao.ConfigurationDao
import sdk.sahha.android.di.IoScope
import sdk.sahha.android.di.MainScope
import sdk.sahha.android.domain.model.device_info.DeviceInformation
import sdk.sahha.android.domain.repository.DeviceInfoRepo
import sdk.sahha.android.domain.use_case.AnalyzeProfileUseCase
import sdk.sahha.android.domain.use_case.GetDemographicUseCase
import sdk.sahha.android.domain.use_case.post.PostDemographicUseCase
import sdk.sahha.android.source.SahhaDemographic
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

private const val tag = "UserDataInteractionManager"

class UserDataInteractionManager @Inject constructor(
    @MainScope private val mainScope: CoroutineScope,
    @IoScope private val ioScope: CoroutineScope,
    private val deviceInfoRepo: DeviceInfoRepo,
    private val configurationDao: ConfigurationDao,
    private val analyzeProfileUseCase: AnalyzeProfileUseCase,
    private val getDemographicUseCase: GetDemographicUseCase,
    private val postDemographicUseCase: PostDemographicUseCase,
) {
    fun analyze(
        includeSourceData: Boolean = false,
        callback: ((error: String?, success: String?) -> Unit)?
    ) {
        mainScope.launch {
            analyzeProfileUseCase(includeSourceData, callback)
        }
    }


    @JvmName("analyzeDate")
    fun analyze(
        includeSourceData: Boolean = false,
        dates: Pair<Date, Date>,
        callback: ((error: String?, success: String?) -> Unit)?,
    ) {
        mainScope.launch {
            analyzeProfileUseCase(includeSourceData, dates, callback)
        }
    }

    @JvmName("analyzeLocalDateTime")
    fun analyze(
        includeSourceData: Boolean = false,
        dates: Pair<LocalDateTime, LocalDateTime>,
        callback: ((error: String?, success: String?) -> Unit)?,
    ) {
        mainScope.launch {
            analyzeProfileUseCase(includeSourceData, dates, callback)
        }
    }

    fun getDemographic(callback: ((error: String?, demographic: SahhaDemographic?) -> Unit)?) {
        ioScope.launch {
            getDemographicUseCase(callback)
        }
    }

    fun postDemographic(
        sahhaDemographic: SahhaDemographic,
        callback: ((error: String?, success: Boolean) -> Unit)?
    ) {
        ioScope.launch {
            postDemographicUseCase(sahhaDemographic, callback)
        }
    }

    internal suspend fun processAndPutDeviceInfo(context: Context) {
        try {
            val lastDeviceInfo = configurationDao.getDeviceInformation()
            lastDeviceInfo?.also {
                if (!deviceInfoIsEqual(context, it))
                    saveAndPutDeviceInfo(context)
            } ?: saveAndPutDeviceInfo(context)
        } catch (e: Exception) {
            Log.w(tag, e.message ?: "Error sending device info")
        }
    }

    private suspend fun saveAndPutDeviceInfo(context: Context) {
        val framework = configurationDao.getConfig().framework
        val packageName = context.packageManager.getPackageInfo(context.packageName, 0).packageName
        val currentDeviceInfo = DeviceInformation(
            sdkId = framework,
            appId = packageName
        )
        configurationDao.saveDeviceInformation(currentDeviceInfo)
        deviceInfoRepo.putDeviceInformation(currentDeviceInfo)
    }

    private suspend fun deviceInfoIsEqual(
        context: Context,
        lastDeviceInfo: DeviceInformation
    ): Boolean {
        val framework = configurationDao.getConfig().framework
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
}