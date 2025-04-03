package sdk.sahha.android.data.mapper

import android.content.Context
import androidx.health.connect.client.aggregate.AggregationResultGroupedByDuration
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.BasalBodyTemperatureRecord
import androidx.health.connect.client.records.BasalMetabolicRateRecord
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.BodyTemperatureRecord
import androidx.health.connect.client.records.BodyWaterMassRecord
import androidx.health.connect.client.records.BoneMassRecord
import androidx.health.connect.client.records.ExerciseLap
import androidx.health.connect.client.records.ExerciseSegment
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.FloorsClimbedRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.HeightRecord
import androidx.health.connect.client.records.LeanBodyMassRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.RespiratoryRateRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.Vo2MaxRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.metadata.Device
import sdk.sahha.android.common.Constants
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.domain.internal_enum.RecordingMethods
import sdk.sahha.android.domain.manager.IdManager
import sdk.sahha.android.domain.mapper.HealthConnectConstantsMapper
import sdk.sahha.android.domain.model.app_event.AppEvent
import sdk.sahha.android.domain.model.data_log.SahhaDataLog
import sdk.sahha.android.domain.model.device.PhoneUsage
import sdk.sahha.android.domain.model.dto.SleepDto
import sdk.sahha.android.domain.model.local_logs.SahhaSample
import sdk.sahha.android.domain.model.local_logs.SahhaStat
import sdk.sahha.android.domain.model.steps.StepData
import sdk.sahha.android.domain.model.steps.StepSession
import sdk.sahha.android.domain.model.steps.StepsHealthConnect
import sdk.sahha.android.domain.model.steps.getDataType
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaBiomarkerCategory
import sdk.sahha.android.source.SahhaSensor
import java.time.ZonedDateTime
import java.util.UUID

private val defaults = Sahha.di.mapperDefaults

// Converted to SahhaDataLogDto later
// Parameters are for unit tests
internal fun StepsRecord.toStepsHealthConnect(
    mapper: HealthConnectConstantsMapper = defaults.mapper,
    timeManager: SahhaTimeManager = defaults.timeManager
): StepsHealthConnect {
    return StepsHealthConnect(
        metaId = metadata.id,
        dataType = SahhaSensor.steps.name,
        count = count.toInt(),
        source = metadata.dataOrigin.packageName,
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        startDateTime = timeManager.instantToIsoTime(startTime, startZoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, endZoneOffset),
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(
            metadata.lastModifiedTime,
            endZoneOffset
        ),
        deviceManufacturer = metadata.device?.manufacturer ?: Constants.UNKNOWN,
        deviceModel = metadata.device?.model ?: Constants.UNKNOWN
    )
}

internal fun StepsHealthConnect.toSahhaDataLogAsParentLog(
    idManager: IdManager = defaults.idManager
): SahhaDataLog {
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
        deviceId = idManager.getDeviceId(),
        deviceType = deviceType,
        modifiedDateTime = modifiedDateTime
    )
}

internal fun StepsHealthConnect.toSahhaDataLogAsChildLog(
    idManager: IdManager = defaults.idManager
): SahhaDataLog {
    return SahhaDataLog(
        id = UUID.nameUUIDFromBytes(
            (dataType + startDateTime + endDateTime)
                .toByteArray()
        ).toString(),
        logType = Constants.DataLogs.ACTIVITY,
        dataType = dataType,
        value = count.toDouble(),
        source = source,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
        unit = Constants.DataUnits.COUNT,
        recordingMethod = recordingMethod,
        deviceId = idManager.getDeviceId(),
        deviceType = deviceType,
        modifiedDateTime = modifiedDateTime,
        parentId = metaId
    )
}

internal fun SleepSessionRecord.toSahhaDataLogDto(
    mapper: HealthConnectConstantsMapper = defaults.mapper,
    timeManager: SahhaTimeManager = defaults.timeManager,
    idManager: IdManager = defaults.idManager
): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.SLEEP,
        dataType = Constants.SLEEP_STAGE_IN_BED,
        source = metadata.dataOrigin.packageName,
        value = ((endTime.toEpochMilli() - startTime.toEpochMilli()).toDouble() / 1000 / 60),
        unit = Constants.DataUnits.MINUTE,
        startDateTime = timeManager.instantToIsoTime(startTime, startZoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, endZoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceId = idManager.getDeviceId(),
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, endZoneOffset)
    )
}

