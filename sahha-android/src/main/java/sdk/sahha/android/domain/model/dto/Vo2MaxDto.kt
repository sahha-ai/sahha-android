package sdk.sahha.android.domain.model.dto

import androidx.annotation.Keep
import sdk.sahha.android.common.Constants
import sdk.sahha.android.domain.internal_enum.RecordingMethodsHealthConnect

@Keep
data class Vo2MaxDto(
    val dataType: String,
    val value: Double,
    val source: String,
    val startDateTime: String,
    val endDateTime: String,
    val recordingMethod: String? = RecordingMethodsHealthConnect.RECORDING_METHOD_UNKNOWN.name,
    val unit: String? = null,
    val measurementMethod: String = Constants.UNKNOWN,
    val deviceType: String = Constants.UNKNOWN,
    val modifiedDateTime: String? = null,
    val deviceManufacturer: String = Constants.UNKNOWN,
    val deviceModel: String = Constants.UNKNOWN
)
