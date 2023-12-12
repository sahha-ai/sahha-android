package sdk.sahha.android.domain.model.device

import androidx.room.Entity
import androidx.room.PrimaryKey
import sdk.sahha.android.common.Constants
import sdk.sahha.android.domain.model.dto.SahhaDataLogDto


@Entity
data class PhoneUsage(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val isLocked: Boolean,
    val isScreenOn: Boolean,
    val createdAt: String
) {
    constructor(isLocked: Boolean, isScreenOn: Boolean, createdAt: String)
            : this(0, isLocked, isScreenOn, createdAt)
}

fun PhoneUsage.toSahhaDataLogDto(): SahhaDataLogDto {
    return SahhaDataLogDto(
        logType = Constants.DataLogs.DEVICE,
        dataType = Constants.DataTypes.DEVICE_LOCK,
        source = Constants.PHONE_USAGE_DATA_SOURCE,
        value = if (isLocked) 1.0 else 0.0,
        unit = Constants.DataUnits.BOOLEAN,
        additionalProperties = hashMapOf(
            "isScreenOn" to if (isScreenOn) 1.0 else 0.0
        ),
        startDateTime = createdAt,
        endDateTime = createdAt
    )
}
