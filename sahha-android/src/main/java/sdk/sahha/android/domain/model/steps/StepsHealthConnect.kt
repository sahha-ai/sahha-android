package sdk.sahha.android.domain.model.steps

import androidx.room.Entity
import androidx.room.PrimaryKey
import sdk.sahha.android.common.Constants
import sdk.sahha.android.domain.model.data_log.SahhaDataLog
import java.util.UUID

@Entity
internal data class StepsHealthConnect(
    @PrimaryKey val metaId: String,
    val dataType: String,
    val count: Int,
    val source: String,
    val startDateTime: String,
    val endDateTime: String,
    val modifiedDateTime: String,
    val recordingMethod: String,
    val deviceType: String,
    val deviceManufacturer: String,
    val deviceModel: String,
)