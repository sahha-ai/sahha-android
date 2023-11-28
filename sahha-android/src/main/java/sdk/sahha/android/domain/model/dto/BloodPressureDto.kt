package sdk.sahha.android.domain.model.dto

import androidx.annotation.Keep
import sdk.sahha.android.common.Constants

@Keep
data class BloodPressureDto(
    val dataType: String,
    val recordingMethod: String,
    val value: Double,
    val source: String,
    val deviceType: String,
    val startDateTime: String,
    val endDateTime: String,
    val modifiedDateTime: String,
    val bodyPosition: String,
    val measurementLocation: String,
    val unit: String = Constants.DataUnits.MMHG,
    val deviceManufacturer: String = Constants.UNKNOWN,
    val deviceModel: String = Constants.UNKNOWN
)
