package sdk.sahha.android.data.mapper

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
import sdk.sahha.android.common.Constants
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.domain.mapper.HealthConnectConstantsMapper
import sdk.sahha.android.domain.model.data_log.SahhaDataLog
import sdk.sahha.android.domain.model.insight.InsightData
import sdk.sahha.android.domain.model.local_logs.SahhaSample
import sdk.sahha.android.domain.model.local_logs.SahhaStat
import sdk.sahha.android.domain.model.steps.StepsHealthConnect
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensor
import java.time.ZonedDateTime
import java.util.UUID

// Converted to SahhaDataLogDto later
// Parameters are for unit tests
internal fun StepsRecord.toStepsHealthConnect(
    mapper: HealthConnectConstantsMapper = Sahha.di.healthConnectConstantsMapper,
    timeManager: SahhaTimeManager = Sahha.di.timeManager
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

internal fun SleepSessionRecord.toSahhaDataLogDto(
    mapper: HealthConnectConstantsMapper = Sahha.di.healthConnectConstantsMapper,
    timeManager: SahhaTimeManager = Sahha.di.timeManager
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
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, endZoneOffset)
    )
}

internal fun SleepSessionRecord.Stage.toSahhaDataLog(
    session: SleepSessionRecord,
    mapper: HealthConnectConstantsMapper = Sahha.di.healthConnectConstantsMapper,
    timeManager: SahhaTimeManager = Sahha.di.timeManager
): SahhaDataLog {
    val durationInMinutes =
        ((endTime.toEpochMilli() - startTime.toEpochMilli()).toDouble() / 1000 / 60)
    return SahhaDataLog(
        id = UUID.randomUUID().toString(),
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
        deviceType = mapper.devices(session.metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(
            session.metadata.lastModifiedTime,
            session.endZoneOffset
        )
    )
}

internal fun BloodGlucoseRecord.toSahhaDataLogDto(
    mapper: HealthConnectConstantsMapper = Sahha.di.healthConnectConstantsMapper,
    timeManager: SahhaTimeManager = Sahha.di.timeManager
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
    mapper: HealthConnectConstantsMapper = Sahha.di.healthConnectConstantsMapper,
    timeManager: SahhaTimeManager = Sahha.di.timeManager
): SahhaDataLog {
    return SahhaDataLog(
        id = UUID.randomUUID().toString(),
        logType = Constants.DataLogs.BLOOD,
        dataType = SahhaSensor.blood_pressure_diastolic.name,
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        value = diastolic.inMillimetersOfMercury,
        unit = Constants.DataUnits.MMHG,
        source = metadata.dataOrigin.packageName,
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
    mapper: HealthConnectConstantsMapper = Sahha.di.healthConnectConstantsMapper,
    timeManager: SahhaTimeManager = Sahha.di.timeManager
): SahhaDataLog {
    return SahhaDataLog(
        id = UUID.randomUUID().toString(),
        logType = Constants.DataLogs.BLOOD,
        dataType = SahhaSensor.blood_pressure_systolic.name,
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        value = systolic.inMillimetersOfMercury,
        unit = Constants.DataUnits.MMHG,
        source = metadata.dataOrigin.packageName,
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
    mapper: HealthConnectConstantsMapper = Sahha.di.healthConnectConstantsMapper,
    timeManager: SahhaTimeManager = Sahha.di.timeManager
): SahhaDataLog {
    return SahhaDataLog(
        id = UUID.randomUUID().toString(),
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
        deviceType = mapper.devices(record.metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(
            record.metadata.lastModifiedTime,
            record.endZoneOffset
        )
    )
}

internal fun RestingHeartRateRecord.toSahhaLogDto(
    mapper: HealthConnectConstantsMapper = Sahha.di.healthConnectConstantsMapper,
    timeManager: SahhaTimeManager = Sahha.di.timeManager
): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.HEART,
        dataType = SahhaSensor.resting_heart_rate.name,
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        value = beatsPerMinute.toDouble(),
        unit = Constants.DataUnits.BEAT_PER_MIN,
        source = metadata.dataOrigin.packageName,
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

internal fun AggregationResultGroupedByDuration.toActiveCaloriesBurned(
    mapper: HealthConnectConstantsMapper = Sahha.di.healthConnectConstantsMapper,
    timeManager: SahhaTimeManager = Sahha.di.timeManager
): SahhaDataLog {
    return SahhaDataLog(
        id = UUID.randomUUID().toString(),
        logType = Constants.DataLogs.ENERGY,
        dataType = SahhaSensor.active_energy_burned.name,
        value = result[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories ?: 0.0,
        unit = Constants.DataUnits.KILOCALORIE,
        source = result.dataOrigins.map { it.packageName }.toString(),
        startDateTime = timeManager.instantToIsoTime(startTime, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, zoneOffset),
    )
}

internal fun AggregationResultGroupedByDuration.toTotalCaloriesBurned(
    mapper: HealthConnectConstantsMapper = Sahha.di.healthConnectConstantsMapper,
    timeManager: SahhaTimeManager = Sahha.di.timeManager
): SahhaDataLog {
    return SahhaDataLog(
        id = UUID.randomUUID().toString(),
        logType = Constants.DataLogs.ENERGY,
        dataType = SahhaSensor.total_energy_burned.name,
        value = result[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inKilocalories
            ?: 0.0,
        unit = Constants.DataUnits.KILOCALORIE,
        source = result.dataOrigins.map { it.packageName }.toString(),
        startDateTime = timeManager.instantToIsoTime(startTime, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, zoneOffset),
    )
}

internal fun AggregationResultGroupedByDuration.toActiveEnergyInsight(
    mapper: HealthConnectConstantsMapper = Sahha.di.healthConnectConstantsMapper,
    timeManager: SahhaTimeManager = Sahha.di.timeManager
): InsightData {
    return InsightData(
        name = Constants.INSIGHT_NAME_ACTIVE_ENERGY,
        value = result[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories ?: 0.0,
        unit = Constants.DataUnits.KILOCALORIE,
        startDateTime = timeManager.instantToIsoTime(startTime, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, zoneOffset)
    )
}

internal fun AggregationResultGroupedByDuration.toTotalEnergyInsight(
    mapper: HealthConnectConstantsMapper = Sahha.di.healthConnectConstantsMapper,
    timeManager: SahhaTimeManager = Sahha.di.timeManager
): InsightData {
    return InsightData(
        name = Constants.INSIGHT_NAME_TOTAL_ENERGY,
        value = result[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inKilocalories ?: 0.0,
        unit = Constants.DataUnits.KILOCALORIE,
        startDateTime = timeManager.instantToIsoTime(startTime, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, zoneOffset)
    )
}

internal fun AggregationResultGroupedByDuration.toSahhaStat(
    sensor: SahhaSensor,
    value: Double,
    unit: String,
    sources: List<String> = listOf()
): SahhaStat {
    val consistentUid = UUID.nameUUIDFromBytes(
        (startTime.toEpochMilli() + endTime.toEpochMilli()).toString().toByteArray()
    )
    return SahhaStat(
        id = consistentUid.toString(),
        type = sensor.name,
        value = value,
        unit = unit,
        startDate = ZonedDateTime.ofInstant(this.startTime, this.zoneOffset),
        endDate = ZonedDateTime.ofInstant(this.endTime, this.zoneOffset),
        sources = sources
    )
}

internal fun HeartRateVariabilityRmssdRecord.toSahhaDataLogDto(
    mapper: HealthConnectConstantsMapper = Sahha.di.healthConnectConstantsMapper,
    timeManager: SahhaTimeManager = Sahha.di.timeManager
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
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset)
    )
}

internal fun ActiveCaloriesBurnedRecord.toSahhaDataLogDto(
    mapper: HealthConnectConstantsMapper = Sahha.di.healthConnectConstantsMapper,
    timeManager: SahhaTimeManager = Sahha.di.timeManager
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
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, endZoneOffset)
    )
}

internal fun TotalCaloriesBurnedRecord.toSahhaDataLogDto(
    mapper: HealthConnectConstantsMapper = Sahha.di.healthConnectConstantsMapper,
    timeManager: SahhaTimeManager = Sahha.di.timeManager
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
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, endZoneOffset)
    )
}

internal fun OxygenSaturationRecord.toSahhaDataLogDto(
    mapper: HealthConnectConstantsMapper = Sahha.di.healthConnectConstantsMapper,
    timeManager: SahhaTimeManager = Sahha.di.timeManager
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
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset)
    )
}

