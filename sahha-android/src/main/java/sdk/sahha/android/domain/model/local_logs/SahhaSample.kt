package sdk.sahha.android.domain.model.local_logs

data class SahhaSample(
    val id: String,
    val type: String,
    val value: Double,
    val unit: String,
    val startDateTime: String,
    val endDateTime: String,
    val source: String,
)
