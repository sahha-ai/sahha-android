package sdk.sahha.android.domain.repository

import sdk.sahha.android.domain.model.device_info.DeviceInformation

interface DeviceInfoRepo {
    fun getPlatform(): String
    fun getPlatformVer(): String
    fun getDeviceModel(): String
    fun getSdkVersion(): String
    suspend fun putDeviceInformation(
        deviceInformation: DeviceInformation,
        callback: ((error: String?, success: Boolean) -> Unit)? = null
    )
}