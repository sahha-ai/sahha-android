package sdk.sahha.android.domain.model.dto.send

import androidx.annotation.Keep

@Keep
internal data class DeviceInformationDto(
    val deviceId: String,
    val sdkId: String,
    val sdkVersion: String,
    val appId: String,
    val deviceType: String,
    val deviceModel: String,
    val system: String,
    val systemVersion: String,
    val timeZone: String,
    val appVersion: String?,
)
