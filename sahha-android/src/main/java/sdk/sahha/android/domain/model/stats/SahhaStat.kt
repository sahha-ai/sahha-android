package sdk.sahha.android.domain.model.stats

import sdk.sahha.android.source.SahhaSensor
import java.time.ZonedDateTime

data class SahhaStat(
    val id: String,
    val sensor: SahhaSensor,
    val value: Long,
    val unit: String,
    val startDate: ZonedDateTime,
    val endDate: ZonedDateTime,
    val sources: List<String>? = null,
)