internal fun SleepSessionRecord.Stage.toSahhaDataLog(
    session: SleepSessionRecord,
    mapper: HealthConnectConstantsMapper = defaults.mapper,
    timeManager: SahhaTimeManager = defaults.timeManager,
    idManager: IdManager = defaults.idManager
): SahhaDataLog {
    val durationInMinutes =
        ((endTime.toEpochMilli() - startTime.toEpochMilli()).toDouble() / 1000 / 60)
    return SahhaDataLog(
        id = UUID.nameUUIDFromBytes(
            (session.metadata.id + startTime + endTime)
                .toByteArray()
        ).toString(),
        parentId = session.metadata.id,
        logType = Constants.DataLogs.SLEEP,
        value = durationInMinutes,
        unit = Constants.DataUnits.MINUTE,
        source = session.metadata.dataOrigin.packageName,
        dataType = (mapper.sleepStages(stage)
            ?: Constants.SLEEP_STAGE_UNKNOWN),
        startDateTime = timeManager.instantToIsoTime(
            startTime, session.startZoneOffset
        ),
        endDateTime = timeManager.instantToIsoTime(
            endTime, session.endZoneOffset
        ),
        recordingMethod = mapper.recordingMethod(session.metadata.recordingMethod),
        deviceId = idManager.getDeviceId(),
        deviceType = mapper.devices(session.metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(
            session.metadata.lastModifiedTime,
            session.endZoneOffset
        )
    )
}

internal fun BloodGlucoseRecord.toSahhaDataLogDto(
    mapper: HealthConnectConstantsMapper = defaults.mapper,
    timeManager: SahhaTimeManager = defaults.timeManager,
    idManager: IdManager = defaults.idManager
): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.BLOOD,
        dataType = SahhaSensor.blood_glucose.name,
        value = level.inMillimolesPerLiter,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(
            time, zoneOffset
        ),
        endDateTime = timeManager.instantToIsoTime(
            time, zoneOffset
        ),
        unit = Constants.DataUnits.MMOL_PER_LITRE,
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceId = idManager.getDeviceId(),
        deviceType = mapper.devices(metadata.device?.type),
        additionalProperties = hashMapOf(
            "relationToMeal" to (mapper.relationToMeal(relationToMeal) ?: Constants.UNKNOWN),
            "mealType" to (mapper.mealType(mealType) ?: Constants.UNKNOWN),
            "specimenSource" to (mapper.specimenSource(specimenSource) ?: Constants.UNKNOWN)
        ),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset)
    )
}

internal fun BloodPressureRecord.toBloodPressureDiastolic(
    mapper: HealthConnectConstantsMapper = defaults.mapper,
    timeManager: SahhaTimeManager = defaults.timeManager,
    idManager: IdManager = defaults.idManager
): SahhaDataLog {
    return SahhaDataLog(
        id = UUID.nameUUIDFromBytes(
            (metadata.id + time)
                .toByteArray()
        ).toString(),
        logType = Constants.DataLogs.BLOOD,
        dataType = SahhaSensor.blood_pressure_diastolic.name,
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        value = diastolic.inMillimetersOfMercury,
        unit = Constants.DataUnits.MMHG,
        source = metadata.dataOrigin.packageName,
        deviceId = idManager.getDeviceId(),
        deviceType = mapper.devices(metadata.device?.type),
        startDateTime = timeManager.instantToIsoTime(
            time, zoneOffset
        ),
        endDateTime = timeManager.instantToIsoTime(
            time, zoneOffset
        ),
        additionalProperties = hashMapOf(
            "bodyPosition" to (mapper.bodyPosition(bodyPosition) ?: Constants.UNKNOWN),
            "measurementLocation" to (mapper.measurementLocation(measurementLocation)
                ?: Constants.UNKNOWN),
        ),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset),
        parentId = metadata.id
    )
}

