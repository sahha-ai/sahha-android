package sdk.sahha.android.controller

import android.os.Build

object SahhaDeviceInfo {
    fun getPlatform(): String {
        return "Android"
    }

    fun getPlatformVer(): String {
        return "${Build.VERSION.SDK_INT}"
    }

    fun getDeviceModel(): String {
        return "${Build.BRAND}:${Build.DEVICE}"
    }

    fun getSDKVersion(): String {
        return ""
    }
}
