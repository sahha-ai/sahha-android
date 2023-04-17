package sdk.sahha.android.data.repository

import android.os.Build
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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
        TODO("Not yet implemented")
    }

    override suspend fun putDeviceInformation(deviceInformation: DeviceInformation) {
        val call = putDeviceInformationResponse(deviceInformation)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (ResponseCode.isSuccessful(response.code())) {
                    return
                }

                sahhaErrorLogger.api(call, response)
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                sahhaErrorLogger.api(
                    call,
                    SahhaErrors.typeResponse,
                    null,
                    t.message ?: SahhaErrors.responseFailure
                )
            }
        })
    }

    private suspend fun putDeviceInformationResponse(deviceInformation: DeviceInformation): Call<ResponseBody> {
        val token = authRepo.getToken()!!
        return api.putDeviceInformation(
            token,
            SahhaConverterUtility.deviceInfoToDeviceInfoSendDto(deviceInformation)
        )
    }
}