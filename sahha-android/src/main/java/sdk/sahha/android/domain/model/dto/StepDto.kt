package sdk.sahha.android.domain.model.dto

import androidx.annotation.Keep
import sdk.sahha.android.common.Constants
import sdk.sahha.android.domain.internal_enum.RecordingMethods

@Keep
internal data class StepDto(
    val dataType: String,
    val value: Int,
    val unit: String = Constants.DataUnits.COUNT,
    val source: String,
    val startDateTime: String,
    val endDateTime: String,
    val recordingMethod: String = RecordingMethods.unknown.name,
    val deviceType: String = Constants.UNKNOWN,
    val modifiedDateTime: String,
    val deviceManufacturer: String? = null,
    val deviceModel: String? = null,
)
