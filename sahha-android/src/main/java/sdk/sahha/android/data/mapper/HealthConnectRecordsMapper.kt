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
import androidx.health.connect.client.records.StepsCadenceRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.Vo2MaxRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.metadata.Metadata
import sdk.sahha.android.common.Constants
import sdk.sahha.android.domain.model.dto.BloodGlucoseDto
import sdk.sahha.android.domain.model.dto.BloodPressureDto
import sdk.sahha.android.domain.model.dto.HealthDataDto
import sdk.sahha.android.domain.model.dto.Vo2MaxDto
import sdk.sahha.android.domain.model.dto.send.SleepSendDto
import sdk.sahha.android.domain.model.steps.StepsHealthConnect
import sdk.sahha.android.source.Sahha
import java.time.ZoneOffset

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
        deviceType = mapper.devices(metadata.device?.type),
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
        sleepStage = mapper.sleepStages(SleepSessionRecord.STAGE_TYPE_SLEEPING),
        source = metadata.dataOrigin.packageName,
        durationInMinutes = ((endTime.toEpochMilli() - startTime.toEpochMilli()) / 1000 / 60).toInt(),
        startDateTime = timeManager.instantToIsoTime(startTime, startZoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, endZoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(
            metadata.lastModifiedTime,
            endZoneOffset
        ),
        deviceManufacturer = metadata.device?.manufacturer ?: Constants.UNKNOWN,
        deviceModel = metadata.device?.model ?: Constants.UNKNOWN
    )
}

fun BloodGlucoseRecord.toBloodGlucoseDto(): BloodGlucoseDto {
    return BloodGlucoseDto(
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        count = level.inMillimolesPerLiter,
        source = metadata.dataOrigin.packageName,
        deviceType = mapper.devices(metadata.device?.type),
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
        specimenSource = mapper.specimenSource(specimenSource) ?: Constants.UNKNOWN,
        deviceManufacturer = metadata.device?.manufacturer ?: Constants.UNKNOWN,
        deviceModel = metadata.device?.model ?: Constants.UNKNOWN
    )
}

fun BloodPressureRecord.toBloodPressureDiastolicDto(): BloodPressureDto {
    return BloodPressureDto(
        dataType = Constants.HEALTH_CONNECT_BLOOD_PRESSURE_DIASTOLIC,
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        count = diastolic.inMillimetersOfMercury,
        source = metadata.dataOrigin.packageName,
        deviceType = mapper.devices(metadata.device?.type),
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
        deviceManufacturer = metadata.device?.manufacturer ?: Constants.UNKNOWN,
        deviceModel = metadata.device?.model ?: Constants.UNKNOWN
    )
}

fun BloodPressureRecord.toBloodPressureSystolicDto(): BloodPressureDto {
    return BloodPressureDto(
        dataType = Constants.HEALTH_CONNECT_BLOOD_PRESSURE_SYSTOLIC,
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        count = systolic.inMillimetersOfMercury,
        source = metadata.dataOrigin.packageName,
        deviceType = mapper.devices(metadata.device?.type),
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
        deviceManufacturer = metadata.device?.manufacturer ?: Constants.UNKNOWN,
        deviceModel = metadata.device?.model ?: Constants.UNKNOWN
    )
}

fun RestingHeartRateRecord.toHeartRateDto(): HealthDataDto {
    return HealthDataDto(
        dataType = Constants.HEALTH_CONNECT_RESTING_HEART_RATE,
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        count = beatsPerMinute,
        source = metadata.dataOrigin.packageName,
        deviceType = mapper.devices(metadata.device?.type),
        startDateTime = timeManager.instantToIsoTime(
            time, zoneOffset
        ),
        endDateTime = timeManager.instantToIsoTime(
            time, zoneOffset
        ),
        modifiedDateTime = timeManager.instantToIsoTime(
            metadata.lastModifiedTime, zoneOffset
        ),
        deviceManufacturer = metadata.device?.manufacturer ?: Constants.UNKNOWN,
        deviceModel = metadata.device?.model ?: Constants.UNKNOWN
    )
}

