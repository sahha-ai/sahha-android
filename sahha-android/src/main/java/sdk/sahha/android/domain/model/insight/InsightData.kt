package sdk.sahha.android.domain.model.insight

data class InsightData(
    val name: String,
    val value: Double,
    val unit: String,
    val startDateTime: String,
    val endDateTime: String
)