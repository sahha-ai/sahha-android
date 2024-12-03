package sdk.sahha.android.domain.model.device

import androidx.room.Entity
import androidx.room.PrimaryKey
import sdk.sahha.android.common.Constants
import sdk.sahha.android.domain.model.data_log.SahhaDataLog
import sdk.sahha.android.domain.model.metadata.HasMetadata
import sdk.sahha.android.domain.model.metadata.SahhaMetadata
import java.util.UUID


@Entity
internal data class PhoneUsage(
    val isLocked: Boolean,
    val isScreenOn: Boolean,
    val createdAt: String,
    override val metadata: SahhaMetadata? = null,
    @PrimaryKey val id: String = UUID.randomUUID().toString()
) : HasMetadata<PhoneUsage> {
    override fun copyWithMetadata(metadata: SahhaMetadata): PhoneUsage {
        return this.copy(metadata = metadata)
    }
}

internal fun PhoneUsage.toSahhaDataLogDto(): SahhaDataLog {
    return SahhaDataLog(
        id = id,
        logType = Constants.DataLogs.DEVICE,
        dataType = Constants.DataTypes.DEVICE_LOCK,
        source = Constants.PHONE_USAGE_DATA_SOURCE,
        value = if (isLocked) 1.0 else 0.0,
        unit = Constants.DataUnits.BOOLEAN,
        additionalProperties = hashMapOf(
            "isScreenOn" to if (isScreenOn) "1" else "0"
        ),
        startDateTime = createdAt,
        endDateTime = createdAt,
        metadata = metadata
    )
}
