package sdk.sahha.android.domain.model.steps

import androidx.room.Entity
import androidx.room.PrimaryKey
import sdk.sahha.android.common.Constants
import sdk.sahha.android.domain.model.dto.SahhaDataLogDto
import sdk.sahha.android.domain.model.dto.StepDto

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

internal fun StepsHealthConnect.toSahhaDataLogDto(): SahhaDataLogDto {
    return SahhaDataLogDto(
        id = metaId,
        logType = Constants.DataLogs.ACTIVITY,
        dataType = dataType,
        value = count.toDouble(),
        source = source,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
        unit = Constants.DataUnits.COUNT,
        recordingMethod = recordingMethod,
        deviceType = deviceType
    )
}