package sdk.sahha.android.domain.model.sleep

import androidx.room.PrimaryKey
import sdk.sahha.android.data.Constants

data class SleepDataSilver(
    val startDateTime: String,
    val endDateTime: String,
    val source: String = Constants.SLEEP_DATA_SOURCE_HOURLY,
    val sleepStage: String = Constants.SLEEP_STAGE_ASLEEP,
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
)
