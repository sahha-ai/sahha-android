package sdk.sahha.android.domain.model.device_info

import android.os.Build
import androidx.room.Entity
import androidx.room.PrimaryKey
import sdk.sahha.android.BuildConfig
import sdk.sahha.android.common.Constants.PLATFORM_NAME
import sdk.sahha.android.domain.model.dto.send.DeviceInformationDto
import sdk.sahha.android.source.Sahha

@Entity
data class DeviceInformation(
    @PrimaryKey val id: Int = 1,
    val sdkId: String,
    val sdkVersion: String = BuildConfig.SDK_VERSION_NAME,
    val appId: String,
    val deviceType: String = Build.MANUFACTURER,
    val deviceModel: String = "${Build.DEVICE}:${Build.MODEL}",
    val system: String = PLATFORM_NAME,
    val systemVersion: String = "Android SDK: ${Build.VERSION.SDK_INT} (${Build.VERSION.RELEASE})",
    val timeZone: String = Sahha.di.timeManager.getTimezone()
)

fun DeviceInformation.toDeviceInformationSendDto(): DeviceInformationDto {
    return DeviceInformationDto(
        sdkId,
        sdkVersion,
        appId,
        deviceType,
        deviceModel,
        system,
        systemVersion,
        timeZone
    )
}