fun AggregationResultGroupedByDuration.toActiveCaloriesBurned(): HealthDataDto? {
    return HealthDataDto(
        dataType = Constants.HEALTH_CONNECT_ACTIVE_CALORIES_BURNED,
        count = result[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inCalories?.toLong()
            ?: return null,
        unit = Constants.HEALTH_CONNECT_UNIT_CALORIES,
        source = result.dataOrigins.map { it.packageName }.toString(),
        startDateTime = timeManager.instantToIsoTime(startTime, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, zoneOffset),
    )
}

fun AggregationResultGroupedByDuration.toTotalCaloriesBurned(): HealthDataDto? {
    return HealthDataDto(
        dataType = Constants.HEALTH_CONNECT_TOTAL_CALORIES_BURNED,
        count = result[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inCalories?.toLong()
            ?: return null,
        unit = Constants.HEALTH_CONNECT_UNIT_CALORIES,
        source = result.dataOrigins.map { it.packageName }.toString(),
        startDateTime = timeManager.instantToIsoTime(startTime, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, zoneOffset),
    )
}

fun AggregationResultGroupedByDuration.toHeartRateAvgDto(): HealthDataDto {
    return HealthDataDto(
        dataType = Constants.HEALTH_CONNECT_HEART_RATE_AVG,
        count = result[HeartRateRecord.BPM_AVG] ?: -1,
        source = result.dataOrigins.map { it.packageName }.toString(),
        startDateTime = timeManager.instantToIsoTime(startTime, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, zoneOffset),
    )
}

fun AggregationResultGroupedByDuration.toHeartRateMinDto(): HealthDataDto {
    return HealthDataDto(
        dataType = Constants.HEALTH_CONNECT_HEART_RATE_MIN,
        count = result[HeartRateRecord.BPM_MIN] ?: -1,
        source = result.dataOrigins.map {
            it.packageName
        }.toString(),
        startDateTime = timeManager.instantToIsoTime(startTime, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, zoneOffset),
    )
}

fun AggregationResultGroupedByDuration.toHeartRateMaxDto(): HealthDataDto {
    return HealthDataDto(
        dataType = Constants.HEALTH_CONNECT_HEART_RATE_MAX,
        count = result[HeartRateRecord.BPM_MAX] ?: -1,
        source = result.dataOrigins.map {
            it.packageName
        }.toString(),
        startDateTime = timeManager.instantToIsoTime(startTime, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, zoneOffset),
    )
}

fun AggregationResultGroupedByDuration.toRestingHeartRateAvgDto(): HealthDataDto {
    return HealthDataDto(
        dataType = Constants.HEALTH_CONNECT_RESTING_HEART_RATE_AVG,
        count = result[RestingHeartRateRecord.BPM_AVG] ?: -1,
        source = result.dataOrigins.map {
            it.packageName
        }.toString(),
        startDateTime = timeManager.instantToIsoTime(startTime, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, zoneOffset),
    )
}

fun AggregationResultGroupedByDuration.toRestingHeartRateMinDto(): HealthDataDto {
    return HealthDataDto(
        dataType = Constants.HEALTH_CONNECT_RESTING_HEART_RATE_MIN,
        count = result[RestingHeartRateRecord.BPM_MIN] ?: -1,
        source = result.dataOrigins.map {
            it.packageName
        }.toString(),
        startDateTime = timeManager.instantToIsoTime(startTime, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, zoneOffset),
    )
}

fun AggregationResultGroupedByDuration.toRestingHeartRateMaxDto(): HealthDataDto {
    return HealthDataDto(
        dataType = Constants.HEALTH_CONNECT_RESTING_HEART_RATE_MAX,
        count = result[RestingHeartRateRecord.BPM_MAX] ?: -1,
        source = result.dataOrigins.map {
            it.packageName
        }.toString(),
        startDateTime = timeManager.instantToIsoTime(startTime, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, zoneOffset),
    )
}

fun HeartRateVariabilityRmssdRecord.toHeartRateDto(): HealthDataDto {
    return HealthDataDto(
        dataType = Constants.HEALTH_CONNECT_HEART_RATE_VARIABILITY_RMSSD,
        count = this.heartRateVariabilityMillis.toLong(),
        unit = Constants.HEALTH_CONNECT_UNIT_MILLISECONDS,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset),
        deviceManufacturer = metadata.device?.manufacturer ?: Constants.UNKNOWN,
        deviceModel = metadata.device?.model ?: Constants.UNKNOWN
    )
}

fun ActiveCaloriesBurnedRecord.toActiveCaloriesBurned(): HealthDataDto {
    return HealthDataDto(
        dataType = Constants.HEALTH_CONNECT_ACTIVE_CALORIES_BURNED,
        count = energy.inCalories.toLong(),
        unit = Constants.HEALTH_CONNECT_UNIT_CALORIES,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(startTime, startZoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, endZoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, endZoneOffset),
        deviceManufacturer = metadata.device?.manufacturer ?: Constants.UNKNOWN,
        deviceModel = metadata.device?.model ?: Constants.UNKNOWN
    )
}

fun BodyTemperatureRecord.toBodyTemperature(): HealthDataDto {
    return HealthDataDto(
        dataType = Constants.HEALTH_CONNECT_BODY_TEMPERATURE,
        count = temperature.inCelsius.toLong(),
        unit = Constants.HEALTH_CONNECT_UNIT_CELSIUS,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset),
        deviceManufacturer = metadata.device?.manufacturer ?: Constants.UNKNOWN,
        deviceModel = metadata.device?.model ?: Constants.UNKNOWN
    )
}

