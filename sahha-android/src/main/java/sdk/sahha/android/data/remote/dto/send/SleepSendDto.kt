package sdk.sahha.android.data.remote.dto.send

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import sdk.sahha.android.data.Constants

@Keep
data class SleepSendDto(
    val source: String,
    val durationInMinutes: Int,
    val sleepStage: String,
    val startDateTime: String,
    val endDateTime: String,
    val createdAt: String,
)
