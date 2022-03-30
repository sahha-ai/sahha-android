package sdk.sahha.android.data.remote.dto

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SleepDto(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val minutesSlept: Int,
    val startDateTime: String,
    val endDateTime: String,
    val createdAt: String
) {
    constructor(
        minutesSlept: Int,
        startDateTime: String,
        endDateTime: String,
        createdAt: String
    ) : this(
        0,
        minutesSlept,
        startDateTime,
        endDateTime,
        createdAt
    )
}
