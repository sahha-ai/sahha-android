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

internal fun StepsHealthConnect.toSahhaDataLogAsParentLog(): SahhaDataLog {
    return SahhaDataLog(
        id = metaId,
        logType = Constants.DataLogs.ACTIVITY,
        dataType = dataType,
        value = count.toDouble(),
        source = source,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
        unit = Constants.DataUnits.COUNT,
        recordingMethod = recordingMethod,
        deviceType = deviceType,
    )
}

internal fun StepsHealthConnect.toSahhaDataLogAsChildLog(): SahhaDataLog {
    return SahhaDataLog(
        id = UUID.randomUUID().toString(),
        logType = Constants.DataLogs.ACTIVITY,
        dataType = dataType,
        value = count.toDouble(),
        source = source,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
        unit = Constants.DataUnits.COUNT,
        recordingMethod = recordingMethod,
        deviceType = deviceType,
        parentId = metaId
    )
}