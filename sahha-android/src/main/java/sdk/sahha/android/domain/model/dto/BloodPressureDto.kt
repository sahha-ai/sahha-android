package sdk.sahha.android.domain.model.dto

import androidx.annotation.Keep
import sdk.sahha.android.data.Constants

@Keep
data class BloodPressureDto(
    val dataType: String,
    val recordingMethod: String,
    val count: Double,
    val source: String,
    val deviceType: String,
    val startDateTime: String,
    val endDateTime: String,
    val modifiedDateTime: String,
    val bodyPosition: String,
    val measurementLocation: String,
    val unit: String = Constants.HEALTH_CONNECT_UNIT_MMHG,
    val deviceManufacturer: String = Constants.UNKNOWN,
    val deviceModel: String = Constants.UNKNOWN
)