fun FloorsClimbedRecord.toFloorsClimbed(): HealthDataDto {
    return HealthDataDto(
        dataType = Constants.HEALTH_CONNECT_FLOORS_CLIMBED,
        count = floors.toLong(),
        unit = Constants.HEALTH_CONNECT_UNIT_FLOORS,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(startTime, startZoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, endZoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, endZoneOffset),
        deviceManufacturer = metadata.device?.manufacturer ?: Constants.UNKNOWN,
        deviceModel = metadata.device?.model ?: Constants.UNKNOWN
    )
}

fun OxygenSaturationRecord.toOxygenSaturation(): HealthDataDto {
    return HealthDataDto(
        dataType = Constants.HEALTH_CONNECT_OXYGEN_SATURATION,
        count = percentage.value.toLong(),
        unit = Constants.HEALTH_CONNECT_UNIT_PERCENTAGE,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset),
        deviceManufacturer = metadata.device?.manufacturer ?: Constants.UNKNOWN,
        deviceModel = metadata.device?.model ?: Constants.UNKNOWN
    )
}

fun TotalCaloriesBurnedRecord.toTotalCaloriesBurned(): HealthDataDto {
    return HealthDataDto(
        dataType = Constants.HEALTH_CONNECT_TOTAL_CALORIES_BURNED,
        count = energy.inCalories.toLong(),
        unit = Constants.HEALTH_CONNECT_UNIT_CALORIES,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(startTime, startZoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, endZoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, endZoneOffset),
        deviceManufacturer = metadata.device?.manufacturer ?: Constants.UNKNOWN,
        deviceModel = metadata.device?.model ?: Constants.UNKNOWN
    )
}

fun BasalBodyTemperatureRecord.toHealthDataDto(): HealthDataDto {
    return HealthDataDto(
        dataType = Constants.HEALTH_CONNECT_BASAL_BODY_TEMPERATURE,
        count = temperature.inCelsius.toLong(),
        unit = Constants.HEALTH_CONNECT_UNIT_CELSIUS,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset),
        deviceManufacturer = metadata.device?.manufacturer ?: Constants.UNKNOWN,
        deviceModel = metadata.device?.model ?: Constants.UNKNOWN
    )
}

fun BasalMetabolicRateRecord.toHealthDataDto(): HealthDataDto {
    return HealthDataDto(
        dataType = Constants.HEALTH_CONNECT_BASAL_METABOLIC_RATE,
        count = this.basalMetabolicRate.inKilocaloriesPerDay.toLong(), // TODO: decide on unit
        unit = Constants.HEALTH_CONNECT_UNIT_KCAL_PER_DAY,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset),
        deviceManufacturer = metadata.device?.manufacturer ?: Constants.UNKNOWN,
        deviceModel = metadata.device?.model ?: Constants.UNKNOWN
    )
}

fun BodyFatRecord.toHealthDataDto(): HealthDataDto {
    return HealthDataDto(
        dataType = Constants.HEALTH_CONNECT_BODY_FAT,
        count = percentage.value.toLong(),
        unit = Constants.HEALTH_CONNECT_UNIT_PERCENTAGE,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset),
        deviceManufacturer = metadata.device?.manufacturer ?: Constants.UNKNOWN,
        deviceModel = metadata.device?.model ?: Constants.UNKNOWN
    )
}

fun BodyWaterMassRecord.toHealthDataDto(): HealthDataDto {
    return HealthDataDto(
        dataType = Constants.HEALTH_CONNECT_BODY_WATER_MASS,
        count = mass.inGrams.toLong(),
        unit = Constants.HEALTH_CONNECT_UNIT_GRAMS,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset),
        deviceManufacturer = metadata.device?.manufacturer ?: Constants.UNKNOWN,
        deviceModel = metadata.device?.model ?: Constants.UNKNOWN
    )
}

