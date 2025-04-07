package sdk.sahha.android.domain.model.dto.send

import sdk.sahha.android.common.Constants
import sdk.sahha.android.domain.internal_enum.RecordingMethods
import sdk.sahha.android.domain.model.data_log.SahhaDataLog

private const val AGGREGATION_KEY = "aggregation"
private const val PERIODICITY_KEY = "periodicity"

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
    val recordingMethod: String = RecordingMethods.unknown.name,
    val deviceId: String?,
    val deviceType: String = Constants.UNKNOWN,
    val additionalProperties: Map<String, Any>? = null,
    val aggregation: String? = null,
    val periodicity: String? = null,
    val parentId: String? = null,
    val postDateTime: String? = null,
    val modifiedDateTime: String? = null
)

internal fun SahhaDataLog.toSahhaDataLogDto(
    dataType: String? = null
): SahhaDataLogDto {
    val addProps: MutableMap<String, String>? = additionalProperties

    // in case of other additional properties
    // remove aggregation from additional properties to add as its own param
    val aggregation = additionalProperties?.get(AGGREGATION_KEY)?.let { aggr ->
        addProps?.remove(AGGREGATION_KEY)
        return@let aggr
    }

    // remove periodicity from additional properties to add as its own param
    val periodicity = additionalProperties?.get(PERIODICITY_KEY)?.let { per ->
        addProps?.remove(PERIODICITY_KEY)
        return@let per
    }

    return SahhaDataLogDto(
        id = id,
        logType = logType,
        dataType = dataType ?: this.dataType,
        value = value,
        source = source,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
        unit = unit,
        recordingMethod = recordingMethod,
        deviceId = deviceId,
        deviceType = deviceType,
        additionalProperties = addProps, // use the additional properties with aggregation and periodicity removed
        aggregation = aggregation,
        periodicity = periodicity,
        parentId = parentId,
        postDateTime = postDateTimes?.first(),
        modifiedDateTime = modifiedDateTime
    )
}
