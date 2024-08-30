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
import androidx.health.connect.client.records.CervicalMucusRecord
import androidx.health.connect.client.records.ExerciseLap
import androidx.health.connect.client.records.ExerciseSegment
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.FloorsClimbedRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.HeightRecord
import androidx.health.connect.client.records.IntermenstrualBleedingRecord
import androidx.health.connect.client.records.LeanBodyMassRecord
import androidx.health.connect.client.records.MenstruationFlowRecord
import androidx.health.connect.client.records.MenstruationPeriodRecord
import androidx.health.connect.client.records.OvulationTestRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.RespiratoryRateRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SexualActivityRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.Vo2MaxRecord
import androidx.health.connect.client.records.WeightRecord
import sdk.sahha.android.common.Constants
import sdk.sahha.android.domain.model.data_log.SahhaDataLog
import sdk.sahha.android.domain.model.insight.InsightData
import sdk.sahha.android.domain.model.steps.StepsHealthConnect
import sdk.sahha.android.source.Sahha
import java.util.UUID

private val mapper = Sahha.di.healthConnectConstantsMapper
private val timeManager = Sahha.di.timeManager

// Converted to SahhaDataLogDto later
internal fun StepsRecord.toStepsHealthConnect(): StepsHealthConnect {
    return StepsHealthConnect(
        metaId = metadata.id,
        dataType = Constants.DataTypes.STEP,
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

internal fun SleepSessionRecord.toSahhaDataLogDto(): SahhaDataLog {
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
    )
}

internal fun SleepSessionRecord.Stage.toSahhaDataLog(session: SleepSessionRecord): SahhaDataLog {
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
    )
}

internal fun BloodGlucoseRecord.toSahhaDataLogDto(): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.BLOOD,
        dataType = Constants.DataTypes.BLOOD_GLUCOSE,
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
    )
}

internal fun BloodPressureRecord.toBloodPressureDiastolic(): SahhaDataLog {
    return SahhaDataLog(
        id = UUID.randomUUID().toString(),
        logType = Constants.DataLogs.BLOOD,
        dataType = Constants.DataTypes.BLOOD_PRESSURE_DIASTOLIC,
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
        parentId = metadata.id
    )
}

internal fun BloodPressureRecord.toBloodPressureSystolic(): SahhaDataLog {
    return SahhaDataLog(
        id = UUID.randomUUID().toString(),
        logType = Constants.DataLogs.BLOOD,
        dataType = Constants.DataTypes.BLOOD_PRESSURE_SYSTOLIC,
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
        parentId = metadata.id
    )
}

internal fun HeartRateRecord.Sample.toSahhaDataLog(record: HeartRateRecord): SahhaDataLog {
    return SahhaDataLog(
        id = UUID.randomUUID().toString(),
        parentId = record.metadata.id,
        logType = Constants.DataLogs.HEART,
        dataType = Constants.DataTypes.HEART_RATE,
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
    )
}

internal fun RestingHeartRateRecord.toSahhaLogDto(): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.HEART,
        dataType = Constants.DataTypes.RESTING_HEART_RATE,
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
    )
}