fun LeanBodyMassRecord.toHealthDataDto(): HealthDataDto {
    return HealthDataDto(
        dataType = Constants.HEALTH_CONNECT_LEAN_BODY_MASS,
        count = mass.inGrams.toLong(),
        unit = Constants.HEALTH_CONNECT_UNIT_GRAMS,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset),
        deviceManufacturer = metadata.device?.manufacturer ?: Constants.UNKNOWN,
        deviceModel = metadata.device?.model ?: Constants.UNKNOWN
    )
}

fun HeightRecord.toHealthDataDto(): HealthDataDto {
    return HealthDataDto(
        dataType = Constants.HEALTH_CONNECT_HEIGHT,
        count = height.inInches.toLong(),
        unit = Constants.HEALTH_CONNECT_UNIT_INCHES,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset),
        deviceManufacturer = metadata.device?.manufacturer ?: Constants.UNKNOWN,
        deviceModel = metadata.device?.model ?: Constants.UNKNOWN
    )
}

fun WeightRecord.toHealthDataDto(): HealthDataDto {
    return HealthDataDto(
        dataType = Constants.HEALTH_CONNECT_WEIGHT,
        count = weight.inKilograms.toLong(),
        unit = Constants.HEALTH_CONNECT_UNIT_KILOGRAMS,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset),
        deviceManufacturer = metadata.device?.manufacturer ?: Constants.UNKNOWN,
        deviceModel = metadata.device?.model ?: Constants.UNKNOWN
    )
}

fun Vo2MaxRecord.toVo2Max(): Vo2MaxDto {
    return Vo2MaxDto(
        dataType = Constants.HEALTH_CONNECT_VO2_MAX,
        count = vo2MillilitersPerMinuteKilogram.toLong(),
        unit = Constants.HEALTH_CONNECT_UNIT_ML_PER_KG_PER_MIN,
        measurementMethod = mapper.measurementMethod(measurementMethod) ?: Constants.UNKNOWN,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset),
        deviceManufacturer = metadata.device?.manufacturer ?: Constants.UNKNOWN,
        deviceModel = metadata.device?.model ?: Constants.UNKNOWN
    )
}

fun RespiratoryRateRecord.toHealthDataDto(): HealthDataDto {
    return HealthDataDto(
        dataType = Constants.HEALTH_CONNECT_RESPIRATORY_RATE,
        count = rate.toLong(),
        unit = Constants.HEALTH_CONNECT_UNIT_BREATHS_PER_MIN,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset),
        deviceManufacturer = metadata.device?.manufacturer ?: Constants.UNKNOWN,
        deviceModel = metadata.device?.model ?: Constants.UNKNOWN
    )
}

fun StepsCadenceRecord.Sample.toHealthDataDto(
    metadata: Metadata,
    zoneOffset: ZoneOffset?
): HealthDataDto {
    return HealthDataDto(
        dataType = Constants.HEALTH_CONNECT_STEPS_CADENCE,
        count = rate.toLong(),
        unit = Constants.HEALTH_CONNECT_UNIT_STEPS_PER_MIN,
        source = metadata.dataOrigin.packageName,
        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        deviceType = mapper.devices(metadata.device?.type),
        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset),
        deviceManufacturer = metadata.device?.manufacturer ?: Constants.UNKNOWN,
        deviceModel = metadata.device?.model ?: Constants.UNKNOWN
    )
}

//fun ExerciseSessionRecord.TODO(): HealthDataDto {
//    return HealthDataDto(
//        dataType = Constants.HEALTH_CONNECT_STEPS_CADENCE,
//        count = this.,
//        unit = Constants.HEALTH_CONNECT_UNIT_STEPS_PER_MIN,
//        source = metadata.dataOrigin.packageName,
//        startDateTime = timeManager.instantToIsoTime(time, zoneOffset),
//        endDateTime = timeManager.instantToIsoTime(time, zoneOffset),
//        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
//        deviceType = mapper.devices(metadata.device?.type),
//        modifiedDateTime = timeManager.instantToIsoTime(metadata.lastModifiedTime, zoneOffset),
//        deviceManufacturer = metadata.device?.manufacturer ?: Constants.UNKNOWN,
//        deviceModel = metadata.device?.model ?: Constants.UNKNOWN
//    )
//}