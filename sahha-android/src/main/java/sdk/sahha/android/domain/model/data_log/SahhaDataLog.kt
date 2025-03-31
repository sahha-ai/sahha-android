package sdk.sahha.android.domain.model.data_log

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import sdk.sahha.android.common.Constants
import sdk.sahha.android.domain.internal_enum.RecordingMethods
import sdk.sahha.android.domain.model.metadata.HasMetadata

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
    val recordingMethod: String = RecordingMethods.unknown.name,
    val deviceId: String?,
    val deviceType: String = Constants.UNKNOWN,
    val additionalProperties: HashMap<String, Any>? = null,
    val parentId: String? = null,
    override val postDateTimes: ArrayList<String>? = null,
    override val modifiedDateTime: String? = null
) : HasMetadata<SahhaDataLog> {
    override fun copyWithMetadata(
        postDateTimes: ArrayList<String>?,
        modifiedDateTime: String?
    ): SahhaDataLog {
        return this.copy(
            postDateTimes = postDateTimes,
            modifiedDateTime = modifiedDateTime
        )
    }
}