internal fun AggregationResultGroupedByDuration.toActiveCaloriesBurned(): SahhaDataLog {
    return SahhaDataLog(
        id = UUID.randomUUID().toString(),
        logType = Constants.DataLogs.ENERGY,
        dataType = Constants.DataTypes.ACTIVE_ENERGY_BURNED,
        value = result[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories ?: 0.0,
        unit = Constants.DataUnits.KILOCALORIE,
        source = result.dataOrigins.map { it.packageName }.toString(),
        startDateTime = timeManager.instantToIsoTime(startTime, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, zoneOffset),
    )
}

internal fun AggregationResultGroupedByDuration.toTotalCaloriesBurned(): SahhaDataLog {
    return SahhaDataLog(
        id = UUID.randomUUID().toString(),
        logType = Constants.DataLogs.ENERGY,
        dataType = Constants.DataTypes.TOTAL_ENERGY_BURNED,
        value = result[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inKilocalories
            ?: 0.0,
        unit = Constants.DataUnits.KILOCALORIE,
        source = result.dataOrigins.map { it.packageName }.toString(),
        startDateTime = timeManager.instantToIsoTime(startTime, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, zoneOffset),
    )
}

internal fun AggregationResultGroupedByDuration.toActiveEnergyInsight(): InsightData {
    return InsightData(
        name = Constants.INSIGHT_NAME_ACTIVE_ENERGY,
        value = result[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories ?: 0.0,
        unit = Constants.DataUnits.KILOCALORIE,
        startDateTime = timeManager.instantToIsoTime(startTime, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, zoneOffset)
    )
}

internal fun AggregationResultGroupedByDuration.toTotalEnergyInsight(): InsightData {
    return InsightData(
        name = Constants.INSIGHT_NAME_TOTAL_ENERGY,
        value = result[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inKilocalories ?: 0.0,
        unit = Constants.DataUnits.KILOCALORIE,
        startDateTime = timeManager.instantToIsoTime(startTime, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, zoneOffset)
    )
}

internal fun AggregationResultGroupedByDuration.toHeartRateAvg(): SahhaDataLog {
    return SahhaDataLog(
        id = UUID.randomUUID().toString(),
        logType = Constants.DataLogs.HEART,
        dataType = Constants.DataTypes.HEART_RATE_AVG,
        value = result[HeartRateRecord.BPM_AVG]?.toDouble() ?: 0.0,
        unit = Constants.DataUnits.BEAT_PER_MIN,
        source = result.dataOrigins.map { it.packageName }.toString(),
        startDateTime = timeManager.instantToIsoTime(startTime, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, zoneOffset),
    )
}

internal fun AggregationResultGroupedByDuration.toHeartRateMin(): SahhaDataLog {
    return SahhaDataLog(
        id = UUID.randomUUID().toString(),
        logType = Constants.DataLogs.HEART,
        dataType = Constants.DataTypes.HEART_RATE_MIN,
        value = result[HeartRateRecord.BPM_MIN]?.toDouble() ?: 0.0,
        unit = Constants.DataUnits.BEAT_PER_MIN,
        source = result.dataOrigins.map {
            it.packageName
        }.toString(),
        startDateTime = timeManager.instantToIsoTime(startTime, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, zoneOffset),
    )
}

internal fun AggregationResultGroupedByDuration.toHeartRateMax(): SahhaDataLog {
    return SahhaDataLog(
        id = UUID.randomUUID().toString(),
        logType = Constants.DataLogs.HEART,
        dataType = Constants.DataTypes.HEART_RATE_MAX,
        value = result[HeartRateRecord.BPM_MAX]?.toDouble() ?: -0.0,
        unit = Constants.DataUnits.BEAT_PER_MIN,
        source = result.dataOrigins.map {
            it.packageName
        }.toString(),
        startDateTime = timeManager.instantToIsoTime(startTime, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, zoneOffset),
    )
}

internal fun AggregationResultGroupedByDuration.toRestingHeartRateAvg(): SahhaDataLog {
    return SahhaDataLog(
        id = UUID.randomUUID().toString(),
        logType = Constants.DataLogs.HEART,
        dataType = Constants.DataTypes.RESTING_HEART_RATE_AVG,
        value = result[RestingHeartRateRecord.BPM_AVG]?.toDouble() ?: 0.0,
        unit = Constants.DataUnits.BEAT_PER_MIN,
        source = result.dataOrigins.map {
            it.packageName
        }.toString(),
        startDateTime = timeManager.instantToIsoTime(startTime, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, zoneOffset),
    )
}

internal fun AggregationResultGroupedByDuration.toRestingHeartRateMin(): SahhaDataLog {
    return SahhaDataLog(
        id = UUID.randomUUID().toString(),
        logType = Constants.DataLogs.HEART,
        dataType = Constants.DataTypes.RESTING_HEART_RATE_MIN,
        value = result[RestingHeartRateRecord.BPM_MIN]?.toDouble() ?: 0.0,
        unit = Constants.DataUnits.BEAT_PER_MIN,
        source = result.dataOrigins.map {
            it.packageName
        }.toString(),
        startDateTime = timeManager.instantToIsoTime(startTime, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, zoneOffset),
    )
}

internal fun AggregationResultGroupedByDuration.toRestingHeartRateMax(): SahhaDataLog {
    return SahhaDataLog(
        id = UUID.randomUUID().toString(),
        logType = Constants.DataLogs.HEART,
        dataType = Constants.DataTypes.RESTING_HEART_RATE_MAX,
        value = result[RestingHeartRateRecord.BPM_MAX]?.toDouble() ?: 0.0,
        unit = Constants.DataUnits.BEAT_PER_MIN,
        source = result.dataOrigins.map {
            it.packageName
        }.toString(),
        startDateTime = timeManager.instantToIsoTime(startTime, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, zoneOffset),
    )
}

internal fun HeartRateVariabilityRmssdRecord.toSahhaDataLogDto(): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.HEART,
        dataType = Constants.DataTypes.HEART_RATE_VARIABILITY_RMSSD,
        value = this.heartRateVariabilityMillis,
        unit = Constants.DataUnits.MILLISECOND,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceType = mapper.devices(metadata.device?.type),
    )
}

internal fun ActiveCaloriesBurnedRecord.toSahhaDataLogDto(): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.ENERGY,
        dataType = Constants.DataTypes.ACTIVE_ENERGY_BURNED,
        value = energy.inKilocalories,
        unit = Constants.DataUnits.KILOCALORIE,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(startTime, startZoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, endZoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceType = mapper.devices(metadata.device?.type),
    )
}

internal fun TotalCaloriesBurnedRecord.toSahhaDataLogDto(): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.ENERGY,
        dataType = Constants.DataTypes.TOTAL_ENERGY_BURNED,
        value = energy.inKilocalories,
        unit = Constants.DataUnits.KILOCALORIE,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(startTime, startZoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, endZoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceType = mapper.devices(metadata.device?.type),
    )
}