internal fun BloodPressureRecord.toBloodPressureSystolic(
    mapper: HealthConnectConstantsMapper = defaults.mapper,
    timeManager: SahhaTimeManager = defaults.timeManager,
    idManager: IdManager = defaults.idManager
): SahhaDataLog {
    return SahhaDataLog(
        id = UUID.nameUUIDFromBytes(
            (metadata.id + time)
                .toByteArray()
        ).toString(),
        logType = Constants.DataLogs.BLOOD,
        dataType = SahhaSensor.blood_pressure_systolic.name,
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        value = systolic.inMillimetersOfMercury,
        unit = Constants.DataUnits.MMHG,
        source = metadata.dataOrigin.packageName,
        deviceId = idManager.getDeviceId(),
        deviceType = mapper.devices(metadata.device?.type),
        startDateTime = timeManager.instantToIsoTime(
            time, zoneOffset
        ),
        endDateTime = timeManager.instantToIsoTime(
            time, zoneOffset
        ),
        additionalProperties = hashMapOf(
            "bodyPosition" to (mapper.bodyPosition(bodyPosition) ?: Constants.UNKNOWN),
            "measurementLocation" to (mapper.measurementLocation(measurementLocation)
                ?: Constants.UNKNOWN)
        ),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset),
        parentId = metadata.id
    )
}

internal fun HeartRateRecord.Sample.toSahhaDataLog(
    record: HeartRateRecord,
    mapper: HealthConnectConstantsMapper = defaults.mapper,
    timeManager: SahhaTimeManager = defaults.timeManager,
    idManager: IdManager = defaults.idManager
): SahhaDataLog {
    return SahhaDataLog(
        id = UUID.nameUUIDFromBytes(
            (record.metadata.id + time)
                .toByteArray()
        ).toString(),
        parentId = record.metadata.id,
        logType = Constants.DataLogs.HEART,
        dataType = SahhaSensor.heart_rate.name,
        value = beatsPerMinute.toDouble(),
        unit = Constants.DataUnits.BEAT_PER_MIN,
        source = record.metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(
            time, record.startZoneOffset
        ),
        endDateTime = timeManager.instantToIsoTime(
            time, record.endZoneOffset
        ),
        recordingMethod = mapper.recordingMethod(record.metadata.recordingMethod),
        deviceId = idManager.getDeviceId(),
        deviceType = mapper.devices(record.metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(
            record.metadata.lastModifiedTime,
            record.endZoneOffset
        )
    )
}

internal fun RestingHeartRateRecord.toSahhaLogDto(
    mapper: HealthConnectConstantsMapper = defaults.mapper,
    timeManager: SahhaTimeManager = defaults.timeManager,
    idManager: IdManager = defaults.idManager
): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.HEART,
        dataType = SahhaSensor.resting_heart_rate.name,
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        value = beatsPerMinute.toDouble(),
        unit = Constants.DataUnits.BEAT_PER_MIN,
        source = metadata.dataOrigin.packageName,
        deviceId = idManager.getDeviceId(),
        deviceType = mapper.devices(metadata.device?.type),
        startDateTime = timeManager.instantToIsoTime(
            time, zoneOffset
        ),
        endDateTime = timeManager.instantToIsoTime(
            time, zoneOffset
        ),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset)
    )
}

internal fun AggregationResultGroupedByDuration.toSahhaStat(
    category: SahhaBiomarkerCategory,
    sensor: SahhaSensor,
    value: Double,
    unit: String,
    sources: List<String> = listOf()
): SahhaStat {
    val consistentUid = UUID.nameUUIDFromBytes(
        ("Aggregate${sensor.name}" + startTime + endTime).toByteArray()
    )
    return SahhaStat(
        id = consistentUid.toString(),
        category = category.name,
        type = sensor.name,
        value = value,
        unit = unit,
        startDateTime = ZonedDateTime.ofInstant(this.startTime, this.zoneOffset),
        endDateTime = ZonedDateTime.ofInstant(this.endTime, this.zoneOffset),
        sources = sources
    )
}

internal fun HeartRateVariabilityRmssdRecord.toSahhaDataLogDto(
    mapper: HealthConnectConstantsMapper = defaults.mapper,
    timeManager: SahhaTimeManager = defaults.timeManager,
    idManager: IdManager = defaults.idManager
): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.HEART,
        dataType = SahhaSensor.heart_rate_variability_rmssd.name,
        value = this.heartRateVariabilityMillis,
        unit = Constants.DataUnits.MILLISECOND,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceId = idManager.getDeviceId(),
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset)
    )
}

