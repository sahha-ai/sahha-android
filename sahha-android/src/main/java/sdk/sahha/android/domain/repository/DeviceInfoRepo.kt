package sdk.sahha.android.domain.repository

import sdk.sahha.android.domain.model.device_info.DeviceInformation

interface DeviceInfoRepo {
    fun getPlatform(): String
    fun getPlatformVer(): String
    fun getDeviceModel(): String
    fun getSdkVersion(): String
    suspend fun getDeviceInformation(): DeviceInformation?
    suspend fun putDeviceInformation(
        token: String,
        deviceInformation: DeviceInformation,
        callback: (suspend (error: String?, success: Boolean) -> Unit)? = null
    )
}