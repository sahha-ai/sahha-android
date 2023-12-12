package sdk.sahha.android.data.mapper

import androidx.health.connect.client.aggregate.AggregationResultGroupedByDuration
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.BasalMetabolicRateRecord
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.BodyWaterMassRecord
import androidx.health.connect.client.records.BoneMassRecord
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
import sdk.sahha.android.domain.model.dto.SahhaDataLogDto
import sdk.sahha.android.domain.model.insight.InsightData
import sdk.sahha.android.domain.model.steps.StepsHealthConnect
import sdk.sahha.android.source.Sahha

private val mapper = Sahha.di.healthConnectConstantsMapper
private val timeManager = Sahha.di.timeManager

// Converted to SahhaDataLogDto later
fun StepsRecord.toStepsHealthConnect(): StepsHealthConnect {
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

fun SleepSessionRecord.toSahhaDataLogDto(): SahhaDataLogDto {
    return SahhaDataLogDto(
        logType = Constants.DataLogs.SLEEP,
        additionalProperties = hashMapOf(
            "sleepStage" to (mapper.sleepStages(SleepSessionRecord.STAGE_TYPE_SLEEPING)
                ?: Constants.SLEEP_STAGE_SLEEPING)
        ),
        dataType = Constants.DataTypes.SLEEP,
        source = metadata.dataOrigin.packageName,
        value = ((endTime.toEpochMilli() - startTime.toEpochMilli()) / 1000 / 60).toDouble(),
        unit = Constants.DataUnits.MINUTE,
        startDateTime = timeManager.instantToIsoTime(startTime, startZoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, endZoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceType = mapper.devices(metadata.device?.type),
    )
}

fun BloodGlucoseRecord.toSahhaDataLogDto(): SahhaDataLogDto {
    return SahhaDataLogDto(
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

fun BloodPressureRecord.toBloodPressureDiastolic(): SahhaDataLogDto {
    return SahhaDataLogDto(
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

        )
}

fun BloodPressureRecord.toBloodPressureSystolic(): SahhaDataLogDto {
    return SahhaDataLogDto(
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
    )
}

fun RestingHeartRateRecord.toSahhaLogDto(): SahhaDataLogDto {
    return SahhaDataLogDto(
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

fun AggregationResultGroupedByDuration.toActiveCaloriesBurned(): SahhaDataLogDto {
    return SahhaDataLogDto(
        logType = Constants.DataLogs.ENERGY,
        dataType = Constants.DataTypes.ACTIVE_ENERGY_BURNED,
        value = result[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories ?: 0.0,
        unit = Constants.DataUnits.KILOCALORIE,
        source = result.dataOrigins.map { it.packageName }.toString(),
        startDateTime = timeManager.instantToIsoTime(startTime, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, zoneOffset),
    )
}

fun AggregationResultGroupedByDuration.toTotalCaloriesBurned(): SahhaDataLogDto {
    return SahhaDataLogDto(
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

fun AggregationResultGroupedByDuration.toActiveEnergyInsight(): InsightData {
    return InsightData(
        name = Constants.INSIGHT_NAME_ACTIVE_ENERGY,
        value = result[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories ?: 0.0,
        unit = Constants.DataUnits.KILOCALORIE,
        startDateTime = timeManager.instantToIsoTime(startTime, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, zoneOffset)
    )
}

fun AggregationResultGroupedByDuration.toTotalEnergyInsight(): InsightData {
    return InsightData(
        name = Constants.INSIGHT_NAME_TOTAL_ENERGY,
        value = result[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inKilocalories ?: 0.0,
        unit = Constants.DataUnits.KILOCALORIE,
        startDateTime = timeManager.instantToIsoTime(startTime, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, zoneOffset)
    )
}

fun AggregationResultGroupedByDuration.toHeartRateAvg(): SahhaDataLogDto {
    return SahhaDataLogDto(
        logType = Constants.DataLogs.HEART,
        dataType = Constants.DataTypes.HEART_RATE_AVG,
        value = result[HeartRateRecord.BPM_AVG]?.toDouble() ?: 0.0,
        unit = Constants.DataUnits.BEAT_PER_MIN,
        source = result.dataOrigins.map { it.packageName }.toString(),
        startDateTime = timeManager.instantToIsoTime(startTime, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, zoneOffset),
    )
}

fun AggregationResultGroupedByDuration.toHeartRateMin(): SahhaDataLogDto {
    return SahhaDataLogDto(
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

fun AggregationResultGroupedByDuration.toHeartRateMax(): SahhaDataLogDto {
    return SahhaDataLogDto(
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

fun AggregationResultGroupedByDuration.toRestingHeartRateAvg(): SahhaDataLogDto {
    return SahhaDataLogDto(
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

fun AggregationResultGroupedByDuration.toRestingHeartRateMin(): SahhaDataLogDto {
    return SahhaDataLogDto(
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

fun AggregationResultGroupedByDuration.toRestingHeartRateMax(): SahhaDataLogDto {
    return SahhaDataLogDto(
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

fun HeartRateVariabilityRmssdRecord.toSahhaDataLogDto(): SahhaDataLogDto {
    return SahhaDataLogDto(
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

fun ActiveCaloriesBurnedRecord.toSahhaDataLogDto(): SahhaDataLogDto {
    return SahhaDataLogDto(
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

fun TotalCaloriesBurnedRecord.toSahhaDataLogDto(): SahhaDataLogDto {
    return SahhaDataLogDto(
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

fun OxygenSaturationRecord.toSahhaDataLogDto(): SahhaDataLogDto {
    return SahhaDataLogDto(
        logType = Constants.DataLogs.OXYGEN,
        dataType = Constants.DataTypes.OXYGEN_SATURATION,
        value = percentage.value,
        unit = Constants.DataUnits.PERCENTAGE,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceType = mapper.devices(metadata.device?.type),
    )
}

fun BasalMetabolicRateRecord.toSahhaDataLogDto(): SahhaDataLogDto {
    return SahhaDataLogDto(
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

fun BodyFatRecord.toSahhaDataLogDto(): SahhaDataLogDto {
    return SahhaDataLogDto(
        logType = Constants.DataLogs.BODY,
        dataType = Constants.DataTypes.BODY_FAT,
        value = percentage.value,
        unit = Constants.DataUnits.PERCENTAGE,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceType = mapper.devices(metadata.device?.type),
    )
}

fun BodyWaterMassRecord.toSahhaDataLogDto(): SahhaDataLogDto {
    return SahhaDataLogDto(
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

fun LeanBodyMassRecord.toSahhaDataLogDto(): SahhaDataLogDto {
    return SahhaDataLogDto(
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

fun BoneMassRecord.toSahhaDataLogDto(): SahhaDataLogDto {
    return SahhaDataLogDto(
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

fun HeightRecord.toSahhaDataLogDto(): SahhaDataLogDto {
    return SahhaDataLogDto(
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

fun WeightRecord.toSahhaDataLogDto(): SahhaDataLogDto {
    return SahhaDataLogDto(
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

fun Vo2MaxRecord.toSahhaDataLogDto(): SahhaDataLogDto {
    return SahhaDataLogDto(
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

fun RespiratoryRateRecord.toSahhaDataLogDto(): SahhaDataLogDto {
    return SahhaDataLogDto(
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
