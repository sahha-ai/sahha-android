package sdk.sahha.android.data.repository

import android.os.Build
import okhttp3.ResponseBody
import retrofit2.Response
import sdk.sahha.android.BuildConfig
import sdk.sahha.android.common.ResponseCode
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.TokenBearer
import sdk.sahha.android.data.local.dao.ConfigurationDao
import sdk.sahha.android.data.remote.SahhaApi
import sdk.sahha.android.domain.model.device_info.DeviceInformation
import sdk.sahha.android.domain.repository.DeviceInfoRepo
import sdk.sahha.android.common.SahhaConverterUtility

private const val tag = "DeviceInfoRepoImpl"

internal class DeviceInfoRepoImpl(
    private val configDao: ConfigurationDao,
    private val api: SahhaApi,
    private val sahhaErrorLogger: SahhaErrorLogger
) : DeviceInfoRepo {
    override fun getPlatform(): String {
        return "Android"
    }

    override fun getPlatformVer(): String {
        return "${Build.VERSION.SDK_INT}"
    }

    override fun getDeviceModel(): String {
        return "${Build.BRAND}:${Build.DEVICE}"
    }

    override fun getSdkVersion(): String {
        return BuildConfig.SDK_VERSION_NAME
    }

    override suspend fun getDeviceInformation(): DeviceInformation? {
        return configDao.getDeviceInformation()
    }

    override suspend fun putDeviceInformation(
        token: String,
        deviceInformation: DeviceInformation,
        callback: (suspend (error: String?, success: Boolean) -> Unit)?
    ) {
        val response = putDeviceInformationResponse(token, deviceInformation)
        try {
            if (ResponseCode.isSuccessful(response.code())) {
                callback?.invoke(null, true)
                return
            }

            when (response.code()) {
                401 -> {
                    callback?.invoke("${response.code()}: ${response.message()}", false)
                    sahhaErrorLogger.api(response)
                }

                else -> {
                    callback?.invoke("${response.code()}: ${response.message()}", false)
                    sahhaErrorLogger.api(response)
                }
            }
        } catch (e: Exception) {
            callback?.invoke(e.message, false)
            sahhaErrorLogger.application(
                e.message ?: SahhaErrors.somethingWentWrong,
                tag,
                "putDeviceInformation",
                deviceInformation.toString(),
            )
        }
    }

    private suspend fun putDeviceInformationResponse(token: String, deviceInformation: DeviceInformation): Response<ResponseBody> {
        return api.putDeviceInformation(
            TokenBearer(token),
            SahhaConverterUtility.deviceInfoToDeviceInfoSendDto(deviceInformation)
        )
    }
}