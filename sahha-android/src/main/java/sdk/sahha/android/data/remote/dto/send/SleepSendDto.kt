package sdk.sahha.android.data.remote.dto.send

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
