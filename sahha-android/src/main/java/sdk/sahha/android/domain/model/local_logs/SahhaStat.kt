package sdk.sahha.android.domain.model.local_logs

import androidx.annotation.Keep
import java.time.ZonedDateTime

@Keep
data class SahhaStat(
    val id: String,
    val type: String,
    val value: Double,
    val unit: String,
    val startDateTime: ZonedDateTime,
    val endDateTime: ZonedDateTime,
    val sources: List<String> = emptyList(),
)
