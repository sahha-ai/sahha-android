package sdk.sahha.android.domain.model.dto

import androidx.annotation.Keep

@Keep
data class HeartRateDto(
    val dataType: String,
    val count: Long,
    val source: String,
    val startDateTime: String,
    val endDateTime: String,
    val recordingMethod: String? = null,
    val unit: String? = null,
    val sourceDevice: String? = null,
    val modifiedDateTime: String? = null,
    val deviceManufacturer: String? = null,
    val deviceModel: String? = null
)
