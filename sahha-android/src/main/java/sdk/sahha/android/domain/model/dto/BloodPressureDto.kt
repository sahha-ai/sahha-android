package sdk.sahha.android.domain.model.dto

import sdk.sahha.android.data.Constants

data class BloodPressureDto(
    val dataType: String,
    val recordingMethod: String,
    val count: Double,
    val source: String,
    val sourceDevice: String,
    val startDateTime: String,
    val endDateTime: String,
    val modifiedDateTime: String,
    val bodyPosition: String,
    val measurementLocation: String,
    val unit: String = Constants.HEALTH_CONNECT_UNIT_MMHG
)
