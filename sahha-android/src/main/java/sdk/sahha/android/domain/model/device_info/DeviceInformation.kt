package sdk.sahha.android.domain.model.device_info

import android.os.Build
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import sdk.sahha.android.BuildConfig
import sdk.sahha.android.source.Sahha

@Entity
@Keep
data class DeviceInformation(
    @PrimaryKey val id: Int = 1,
    val sdkId: String = "android_kotlin",
    val sdkVersion: String = BuildConfig.SDK_VERSION_NAME,
    val appId: String = BuildConfig.LIBRARY_PACKAGE_NAME,
    val deviceType: String = Build.BRAND,
    val deviceModel: String = "${Build.DEVICE}:${Build.MODEL}",
    val system: String = "Android",
    val systemVersion: String = Build.VERSION.SDK_INT.toString(),
    val timezone: String = Sahha.di.timeManager.getTimezone()
)