internal fun ActiveCaloriesBurnedRecord.toSahhaDataLogDto(
    mapper: HealthConnectConstantsMapper = defaults.mapper,
    timeManager: SahhaTimeManager = defaults.timeManager,
    idManager: IdManager = defaults.idManager
): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.ENERGY,
        dataType = SahhaSensor.active_energy_burned.name,
        value = energy.inKilocalories,
        unit = Constants.DataUnits.KILOCALORIE,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(startTime, startZoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, endZoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceId = idManager.getDeviceId(),
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, endZoneOffset)
    )
}

internal fun TotalCaloriesBurnedRecord.toSahhaDataLogDto(
    mapper: HealthConnectConstantsMapper = defaults.mapper,
    timeManager: SahhaTimeManager = defaults.timeManager,
    idManager: IdManager = defaults.idManager
): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.ENERGY,
        dataType = SahhaSensor.total_energy_burned.name,
        value = energy.inKilocalories,
        unit = Constants.DataUnits.KILOCALORIE,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(startTime, startZoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, endZoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceId = idManager.getDeviceId(),
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, endZoneOffset)
    )
}

internal fun OxygenSaturationRecord.toSahhaDataLogDto(
    mapper: HealthConnectConstantsMapper = defaults.mapper,
    timeManager: SahhaTimeManager = defaults.timeManager,
    idManager: IdManager = defaults.idManager
): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.OXYGEN,
        dataType = SahhaSensor.oxygen_saturation.name,
        value = percentage.value.toDecimalPercentage(),
        unit = Constants.DataUnits.PERCENTAGE,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceId = idManager.getDeviceId(),
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset)
    )
}

internal fun BasalMetabolicRateRecord.toSahhaDataLogDto(
    mapper: HealthConnectConstantsMapper = defaults.mapper,
    timeManager: SahhaTimeManager = defaults.timeManager,
    idManager: IdManager = defaults.idManager
): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.ENERGY,
        dataType = SahhaSensor.basal_metabolic_rate.name,
        value = this.basalMetabolicRate.inKilocaloriesPerDay,
        unit = Constants.DataUnits.KCAL_PER_DAY,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceId = idManager.getDeviceId(),
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset)
    )
}

internal fun BodyFatRecord.toSahhaDataLogDto(
    mapper: HealthConnectConstantsMapper = defaults.mapper,
    timeManager: SahhaTimeManager = defaults.timeManager,
    idManager: IdManager = defaults.idManager
): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.BODY,
        dataType = SahhaSensor.body_fat.name,
        value = percentage.value.toDecimalPercentage(),
        unit = Constants.DataUnits.PERCENTAGE,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceId = idManager.getDeviceId(),
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset)
    )
}

internal fun BodyWaterMassRecord.toSahhaDataLogDto(
    mapper: HealthConnectConstantsMapper = defaults.mapper,
    timeManager: SahhaTimeManager = defaults.timeManager,
    idManager: IdManager = defaults.idManager
): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.BODY,
        dataType = SahhaSensor.body_water_mass.name,
        value = mass.inKilograms,
        unit = Constants.DataUnits.KILOGRAM,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceId = idManager.getDeviceId(),
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset)
    )
}

internal fun LeanBodyMassRecord.toSahhaDataLogDto(
    mapper: HealthConnectConstantsMapper = defaults.mapper,
    timeManager: SahhaTimeManager = defaults.timeManager,
    idManager: IdManager = defaults.idManager
): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.BODY,
        dataType = SahhaSensor.lean_body_mass.name,
        value = mass.inKilograms,
        unit = Constants.DataUnits.KILOGRAM,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceId = idManager.getDeviceId(),
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset)
    )
}

internal fun BoneMassRecord.toSahhaDataLogDto(
    mapper: HealthConnectConstantsMapper = defaults.mapper,
    timeManager: SahhaTimeManager = defaults.timeManager,
    idManager: IdManager = defaults.idManager
): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.BODY,
        dataType = SahhaSensor.bone_mass.name,
        value = mass.inKilograms,
        unit = Constants.DataUnits.KILOGRAM,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceId = idManager.getDeviceId(),
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset)
    )
}