internal fun OxygenSaturationRecord.toSahhaDataLogDto(): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.OXYGEN,
        dataType = Constants.DataTypes.OXYGEN_SATURATION,
        value = percentage.value.toDecimalPercentage(),
        unit = Constants.DataUnits.PERCENTAGE,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceType = mapper.devices(metadata.device?.type),
    )
}

internal fun BasalMetabolicRateRecord.toSahhaDataLogDto(): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.ENERGY,
        dataType = Constants.DataTypes.BASAL_METABOLIC_RATE,
        value = this.basalMetabolicRate.inKilocaloriesPerDay,
        unit = Constants.DataUnits.KCAL_PER_DAY,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceType = mapper.devices(metadata.device?.type),
    )
}

internal fun BodyFatRecord.toSahhaDataLogDto(): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.BODY,
        dataType = Constants.DataTypes.BODY_FAT,
        value = percentage.value.toDecimalPercentage(),
        unit = Constants.DataUnits.PERCENTAGE,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceType = mapper.devices(metadata.device?.type),
    )
}

internal fun BodyWaterMassRecord.toSahhaDataLogDto(): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.BODY,
        dataType = Constants.DataTypes.BODY_WATER_MASS,
        value = mass.inKilograms,
        unit = Constants.DataUnits.KILOGRAM,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceType = mapper.devices(metadata.device?.type),
    )
}

internal fun LeanBodyMassRecord.toSahhaDataLogDto(): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.BODY,
        dataType = Constants.DataTypes.LEAN_BODY_MASS,
        value = mass.inKilograms,
        unit = Constants.DataUnits.KILOGRAM,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceType = mapper.devices(metadata.device?.type),
    )
}

internal fun BoneMassRecord.toSahhaDataLogDto(): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.BODY,
        dataType = Constants.DataTypes.BONE_MASS,
        value = mass.inKilograms,
        unit = Constants.DataUnits.KILOGRAM,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceType = mapper.devices(metadata.device?.type),
    )
}

internal fun HeightRecord.toSahhaDataLogDto(): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.BODY,
        dataType = Constants.DataTypes.HEIGHT,
        value = height.inMeters,
        unit = Constants.DataUnits.METRE,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceType = mapper.devices(metadata.device?.type),
    )
}

internal fun WeightRecord.toSahhaDataLogDto(): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.BODY,
        dataType = Constants.DataTypes.WEIGHT,
        value = weight.inKilograms,
        unit = Constants.DataUnits.KILOGRAM,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceType = mapper.devices(metadata.device?.type),
    )
}

internal fun Vo2MaxRecord.toSahhaDataLogDto(): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.OXYGEN,
        dataType = Constants.DataTypes.VO2_MAX,
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
        )
    )
}

