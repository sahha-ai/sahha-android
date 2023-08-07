package sdk.sahha.android.data.repository

import android.os.Build
import okhttp3.ResponseBody
import retrofit2.Response
import sdk.sahha.android.BuildConfig
import sdk.sahha.android.common.ResponseCode
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.data.remote.SahhaApi
import sdk.sahha.android.domain.model.device_info.DeviceInformation
import sdk.sahha.android.domain.repository.AuthRepo
import sdk.sahha.android.domain.repository.DeviceInfoRepo
import sdk.sahha.android.source.SahhaConverterUtility

class DeviceInfoRepoImpl(
    private val authRepo: AuthRepo,
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

    override suspend fun putDeviceInformation(
        deviceInformation: DeviceInformation,
        callback: ((error: String?, success: Boolean) -> Unit)?
    ) {
        val response = putDeviceInformationResponse(deviceInformation)
        try {
            if (ResponseCode.isSuccessful(response.code())) {
                callback?.invoke(null, true)
                return
            }

            when (response.code()) {
                400 -> {
                    callback?.invoke("${response.code()}: ${response.message()}", false)
                    sahhaErrorLogger.api(response, SahhaErrors.typeRequest)
                }
                401 -> {
                    callback?.invoke("${response.code()}: ${response.message()}", false)
                    sahhaErrorLogger.api(response, SahhaErrors.typeAuthentication)
                }
            }
        } catch (e: Exception) {
            callback?.invoke(e.message, false)
            sahhaErrorLogger.application(
                e.message,
                "putDeviceInformation",
                response.message(),
            )
        }
    }

    private suspend fun putDeviceInformationResponse(deviceInformation: DeviceInformation): Response<ResponseBody> {
        val token = authRepo.getToken() ?: ""
        return api.putDeviceInformation(
            token,
            SahhaConverterUtility.deviceInfoToDeviceInfoSendDto(deviceInformation)
        )
    }
}