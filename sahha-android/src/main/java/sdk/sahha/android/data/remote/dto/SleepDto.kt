package sdk.sahha.android.data.remote.dto

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SleepDto(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val durationInMinutes: Int,
    val sleepStage: String,
    val startDateTime: String,
    val endDateTime: String,
    val createdAt: String
) {
    constructor(
        durationInMinutes: Int,
        startDateTime: String,
        endDateTime: String,
        createdAt: String
    ) : this(
        0,
        durationInMinutes,
        "asleep",
        startDateTime,
        endDateTime,
        createdAt
    )
}