internal fun RespiratoryRateRecord.toSahhaDataLogDto(): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.OXYGEN,
        dataType = Constants.DataTypes.RESPIRATORY_RATE,
        value = rate,
        unit = Constants.DataUnits.BREATH_PER_MIN,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceType = mapper.devices(metadata.device?.type),
    )
}

internal fun FloorsClimbedRecord.toSahhaDataLogDto(): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.ACTIVITY,
        dataType = Constants.DataTypes.FLOOR_COUNT,
        value = floors,
        unit = Constants.DataUnits.COUNT,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(startTime, startZoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, endZoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceType = mapper.devices(metadata.device?.type),
    )
}

internal fun BodyTemperatureRecord.toSahhaDataLogDto(): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.TEMPERATURE,
        dataType = Constants.DataTypes.BODY_TEMPERATURE,
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
        )
    )
}

internal fun BasalBodyTemperatureRecord.toSahhaDataLogDto(): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.TEMPERATURE,
        dataType = Constants.DataTypes.BASAL_BODY_TEMPERATURE,
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
        )
    )
}

internal fun ExerciseSessionRecord.toSahhaDataLogDto(): SahhaDataLog {
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
    )
}

internal fun ExerciseLap.toSahhaDataLogDto(
    exercise: ExerciseSessionRecord
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
    )
}

internal fun ExerciseSegment.toSahhaDataLogDto(
    exercise: ExerciseSessionRecord
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
    )
}

internal fun CervicalMucusRecord.toSahhaDataLogDto(): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.REPRODUCTIVE,
        dataType = Constants.DataTypes.CERVICAL_MUCUS,
        value = 1.0,
        unit = Constants.DataUnits.COUNT,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceType = mapper.devices(metadata.device?.type),
        additionalProperties = hashMapOf(
            "appearance" to (mapper.cervicalMucusAppearance(appearance) ?: Constants.UNKNOWN),
            "sensation" to (mapper.cervicalMucusSensation(sensation) ?: Constants.UNKNOWN),
        )
    )
}

internal fun IntermenstrualBleedingRecord.toSahhaDataLogDto(): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.REPRODUCTIVE,
        dataType = Constants.DataTypes.INTERMENSTRUAL_BLEEDING,
        value = 1.0,
        unit = Constants.DataUnits.COUNT,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceType = mapper.devices(metadata.device?.type),
    )
}

internal fun MenstruationFlowRecord.toSahhaDataLogDto(): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.REPRODUCTIVE,
        dataType = Constants.DataTypes.MENSTRUATION_FLOW,
        value = 1.0,
        unit = Constants.DataUnits.COUNT,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceType = mapper.devices(metadata.device?.type),
        additionalProperties = hashMapOf(
            "flow" to (mapper.menstruationFlow(flow) ?: Constants.UNKNOWN)
        )
    )
}

internal fun MenstruationPeriodRecord.toSahhaDataLogDto(): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.REPRODUCTIVE,
        dataType = Constants.DataTypes.MENSTRUATION_PERIOD,
        value = 1.0,
        unit = Constants.DataUnits.COUNT,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(startTime, startZoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, endZoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceType = mapper.devices(metadata.device?.type),
    )
}

internal fun OvulationTestRecord.toSahhaDataLogDto(): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.REPRODUCTIVE,
        dataType = Constants.DataTypes.OVULATION_TEST,
        value = 1.0,
        unit = Constants.DataUnits.COUNT,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceType = mapper.devices(metadata.device?.type),
        additionalProperties = hashMapOf(
            "result" to (mapper.ovulationTestResult(result) ?: Constants.UNKNOWN)
        )
    )
}

internal fun SexualActivityRecord.toSahhaDataLogDto(): SahhaDataLog {
    return SahhaDataLog(
        id = metadata.id,
        logType = Constants.DataLogs.REPRODUCTIVE,
        dataType = Constants.DataTypes.SEXUAL_ACTIVITY,
        value = 1.0,
        unit = Constants.DataUnits.COUNT,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceType = mapper.devices(metadata.device?.type),
        additionalProperties = hashMapOf(
            "protectionUsed" to (mapper.sexualActivityProtectionUsed(protectionUsed) ?: Constants.UNKNOWN)
        )
    )
}

private fun Double.toDecimalPercentage(): Double {
    return this / 100
}