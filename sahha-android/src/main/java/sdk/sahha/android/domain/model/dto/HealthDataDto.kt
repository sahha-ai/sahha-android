package sdk.sahha.android.domain.model.dto

import sdk.sahha.android.common.Constants
import sdk.sahha.android.domain.internal_enum.RecordingMethodsHealthConnect


data class HealthDataDto(
    val dataType: String,
    val value: Double,
    val source: String,
    val startDateTime: String,
    val endDateTime: String,
    val recordingMethod: String? = RecordingMethodsHealthConnect.RECORDING_METHOD_UNKNOWN.name,
    val unit: String? = null,
    val deviceType: String = Constants.UNKNOWN,
    val modifiedDateTime: String? = null,
    val deviceManufacturer: String = Constants.UNKNOWN,
    val deviceModel: String = Constants.UNKNOWN
)
