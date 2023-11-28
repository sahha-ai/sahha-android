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
import sdk.sahha.android.domain.model.dto.BloodGlucoseDto
import sdk.sahha.android.domain.model.dto.BloodPressureDto
import sdk.sahha.android.domain.model.dto.HealthDataDto
import sdk.sahha.android.domain.model.dto.Vo2MaxDto
import sdk.sahha.android.domain.model.dto.send.SleepSendDto
import sdk.sahha.android.domain.model.steps.StepsHealthConnect
import sdk.sahha.android.source.Sahha

private val mapper = Sahha.di.healthConnectConstantsMapper
private val timeManager = Sahha.di.timeManager

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
        dataType = Constants.DataTypes.BLOOD_PRESSURE_DIASTOLIC,
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
        dataType = Constants.DataTypes.BLOOD_PRESSURE_SYSTOLIC,
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
        dataType = Constants.DataTypes.RESTING_HEART_RATE,
        recordingMethod = mapper.recordingMethod(metadata.recordingMethod),
        value = beatsPerMinute,
        unit = Constants.DataUnits.BEATS_PER_MIN,
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

fun AggregationResultGroupedByDuration.toActiveCaloriesBurned(): HealthDataDto {
    return HealthDataDto(
        dataType = Constants.DataTypes.ACTIVE_CALORIES_BURNED,
        value = result[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inCalories?.toLong()
            ?: 0,
        unit = Constants.DataUnits.CALORIES,
        source = result.dataOrigins.map { it.packageName }.toString(),
        startDateTime = timeManager.instantToIsoTime(startTime, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, zoneOffset),
    )
}

fun AggregationResultGroupedByDuration.toTotalCaloriesBurned(): HealthDataDto {
    return HealthDataDto(
        dataType = Constants.DataTypes.TOTAL_CALORIES_BURNED,
        value = result[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inCalories?.toLong()
            ?: 0,
        unit = Constants.DataUnits.CALORIES,
        source = result.dataOrigins.map { it.packageName }.toString(),
        startDateTime = timeManager.instantToIsoTime(startTime, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, zoneOffset),
    )
}

fun AggregationResultGroupedByDuration.toHeartRateAvgDto(): HealthDataDto {
    return HealthDataDto(
        dataType = Constants.DataTypes.HEART_RATE_AVG,
        value = result[HeartRateRecord.BPM_AVG] ?: -1,
        unit = Constants.DataUnits.BEATS_PER_MIN,
        source = result.dataOrigins.map { it.packageName }.toString(),
        startDateTime = timeManager.instantToIsoTime(startTime, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, zoneOffset),
    )
}

fun AggregationResultGroupedByDuration.toHeartRateMinDto(): HealthDataDto {
    return HealthDataDto(
        dataType = Constants.DataTypes.HEART_RATE_MIN,
        value = result[HeartRateRecord.BPM_MIN] ?: -1,
        unit = Constants.DataUnits.BEATS_PER_MIN,
        source = result.dataOrigins.map {
            it.packageName
        }.toString(),
        startDateTime = timeManager.instantToIsoTime(startTime, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, zoneOffset),
    )
}

fun AggregationResultGroupedByDuration.toHeartRateMaxDto(): HealthDataDto {
    return HealthDataDto(
        dataType = Constants.DataTypes.HEART_RATE_MAX,
        value = result[HeartRateRecord.BPM_MAX] ?: -1,
        unit = Constants.DataUnits.BEATS_PER_MIN,
        source = result.dataOrigins.map {
            it.packageName
        }.toString(),
        startDateTime = timeManager.instantToIsoTime(startTime, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, zoneOffset),
    )
}

fun AggregationResultGroupedByDuration.toRestingHeartRateAvgDto(): HealthDataDto {
    return HealthDataDto(
        dataType = Constants.DataTypes.RESTING_HEART_RATE_AVG,
        value = result[RestingHeartRateRecord.BPM_AVG] ?: -1,
        unit = Constants.DataUnits.BEATS_PER_MIN,
        source = result.dataOrigins.map {
            it.packageName
        }.toString(),
        startDateTime = timeManager.instantToIsoTime(startTime, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, zoneOffset),
    )
}

fun AggregationResultGroupedByDuration.toRestingHeartRateMinDto(): HealthDataDto {
    return HealthDataDto(
        dataType = Constants.DataTypes.RESTING_HEART_RATE_MIN,
        value = result[RestingHeartRateRecord.BPM_MIN] ?: -1,
        unit = Constants.DataUnits.BEATS_PER_MIN,
        source = result.dataOrigins.map {
            it.packageName
        }.toString(),
        startDateTime = timeManager.instantToIsoTime(startTime, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, zoneOffset),
    )
}

fun AggregationResultGroupedByDuration.toRestingHeartRateMaxDto(): HealthDataDto {
    return HealthDataDto(
        dataType = Constants.DataTypes.RESTING_HEART_RATE_MAX,
        value = result[RestingHeartRateRecord.BPM_MAX] ?: -1,
        unit = Constants.DataUnits.BEATS_PER_MIN,
        source = result.dataOrigins.map {
            it.packageName
        }.toString(),
        startDateTime = timeManager.instantToIsoTime(startTime, zoneOffset),
        endDateTime = timeManager.instantToIsoTime(endTime, zoneOffset),
    )
}

fun HeartRateVariabilityRmssdRecord.toHeartRateDto(): HealthDataDto {
    return HealthDataDto(
        dataType = Constants.DataTypes.HEART_RATE_VARIABILITY,
        value = this.heartRateVariabilityMillis.toLong(),
        unit = Constants.DataUnits.MILLISECONDS,
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
        dataType = Constants.DataTypes.ACTIVE_CALORIES_BURNED,
        value = energy.inCalories.toLong(),
        unit = Constants.DataUnits.CALORIES,
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
        dataType = Constants.DataTypes.OXYGEN_SATURATION,
        value = percentage.value.toLong(),
        unit = Constants.DataUnits.PERCENTAGE,
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
        dataType = Constants.DataTypes.TOTAL_CALORIES_BURNED,
        value = energy.inCalories.toLong(),
        unit = Constants.DataUnits.CALORIES,
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

fun BasalMetabolicRateRecord.toHealthDataDto(): HealthDataDto {
    return HealthDataDto(
        dataType = Constants.DataTypes.BASAL_METABOLIC_RATE,
        value = this.basalMetabolicRate.inKilocaloriesPerDay.toLong(), // TODO: decide on unit
        unit = Constants.DataUnits.KCAL_PER_DAY,
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
        dataType = Constants.DataTypes.BODY_FAT,
        value = percentage.value.toLong(),
        unit = Constants.DataUnits.PERCENTAGE,
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
        dataType = Constants.DataTypes.BODY_WATER_MASS,
        value = mass.inGrams.toLong(),
        unit = Constants.DataUnits.GRAMS,
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
        dataType = Constants.DataTypes.LEAN_BODY_MASS,
        value = mass.inGrams.toLong(),
        unit = Constants.DataUnits.GRAMS,
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
        dataType = Constants.DataTypes.HEIGHT,
        value = height.inMeters.toLong(),
        unit = Constants.DataUnits.METRES,
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
        dataType = Constants.DataTypes.WEIGHT,
        value = weight.inKilograms.toLong(),
        unit = Constants.DataUnits.KILOGRAMS,
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
        dataType = Constants.DataTypes.VO2_MAX,
        value = vo2MillilitersPerMinuteKilogram.toLong(),
        unit = Constants.DataUnits.ML_PER_KG_PER_MIN,
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
        dataType = Constants.DataTypes.RESPIRATORY_RATE,
        value = rate.toLong(),
        unit = Constants.DataUnits.BREATHS_PER_MIN,
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

fun BoneMassRecord.toHealthDataDto(): HealthDataDto {
    return HealthDataDto(
        dataType = Constants.DataTypes.BONE_MASS,
        value = mass.inGrams.toLong(),
        unit = Constants.DataUnits.GRAMS,
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