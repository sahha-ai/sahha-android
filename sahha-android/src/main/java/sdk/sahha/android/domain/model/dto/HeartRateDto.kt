package sdk.sahha.android.domain.model.dto

data class HeartRateDto(
    val dataType: String,
    val count: Long,
    val source: List<String>,
    val startDateTime: String,
    val endDateTime: String,
    val recordingMethod: String? = null,
    val unit: String? = null,
    val sourceDevice: String? = null,
    val modifiedDateTime: String? = null,
)
