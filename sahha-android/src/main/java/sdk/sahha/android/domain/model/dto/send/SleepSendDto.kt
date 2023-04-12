package sdk.sahha.android.domain.model.dto.send

import androidx.annotation.Keep

@Keep
data class SleepSendDto(
    val source: String,
    val durationInMinutes: Int,
    val sleepStage: String,
    val startDateTime: String,
    val endDateTime: String,
    val createdAt: String,
)
