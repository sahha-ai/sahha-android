package sdk.sahha.android.domain.interaction

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.di.IoScope
import sdk.sahha.android.di.MainScope
import sdk.sahha.android.domain.model.config.SahhaConfiguration
import sdk.sahha.android.domain.model.device_info.DeviceInformation
import sdk.sahha.android.domain.repository.AuthRepo
import sdk.sahha.android.domain.repository.DeviceInfoRepo
import sdk.sahha.android.domain.repository.SahhaConfigRepo
import sdk.sahha.android.domain.use_case.GetScoresUseCase
import sdk.sahha.android.domain.use_case.GetDemographicUseCase
import sdk.sahha.android.domain.use_case.post.PostDemographicUseCase
import sdk.sahha.android.source.SahhaDemographic
import sdk.sahha.android.source.SahhaScoreType
import java.time.LocalDateTime
import java.util.Date
import javax.inject.Inject

private const val tag = "UserDataInteractionManager"

internal class UserDataInteractionManager @Inject constructor(
    @MainScope private val mainScope: CoroutineScope,
    @IoScope private val ioScope: CoroutineScope,
    private val authRepo: AuthRepo,
    private val deviceInfoRepo: DeviceInfoRepo,
    private val sahhaConfigRepo: SahhaConfigRepo,
    private val getScoresUseCase: GetScoresUseCase,
    private val getDemographicUseCase: GetDemographicUseCase,
    private val postDemographicUseCase: PostDemographicUseCase,
) {
    fun getScores(
        types: Set<SahhaScoreType>,
        callback: ((error: String?, value: String?) -> Unit)?
    ) {
        mainScope.launch {
            getScoresUseCase(types, callback)
        }
    }


    @JvmName("getScoresDate")
    fun getScores(
        types: Set<SahhaScoreType>,
        dates: Pair<Date, Date>,
        callback: ((error: String?, value: String?) -> Unit)?,
    ) {
        mainScope.launch {
            getScoresUseCase(types, dates, callback)
        }
    }

    @JvmName("getScoresLocalDateTime")
    fun getScores(
        types: Set<SahhaScoreType>,
        dates: Pair<LocalDateTime, LocalDateTime>,
        callback: ((error: String?, value: String?) -> Unit)?,
    ) {
        mainScope.launch {
            getScoresUseCase(types, dates, callback)
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

    internal suspend fun processAndPutDeviceInfo(
        context: Context,
        lastDeviceInfo: DeviceInformation? = runBlocking { deviceInfoRepo.getDeviceInformation() },
        isAuthenticating: Boolean = false,
        callback: (suspend (error: String?, success: Boolean) -> Unit)? = null
    ) {
        try {
            lastDeviceInfo?.also {
                if (!deviceInfoIsEqual(context, it)) {
                    saveAndPutDeviceInfo(context, callback)
                } else callback?.invoke(null, true)
            } ?: handleSavingDeviceInfo(context, isAuthenticating, callback)
        } catch (e: Exception) {
            Log.w(tag, e.message ?: "Error sending device info")
            callback?.invoke(e.message, false)
        }
    }

    internal suspend fun checkAndResetSensors(
        lastSdkVersion: String,
        config: SahhaConfiguration
    ): Boolean {
        val beforeSensorRefactor = lastSdkVersion < "0.15.17"
        if (beforeSensorRefactor)
            sahhaConfigRepo.saveConfig(config.copy(sensorArray = arrayListOf()))
        return beforeSensorRefactor
    }

    private suspend fun handleSavingDeviceInfo(
        context: Context,
        isAuthenticating: Boolean,
        callback: (suspend (error: String?, success: Boolean) -> Unit)?
    ) {
        if (isAuthenticating) {
            saveAndPutDeviceInfo(context, callback)
            return
        }

        callback?.invoke(null, true)
    }

    private suspend fun saveAndPutDeviceInfo(
        context: Context,
        callback: (suspend (error: String?, success: Boolean) -> Unit)?
    ) {
        val framework = sahhaConfigRepo.getConfig().framework
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val currentDeviceInfo = DeviceInformation(
            sdkId = framework,
            appId = packageInfo.packageName,
            appVersion = packageInfo.versionName
        )
        sahhaConfigRepo.saveDeviceInformation(currentDeviceInfo)

        authRepo.getToken()?.also { token ->
            deviceInfoRepo.putDeviceInformation(token, currentDeviceInfo, callback)
        } ?: callback?.invoke(SahhaErrors.noToken, false)
    }

    private suspend fun deviceInfoIsEqual(
        context: Context,
        lastDeviceInfo: DeviceInformation
    ): Boolean {
        val framework = sahhaConfigRepo.getConfig().framework
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