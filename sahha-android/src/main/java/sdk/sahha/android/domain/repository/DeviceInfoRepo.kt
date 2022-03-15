package sdk.sahha.android.domain.repository

interface DeviceInfoRepo {
    fun getPlatform(): String
    fun getPlatformVer(): String
    fun getDeviceModel(): String
    fun getSdkVersion(): String
}