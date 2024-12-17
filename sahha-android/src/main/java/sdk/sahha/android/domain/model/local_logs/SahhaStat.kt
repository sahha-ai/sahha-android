package sdk.sahha.android.domain.model.local_logs

import sdk.sahha.android.source.SahhaSensor
import java.time.ZonedDateTime

data class SahhaStat(
    val id: String,
    val type: String,
    val value: Double,
    val unit: String,
    val startDate: ZonedDateTime,
    val endDate: ZonedDateTime,
    val sources: List<String> = emptyList(),
)