internal fun BasalMetabolicRateRecord.toSahhaDataLogDto(
    mapper: HealthConnectConstantsMapper = Sahha.di.healthConnectConstantsMapper,
    timeManager: SahhaTimeManager = Sahha.di.timeManager
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
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset)
    )
}

internal fun BodyFatRecord.toSahhaDataLogDto(
    mapper: HealthConnectConstantsMapper = Sahha.di.healthConnectConstantsMapper,
    timeManager: SahhaTimeManager = Sahha.di.timeManager
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
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset)
    )
}

internal fun BodyWaterMassRecord.toSahhaDataLogDto(
    mapper: HealthConnectConstantsMapper = Sahha.di.healthConnectConstantsMapper,
    timeManager: SahhaTimeManager = Sahha.di.timeManager
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
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset)
    )
}

internal fun LeanBodyMassRecord.toSahhaDataLogDto(
    mapper: HealthConnectConstantsMapper = Sahha.di.healthConnectConstantsMapper,
    timeManager: SahhaTimeManager = Sahha.di.timeManager
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
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset)
    )
}

internal fun BoneMassRecord.toSahhaDataLogDto(
    mapper: HealthConnectConstantsMapper = Sahha.di.healthConnectConstantsMapper,
    timeManager: SahhaTimeManager = Sahha.di.timeManager
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
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset)
    )
}

