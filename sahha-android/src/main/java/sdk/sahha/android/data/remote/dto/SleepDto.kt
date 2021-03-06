package sdk.sahha.android.data.remote.dto

import androidx.room.Entity
import androidx.room.PrimaryKey
import sdk.sahha.android.data.Constants
import sdk.sahha.android.data.remote.dto.send.SleepSendDto

@Entity
data class SleepDto(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val source: String,
    val durationInMinutes: Int,
    val sleepStage: String,
    val startDateTime: String,
    val endDateTime: String,
    val createdAt: String,
) {
    constructor(
        durationInMinutes: Int,
        startDateTime: String,
        endDateTime: String,
        createdAt: String
    ) : this(
        0,
        Constants.SLEEP_DATA_SOURCE,
        durationInMinutes,
        "asleep",
        startDateTime,
        endDateTime,
        createdAt
    )
}

fun SleepDto.toSleepSendDto(): SleepSendDto {
    return SleepSendDto(
        source,
        durationInMinutes,
        sleepStage,
        startDateTime,
        endDateTime,
        createdAt,
    )
}
