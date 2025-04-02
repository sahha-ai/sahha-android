package sdk.sahha.android.data.repository

import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.domain.model.device_info.DeviceInformation
import sdk.sahha.android.domain.repository.DeviceInfoRepo

internal class MockDeviceInfoRepoImpl: DeviceInfoRepo {
    override fun getPlatform(): String {
        return "Android"
    }

    override fun getPlatformVer(): String {
        return "platform_version"
    }

    override fun getDeviceModel(): String {
        return "device_model"
    }

    override fun getSdkVersion(): String {
        return "sdk_version"
    }

    override suspend fun getDeviceInformation(): DeviceInformation? {
        return DeviceInformation(
            sdkId = "sdk_id",
            appId = "app_id",
//            appVersion = "app_version"
        )
    }

    override suspend fun putDeviceInformation(
        token: String,
        deviceInformation: DeviceInformation,
        callback: (suspend (error: String?, success: Boolean) -> Unit)?
    ) {
        if (token.isEmpty())
            callback?.invoke(SahhaErrors.noToken, false)
        else callback?.invoke(null, true)
    }

    override suspend fun clearDeviceInformation() {

    }
}