internal fun HeightRecord.toSahhaDataLogDto(
    mapper: HealthConnectConstantsMapper = defaults.mapper,
    timeManager: SahhaTimeManager = defaults.timeManager,
    idManager: IdManager = defaults.idManager
): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.BODY,
        dataType = SahhaSensor.height.name,
        value = height.inMeters,
        unit = Constants.DataUnits.METRE,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceId = idManager.getDeviceId(),
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset)
    )
}

internal fun WeightRecord.toSahhaDataLogDto(
    mapper: HealthConnectConstantsMapper = defaults.mapper,
    timeManager: SahhaTimeManager = defaults.timeManager,
    idManager: IdManager = defaults.idManager
): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.BODY,
        dataType = SahhaSensor.weight.name,
        value = weight.inKilograms,
        unit = Constants.DataUnits.KILOGRAM,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceId = idManager.getDeviceId(),
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset)
    )
}

internal fun Vo2MaxRecord.toSahhaDataLogDto(
    mapper: HealthConnectConstantsMapper = defaults.mapper,
    timeManager: SahhaTimeManager = defaults.timeManager,
    idManager: IdManager = defaults.idManager
): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.OXYGEN,
        dataType = SahhaSensor.vo2_max.name,
        value = vo2MillilitersPerMinuteKilogram,
        unit = Constants.DataUnits.ML_PER_KG_PER_MIN,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceId = idManager.getDeviceId(),
        deviceType = mapper.devices(metadata.device?.type),
        additionalProperties = hashMapOf(
            "measurementMethod" to (mapper.measurementMethod(measurementMethod)
                ?: Constants.UNKNOWN),
        ),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset)
    )
}

internal fun RespiratoryRateRecord.toSahhaDataLogDto(
    mapper: HealthConnectConstantsMapper = defaults.mapper,
    timeManager: SahhaTimeManager = defaults.timeManager,
    idManager: IdManager = defaults.idManager
): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.OXYGEN,
        dataType = SahhaSensor.respiratory_rate.name,
        value = rate,
        unit = Constants.DataUnits.BREATH_PER_MIN,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceId = idManager.getDeviceId(),
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset)
    )
}

internal fun FloorsClimbedRecord.toSahhaDataLogDto(
    mapper: HealthConnectConstantsMapper = defaults.mapper,
    timeManager: SahhaTimeManager = defaults.timeManager,
    idManager: IdManager = defaults.idManager
): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.ACTIVITY,
        dataType = SahhaSensor.floors_climbed.name,
        value = floors,
        unit = Constants.DataUnits.COUNT,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(startTime, startZoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, endZoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceId = idManager.getDeviceId(),
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, endZoneOffset)
    )
}

internal fun BodyTemperatureRecord.toSahhaDataLogDto(
    mapper: HealthConnectConstantsMapper = defaults.mapper,
    timeManager: SahhaTimeManager = defaults.timeManager,
    idManager: IdManager = defaults.idManager
): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.TEMPERATURE,
        dataType = SahhaSensor.body_temperature.name,
        value = temperature.inCelsius,
        unit = Constants.DataUnits.CELSIUS,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceId = idManager.getDeviceId(),
        deviceType = mapper.devices(metadata.device?.type),
        additionalProperties = hashMapOf(
            "measurementLocation" to (mapper.bodyTempMeasurementLocation(measurementLocation)
                ?: Constants.UNKNOWN)
        ),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset)
    )
}

internal fun BasalBodyTemperatureRecord.toSahhaDataLogDto(
    mapper: HealthConnectConstantsMapper = defaults.mapper,
    timeManager: SahhaTimeManager = defaults.timeManager,
    idManager: IdManager = defaults.idManager
): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.TEMPERATURE,
        dataType = SahhaSensor.basal_body_temperature.name,
        value = temperature.inCelsius,
        unit = Constants.DataUnits.CELSIUS,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceId = idManager.getDeviceId(),
        deviceType = mapper.devices(metadata.device?.type),
        additionalProperties = hashMapOf(
            "measurementLocation" to (mapper.bodyTempMeasurementLocation(measurementLocation)
                ?: Constants.UNKNOWN)
        ),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset)
    )
}

