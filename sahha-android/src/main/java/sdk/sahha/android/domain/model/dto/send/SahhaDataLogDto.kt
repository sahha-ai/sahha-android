package sdk.sahha.android.domain.model.dto.send

import sdk.sahha.android.common.Constants
import sdk.sahha.android.domain.internal_enum.RecordingMethods
import sdk.sahha.android.domain.model.data_log.SahhaDataLog

// Created a Dto class; as editing the SahhaDataLog is very messy and requires local database migrations and correcting of the tables
internal data class SahhaDataLogDto(
    val id: String,
    val logType: String,
    val dataType: String,
    val value: Double,
    val source: String,
    val startDateTime: String,
    val endDateTime: String,
    val unit: String,
    val recordingMethod: String = RecordingMethods.UNKNOWN.name,
    val deviceId: String?,
    val deviceType: String = Constants.UNKNOWN,
    val additionalProperties: HashMap<String, String>? = null,
    val parentId: String? = null,
    val postDateTime: String? = null,
    val modifiedDateTime: String? = null
)

internal fun SahhaDataLog.toSahhaDataLogDto(): SahhaDataLogDto {
    return SahhaDataLogDto(
        id = id,
        logType = logType,
        dataType = dataType,
        value = value,
        source = source,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
        unit = unit,
        recordingMethod = recordingMethod,
        deviceId = deviceId,
        deviceType = deviceType,
        additionalProperties = additionalProperties,
        parentId = parentId,
        postDateTime = postDateTimes?.first(),
        modifiedDateTime = modifiedDateTime
    )
}