internal fun HeightRecord.toSahhaDataLogDto(
    mapper: HealthConnectConstantsMapper = Sahha.di.healthConnectConstantsMapper,
    timeManager: SahhaTimeManager = Sahha.di.timeManager
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
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset)
    )
}

internal fun WeightRecord.toSahhaDataLogDto(
    mapper: HealthConnectConstantsMapper = Sahha.di.healthConnectConstantsMapper,
    timeManager: SahhaTimeManager = Sahha.di.timeManager
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
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset)
    )
}

internal fun Vo2MaxRecord.toSahhaDataLogDto(
    mapper: HealthConnectConstantsMapper = Sahha.di.healthConnectConstantsMapper,
    timeManager: SahhaTimeManager = Sahha.di.timeManager
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
        deviceType = mapper.devices(metadata.device?.type),
        additionalProperties = hashMapOf(
            "measurementMethod" to (mapper.measurementMethod(measurementMethod)
                ?: Constants.UNKNOWN),
        ),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset)
    )
}

internal fun RespiratoryRateRecord.toSahhaDataLogDto(
    mapper: HealthConnectConstantsMapper = Sahha.di.healthConnectConstantsMapper,
    timeManager: SahhaTimeManager = Sahha.di.timeManager
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
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset)
    )
}

internal fun FloorsClimbedRecord.toSahhaDataLogDto(
    mapper: HealthConnectConstantsMapper = Sahha.di.healthConnectConstantsMapper,
    timeManager: SahhaTimeManager = Sahha.di.timeManager
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
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, endZoneOffset)
    )
}

internal fun BodyTemperatureRecord.toSahhaDataLogDto(
    mapper: HealthConnectConstantsMapper = Sahha.di.healthConnectConstantsMapper,
    timeManager: SahhaTimeManager = Sahha.di.timeManager
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
        deviceType = mapper.devices(metadata.device?.type),
        additionalProperties = hashMapOf(
            "measurementLocation" to (mapper.bodyTempMeasurementLocation(measurementLocation)
                ?: Constants.UNKNOWN)
        ),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset)
    )
}

