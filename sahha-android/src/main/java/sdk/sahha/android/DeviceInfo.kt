package sdk.sahha.android

import android.os.Build

object DeviceInfo {
  fun getPlatform(): String {
    return "Android"
  }

  fun getPlatformVer(): String {
    return "Platform version: ${Build.VERSION.SDK_INT}"
  }

  fun getDeviceModel(): String {
    return "Device model: ${Build.BRAND} ${Build.DEVICE}"
  }

  fun getAppVersion(): String {
    return "App version: null"
  }
}