internal fun ExerciseSessionRecord.toSahhaDataLogDto(
    mapper: HealthConnectConstantsMapper = defaults.mapper,
    timeManager: SahhaTimeManager = defaults.timeManager,
    idManager: IdManager = defaults.idManager
): SahhaDataLog {
    val exerciseType = (mapper.exerciseTypes(exerciseType) ?: Constants.UNKNOWN)
    val source = metadata.dataOrigin.packageName
    val startZoneOffset = this.startZoneOffset
    val endZoneOffset = this.endZoneOffset
    val recordingMethod = mapper.recordingMethod(metadata.recordingMethod)
    val deviceType = mapper.devices(metadata.device?.type)

    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.EXERCISE,
        dataType = "exercise_session_$exerciseType",
        value = ((endTime.toEpochMilli() - startTime.toEpochMilli()).toDouble() / 1000 / 60),
        unit = Constants.DataUnits.MINUTE,
        source = source,
        startDateTime = timeManager.instantToIsoTime(startTime, startZoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, endZoneOffset),
        recordingMethod = recordingMethod,
        deviceId = idManager.getDeviceId(),
        deviceType = deviceType,
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, endZoneOffset)
    )
}

internal fun ExerciseLap.toSahhaDataLogDto(
    exercise: ExerciseSessionRecord,
    mapper: HealthConnectConstantsMapper = defaults.mapper,
    timeManager: SahhaTimeManager = defaults.timeManager,
    idManager: IdManager = defaults.idManager
): SahhaDataLog {
    val source = exercise.metadata.dataOrigin.packageName
    val startZoneOffset = exercise.startZoneOffset
    val endZoneOffset = exercise.endZoneOffset
    val recordingMethod = mapper.recordingMethod(exercise.metadata.recordingMethod)
    val deviceType = mapper.devices(exercise.metadata.device?.type)

    val dataType = "exercise_lap"
    return SahhaDataLog(
        id = UUID.nameUUIDFromBytes(
            (exercise.metadata.id + startTime + endTime)
                .toByteArray()
        ).toString(),
        parentId = exercise.metadata.id,
        logType = Constants.DataLogs.ACTIVITY,
        dataType = dataType,
        value = length?.inMeters ?: 0.0,
        unit = Constants.DataUnits.METRE,
        source = source,
        startDateTime = timeManager.instantToIsoTime(startTime, startZoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, endZoneOffset),
        recordingMethod = recordingMethod,
        deviceId = idManager.getDeviceId(),
        deviceType = deviceType,
        modifiedDateTime = timeManager.instantToIsoTime(
            exercise.metadata.lastModifiedTime,
            endZoneOffset
        )
    )
}

internal fun ExerciseSegment.toSahhaDataLogDto(
    exercise: ExerciseSessionRecord,
    mapper: HealthConnectConstantsMapper = defaults.mapper,
    timeManager: SahhaTimeManager = defaults.timeManager,
    idManager: IdManager = defaults.idManager
): SahhaDataLog {
    val source = exercise.metadata.dataOrigin.packageName
    val startZoneOffset = exercise.startZoneOffset
    val endZoneOffset = exercise.endZoneOffset
    val recordingMethod = mapper.recordingMethod(exercise.metadata.recordingMethod)
    val deviceType = mapper.devices(exercise.metadata.device?.type)
    val segmentType = (mapper.exerciseSegments(segmentType) ?: Constants.UNKNOWN)

    return SahhaDataLog(
        id = UUID.nameUUIDFromBytes(
            (exercise.metadata.id + startTime + endTime)
                .toByteArray()
        ).toString(),
        parentId = exercise.metadata.id,
        logType = Constants.DataLogs.ACTIVITY,
        dataType = "segment_type_$segmentType",
        value = repetitions.toDouble(),
        unit = Constants.DataUnits.COUNT,
        source = source,
        startDateTime = timeManager.instantToIsoTime(startTime, startZoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, endZoneOffset),
        recordingMethod = recordingMethod,
        deviceId = idManager.getDeviceId(),
        deviceType = deviceType,
        modifiedDateTime = timeManager.instantToIsoTime(
            exercise.metadata.lastModifiedTime,
            endZoneOffset
        )
    )
}

