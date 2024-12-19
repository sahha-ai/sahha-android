package sdk.sahha.android.domain.model.local_logs

import java.time.ZonedDateTime

data class SahhaSample(
    val id: String,
    val type: String,
    val value: Double,
    val unit: String,
    val startDateTime: ZonedDateTime,
    val endDateTime: ZonedDateTime,
    val source: String,
)
