package sdk.sahha.android.data.mapper

import androidx.health.connect.client.aggregate.AggregationResultGroupedByDuration
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import sdk.sahha.android.data.Constants
import sdk.sahha.android.domain.model.dto.BloodGlucoseDto
import sdk.sahha.android.domain.model.dto.BloodPressureDto
import sdk.sahha.android.domain.model.dto.HeartRateDto
import sdk.sahha.android.domain.model.dto.StepDto
import sdk.sahha.android.domain.model.dto.send.SleepSendDto
import sdk.sahha.android.domain.model.steps.StepsHealthConnect
import sdk.sahha.android.source.Sahha
import java.time.temporal.ChronoUnit

private val mapper = Sahha.di.healthConnectConstantsMapper
private val timeManager = Sahha.di.timeManager

fun StepsRecord.toStepsHealthConnect(): StepsHealthConnect {
    return StepsHealthConnect(
        metaId = metadata.id,
        dataType = Constants.HEALTH_CONNECT_STEP_DATA_TYPE,
        count = count.toInt(),
        source = metadata.dataOrigin.packageName,
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        startDateTime = timeManager.instantToIsoTime(startTime, startZoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, endZoneOffset),
        sourceDevice = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(
            metadata.lastModifiedTime,
            endZoneOffset
        ),
        deviceManufacturer = metadata.device?.manufacturer ?: Constants.UNKNOWN,
        deviceModel = metadata.device?.model ?: Constants.UNKNOWN
    )
}

