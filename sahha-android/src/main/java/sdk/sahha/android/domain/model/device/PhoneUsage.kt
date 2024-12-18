package sdk.sahha.android.domain.model.device

import androidx.room.Entity
import androidx.room.PrimaryKey
import sdk.sahha.android.common.Constants
import sdk.sahha.android.domain.model.data_log.SahhaDataLog
import sdk.sahha.android.domain.model.metadata.HasMetadata
import sdk.sahha.android.domain.model.metadata.SahhaMetadata
import sdk.sahha.android.source.SahhaSensor
import java.util.UUID


@Entity
internal data class PhoneUsage(
    val isLocked: Boolean,
    val isScreenOn: Boolean,
    val createdAt: String,
    override val postDateTimes: ArrayList<String>? = null,
    override val modifiedDateTime: String? = null,
    @PrimaryKey val id: String = UUID.nameUUIDFromBytes(
            (startTime.toEpochMilli() + endTime.toEpochMilli())
                .toString()
                .toByteArray()
        ).toString()
) : HasMetadata<PhoneUsage> {
    override fun copyWithMetadata(
        postDateTimes: ArrayList<String>?,
        modifiedDateTime: String?,
    ): PhoneUsage {
        return this.copy(
            postDateTimes = postDateTimes,
            modifiedDateTime = modifiedDateTime,
        )
    }
}

internal fun PhoneUsage.toSahhaDataLogDto(): SahhaDataLog {
    return SahhaDataLog(
        id = id,
        logType = Constants.DataLogs.DEVICE,
        dataType = SahhaSensor.device_lock.name,
        source = Constants.PHONE_USAGE_DATA_SOURCE,
        value = if (isLocked) 1.0 else 0.0,
        unit = Constants.DataUnits.BOOLEAN,
        additionalProperties = hashMapOf(
            "isScreenOn" to if (isScreenOn) "1" else "0"
        ),
        startDateTime = createdAt,
        endDateTime = createdAt,
        postDateTimes = postDateTimes,
        modifiedDateTime = modifiedDateTime
    )
}