internal fun BasalBodyTemperatureRecord.toSahhaDataLogDto(
    mapper: HealthConnectConstantsMapper = Sahha.di.healthConnectConstantsMapper,
    timeManager: SahhaTimeManager = Sahha.di.timeManager
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
        deviceType = mapper.devices(metadata.device?.type),
        additionalProperties = hashMapOf(
            "measurementLocation" to (mapper.bodyTempMeasurementLocation(measurementLocation)
                ?: Constants.UNKNOWN)
        ),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset)
    )
}

internal fun ExerciseSessionRecord.toSahhaDataLogDto(
    mapper: HealthConnectConstantsMapper = Sahha.di.healthConnectConstantsMapper,
    timeManager: SahhaTimeManager = Sahha.di.timeManager
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
        deviceType = deviceType,
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, endZoneOffset)
    )
}

internal fun ExerciseLap.toSahhaDataLogDto(
    exercise: ExerciseSessionRecord,
    mapper: HealthConnectConstantsMapper = Sahha.di.healthConnectConstantsMapper,
    timeManager: SahhaTimeManager = Sahha.di.timeManager
): SahhaDataLog {
    val source = exercise.metadata.dataOrigin.packageName
    val startZoneOffset = exercise.startZoneOffset
    val endZoneOffset = exercise.endZoneOffset
    val recordingMethod = mapper.recordingMethod(exercise.metadata.recordingMethod)
    val deviceType = mapper.devices(exercise.metadata.device?.type)

    val dataType = "exercise_lap"
    return SahhaDataLog(
        id = UUID.randomUUID().toString(),
        parentId = exercise.metadata.id,
        logType = Constants.DataLogs.ACTIVITY,
        dataType = dataType,
        value = length?.inMeters ?: 0.0,
        unit = Constants.DataUnits.METRE,
        source = source,
        startDateTime = timeManager.instantToIsoTime(startTime, startZoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, endZoneOffset),
        recordingMethod = recordingMethod,
        deviceType = deviceType,
        modifiedDateTime = timeManager.instantToIsoTime(
            exercise.metadata.lastModifiedTime,
            endZoneOffset
        )
    )
}

internal fun ExerciseSegment.toSahhaDataLogDto(
    exercise: ExerciseSessionRecord,
    mapper: HealthConnectConstantsMapper = Sahha.di.healthConnectConstantsMapper,
    timeManager: SahhaTimeManager = Sahha.di.timeManager
): SahhaDataLog {
    val source = exercise.metadata.dataOrigin.packageName
    val startZoneOffset = exercise.startZoneOffset
    val endZoneOffset = exercise.endZoneOffset
    val recordingMethod = mapper.recordingMethod(exercise.metadata.recordingMethod)
    val deviceType = mapper.devices(exercise.metadata.device?.type)
    val segmentType = (mapper.exerciseSegments(segmentType) ?: Constants.UNKNOWN)

    return SahhaDataLog(
        id = UUID.randomUUID().toString(),
        parentId = exercise.metadata.id,
        logType = Constants.DataLogs.ACTIVITY,
        dataType = "segment_type_$segmentType",
        value = repetitions.toDouble(),
        unit = Constants.DataUnits.COUNT,
        source = source,
        startDateTime = timeManager.instantToIsoTime(startTime, startZoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, endZoneOffset),
        recordingMethod = recordingMethod,
        deviceType = deviceType,
        modifiedDateTime = timeManager.instantToIsoTime(
            exercise.metadata.lastModifiedTime,
            endZoneOffset
        )
    )
}

internal fun SahhaDataLog.toSahhaSample(): SahhaSample {
    return SahhaSample(
        id,
        dataType,
        value,
        source,
        startDateTime,
        endDateTime,
        unit,
    )
}

private fun Double.toDecimalPercentage(
    mapper: HealthConnectConstantsMapper = Sahha.di.healthConnectConstantsMapper,
    timeManager: SahhaTimeManager = Sahha.di.timeManager
): Double {
    return this / 100
}