fun SleepSessionRecord.toSleepSendDto(): SleepSendDto {
    return SleepSendDto(
        source = metadata.dataOrigin.packageName,
        durationInMinutes =
        (endTime.epochSecond - endTime.minus(
            startTime.epochSecond,
            ChronoUnit.SECONDS
        ).epochSecond).toInt(),
        startDateTime = timeManager.instantToIsoTime(startTime, startZoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, endZoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        sourceDevice = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(
            metadata.lastModifiedTime,
            endZoneOffset
        )
    )
}

fun BloodGlucoseRecord.toBloodGlucoseDto(): BloodGlucoseDto {
    return BloodGlucoseDto(
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        count = level.inMillimolesPerLiter,
        source = metadata.dataOrigin.packageName,
        sourceDevice = mapper.devices(metadata.device?.type),
        startDateTime = timeManager.instantToIsoTime(
            time, zoneOffset
        ),
        endDateTime = timeManager.instantToIsoTime(
            time, zoneOffset
        ),
        modifiedDateTime = timeManager.instantToIsoTime(
            metadata.lastModifiedTime, zoneOffset
        ),
        mealType = mapper.mealType(mealType) ?: Constants.UNKNOWN,
        relationToMeal = mapper.relationToMeal(relationToMeal) ?: Constants.UNKNOWN,
        specimenSource = mapper.specimenSource(specimenSource) ?: Constants.UNKNOWN
    )
}

fun BloodPressureRecord.toBloodPressureDiastolicDto(): BloodPressureDto {
    return BloodPressureDto(
        dataType = Constants.HEALTH_CONNECT_BLOOD_PRESSURE_DIASTOLIC,
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        count = diastolic.inMillimetersOfMercury,
        source = metadata.dataOrigin.packageName,
        sourceDevice = mapper.devices(metadata.device?.type),
        startDateTime = timeManager.instantToIsoTime(
            time, zoneOffset
        ),
        endDateTime = timeManager.instantToIsoTime(
            time, zoneOffset
        ),
        modifiedDateTime = timeManager.instantToIsoTime(
            metadata.lastModifiedTime, zoneOffset
        ),
        bodyPosition = mapper.bodyPosition(bodyPosition) ?: Constants.UNKNOWN,
        measurementLocation = mapper.measurementLocation(measurementLocation) ?: Constants.UNKNOWN,
    )
}

fun BloodPressureRecord.toBloodPressureSystolicDto(): BloodPressureDto {
    return BloodPressureDto(
        dataType = Constants.HEALTH_CONNECT_BLOOD_PRESSURE_SYSTOLIC,
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        count = systolic.inMillimetersOfMercury,
        source = metadata.dataOrigin.packageName,
        sourceDevice = mapper.devices(metadata.device?.type),
        startDateTime = timeManager.instantToIsoTime(
            time, zoneOffset
        ),
        endDateTime = timeManager.instantToIsoTime(
            time, zoneOffset
        ),
        modifiedDateTime = timeManager.instantToIsoTime(
            metadata.lastModifiedTime, zoneOffset
        ),
        bodyPosition = mapper.bodyPosition(bodyPosition) ?: Constants.UNKNOWN,
        measurementLocation = mapper.measurementLocation(measurementLocation) ?: Constants.UNKNOWN,
    )
}

fun AggregationResultGroupedByDuration.toHeartRateAvgDto(): HeartRateDto {
    return HeartRateDto(
        dataType = Constants.HEALTH_CONNECT_HEART_RATE_AVG,
        count = result[HeartRateRecord.BPM_AVG] ?: -1,
        source = result.dataOrigins.map {
            it.packageName
        }.toString(),
        startDateTime = timeManager.instantToIsoTime(startTime, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, zoneOffset),
    )
}

fun AggregationResultGroupedByDuration.toHeartRateMinDto(): HeartRateDto {
    return HeartRateDto(
        dataType = Constants.HEALTH_CONNECT_HEART_RATE_MIN,
        count = result[HeartRateRecord.BPM_MIN] ?: -1,
        source = result.dataOrigins.map {
            it.packageName
        }.toString(),
        startDateTime = timeManager.instantToIsoTime(startTime, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, zoneOffset),
    )
}

fun AggregationResultGroupedByDuration.toHeartRateMaxDto(): HeartRateDto {
    return HeartRateDto(
        dataType = Constants.HEALTH_CONNECT_HEART_RATE_MAX,
        count = result[HeartRateRecord.BPM_MAX] ?: -1,
        source = result.dataOrigins.map {
            it.packageName
        }.toString(),
        startDateTime = timeManager.instantToIsoTime(startTime, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, zoneOffset),
    )
}

fun AggregationResultGroupedByDuration.toRestingHeartRateAvgDto(): HeartRateDto {
    return HeartRateDto(
        dataType = Constants.HEALTH_CONNECT_RESTING_HEART_RATE_AVG,
        count = result[RestingHeartRateRecord.BPM_AVG] ?: -1,
        source = result.dataOrigins.map {
            it.packageName
        }.toString(),
        startDateTime = timeManager.instantToIsoTime(startTime, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, zoneOffset),
    )
}

fun AggregationResultGroupedByDuration.toRestingHeartRateMinDto(): HeartRateDto {
    return HeartRateDto(
        dataType = Constants.HEALTH_CONNECT_RESTING_HEART_RATE_MIN,
        count = result[RestingHeartRateRecord.BPM_MIN] ?: -1,
        source = result.dataOrigins.map {
            it.packageName
        }.toString(),
        startDateTime = timeManager.instantToIsoTime(startTime, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, zoneOffset),
    )
}

fun AggregationResultGroupedByDuration.toRestingHeartRateMaxDto(): HeartRateDto {
    return HeartRateDto(
        dataType = Constants.HEALTH_CONNECT_RESTING_HEART_RATE_MAX,
        count = result[RestingHeartRateRecord.BPM_MAX] ?: -1,
        source = result.dataOrigins.map {
            it.packageName
        }.toString(),
        startDateTime = timeManager.instantToIsoTime(startTime, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, zoneOffset),
    )
}

fun HeartRateVariabilityRmssdRecord.toHeartRateDto(): HeartRateDto {
    return HeartRateDto(
        dataType = Constants.HEALTH_CONNECT_HEART_RATE_VARIABILITY_RMSSD,
        count = this.heartRateVariabilityMillis.toLong(),
        unit = Constants.HEALTH_CONNECT_UNIT_MILLISECONDS,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        sourceDevice = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset)
    )
}