package sdk.sahha.android.data.repository

import android.os.Build
import sdk.sahha.android.domain.repository.DeviceInfoRepo

class DeviceInfoRepoImpl : DeviceInfoRepo {
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
}