internal fun SahhaDataLog.toSahhaSample(
    category: SahhaBiomarkerCategory,
    timeManager: SahhaTimeManager = defaults.timeManager
): SahhaSample {
    return SahhaSample(
        id = id,
        category = category.name,
        type = dataType,
        value = value,
        unit = unit,
        startDateTime = timeManager.ISOToZonedDateTime(startDateTime),
        endDateTime = timeManager.ISOToZonedDateTime(endDateTime),
        recordingMethod = recordingMethod,
        source = source,
    )
}

internal fun AppEvent.toSahhaDataLog(
    context: Context,
    mapper: HealthConnectConstantsMapper = defaults.mapper,
    idManager: IdManager = defaults.idManager
): SahhaDataLog {
    val dateTimeIso = Sahha.di.timeManager.instantToIsoTime(
        dateTime.toInstant()
    )

    return SahhaDataLog(
        id = UUID.nameUUIDFromBytes(
            (event.value + dateTime)
                .toByteArray()
        ).toString(),
        logType = Constants.DataLogs.DEVICE,
        dataType = event.value,
        source = context.packageName,
        value = 0.0,
        unit = Constants.DataUnits.EMPTY_STRING,
        startDateTime = dateTimeIso,
        endDateTime = dateTimeIso,
        recordingMethod = RecordingMethods.AUTOMATICALLY_RECORDED.name,
        deviceId = idManager.getDeviceId(),
        deviceType = mapper.devices(Device.TYPE_PHONE),
    )
}

internal fun PhoneUsage.toSahhaDataLogDto(
    mapper: HealthConnectConstantsMapper = defaults.mapper,
    idManager: IdManager = defaults.idManager
): SahhaDataLog {
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
        modifiedDateTime = modifiedDateTime,
        deviceId = idManager.getDeviceId(),
        deviceType = mapper.devices(Device.TYPE_PHONE)
    )
}

internal fun SleepDto.toSahhaDataLogDto(
    mapper: HealthConnectConstantsMapper = defaults.mapper,
    idManager: IdManager = defaults.idManager
): SahhaDataLog {
    return SahhaDataLog(
        id = id,
        logType = Constants.DataLogs.SLEEP,
        dataType = sleepStage,
        source = source,
        value = durationInMinutes.toDouble(),
        unit = Constants.DataUnits.MINUTE,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
        recordingMethod = RecordingMethods.AUTOMATICALLY_RECORDED.name,
        deviceId = idManager.getDeviceId(),
        deviceType = mapper.devices(Device.TYPE_PHONE),
        postDateTimes = postDateTimes,
        modifiedDateTime = modifiedDateTime,
    )
}

internal fun StepData.toSahhaDataLogAsChildLog(
    mapper: HealthConnectConstantsMapper = defaults.mapper,
    idManager: IdManager = defaults.idManager
): SahhaDataLog {
    return SahhaDataLog(
        id = id,
        logType = Constants.DataLogs.ACTIVITY,
        dataType = getDataType(source),
        value = count.toDouble(),
        unit = Constants.DataUnits.COUNT,
        source = source,
        startDateTime = detectedAt,
        endDateTime = detectedAt,
        deviceId = idManager.getDeviceId(),
        deviceType = mapper.devices(Device.TYPE_PHONE),
        recordingMethod = RecordingMethods.AUTOMATICALLY_RECORDED.name,
    )
}

internal fun StepSession.toSahhaDataLogAsChildLog(
    mapper: HealthConnectConstantsMapper = defaults.mapper,
    idManager: IdManager = defaults.idManager,
): SahhaDataLog {
    return SahhaDataLog(
        id = id,
        logType = Constants.DataLogs.ACTIVITY,
        dataType = "custom_step_sessions",
        value = count.toDouble(),
        unit = Constants.DataUnits.COUNT,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
        source = Constants.STEP_DETECTOR_DATA_SOURCE,
        deviceId = idManager.getDeviceId(),
        deviceType = mapper.devices(Device.TYPE_PHONE),
        recordingMethod = RecordingMethods.AUTOMATICALLY_RECORDED.name,
        postDateTimes = postDateTimes,
        modifiedDateTime = modifiedDateTime,
    )
}

private fun Double.toDecimalPercentage(
    mapper: HealthConnectConstantsMapper = defaults.mapper,
    timeManager: SahhaTimeManager = defaults.timeManager
): Double {
    return this / 100
}