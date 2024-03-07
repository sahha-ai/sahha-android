package sdk.sahha.android.domain.model.data_log

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import sdk.sahha.android.common.Constants
import sdk.sahha.android.domain.internal_enum.RecordingMethodsHealthConnect
import java.time.Instant
import java.time.ZonedDateTime

@Keep
@Entity
internal data class SahhaDataLog(
    @PrimaryKey val id: String,
    val logType: String,
    val dataType: String,
    val value: Double,
    val source: String,
    val startDateTime: String,
    val endDateTime: String,
    val unit: String,
    val recordingMethod: String = RecordingMethodsHealthConnect.RECORDING_METHOD_UNKNOWN.name,
    val deviceType: String = Constants.UNKNOWN,
    val additionalProperties: HashMap<String, String>? = null,
    val parentId: String? = null,
)
