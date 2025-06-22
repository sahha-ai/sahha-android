package sdk.sahha.android.data.provider

import androidx.health.connect.client.aggregate.AggregateMetric
import androidx.health.connect.client.aggregate.AggregationResult
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.BasalBodyTemperatureRecord
import androidx.health.connect.client.records.BasalMetabolicRateRecord
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.BodyTemperatureRecord
import androidx.health.connect.client.records.BodyWaterMassRecord
import androidx.health.connect.client.records.BoneMassRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.FloorsClimbedRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.HeightRecord
import androidx.health.connect.client.records.LeanBodyMassRecord
import androidx.health.connect.client.records.NutritionRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.RespiratoryRateRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.Vo2MaxRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.time.TimeRangeFilter
import sdk.sahha.android.common.Constants
import sdk.sahha.android.common.toSpecificHour
import sdk.sahha.android.data.mapper.toBloodPressureDiastolic
import sdk.sahha.android.data.mapper.toBloodPressureSystolic
import sdk.sahha.android.data.mapper.toSahhaDataLog
import sdk.sahha.android.data.mapper.toSahhaDataLogAsParentLog
import sdk.sahha.android.data.mapper.toSahhaDataLogDto
import sdk.sahha.android.data.mapper.toSahhaLogDto
import sdk.sahha.android.data.mapper.toSahhaSample
import sdk.sahha.android.data.mapper.toSahhaStat
import sdk.sahha.android.data.mapper.toStepsHealthConnect
import sdk.sahha.android.domain.internal_enum.AggregationType
import sdk.sahha.android.domain.mapper.category
import sdk.sahha.android.domain.model.data_log.SahhaDataLog
import sdk.sahha.android.domain.model.local_logs.SahhaSample
import sdk.sahha.android.domain.model.local_logs.SahhaStat
import sdk.sahha.android.domain.provider.PermissionActionProvider
import sdk.sahha.android.domain.repository.HealthConnectRepo
import sdk.sahha.android.source.SahhaSensor
import java.time.Duration
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlin.reflect.KClass

internal class PermissionActionProviderImpl @Inject constructor(
    private val repository: HealthConnectRepo
) : PermissionActionProvider {
    private val grantedPermissions = suspend {
        repository.getGrantedPermissions()
    }

    override val permissionActionsStats:
            Map<SahhaSensor, suspend (Duration, ZonedDateTime, ZonedDateTime) -> Pair<String?, List<SahhaStat>?>> =
        mapOf(
            SahhaSensor.steps to createPermissionActionStats(
                sensor = SahhaSensor.steps,
                recordClass = StepsRecord::class,
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                dataUnit = Constants.DataUnits.COUNT,
                extractValue = { result ->
                    result[StepsRecord.COUNT_TOTAL]?.toDouble() ?: 0.0
                }
            ),
            SahhaSensor.floors_climbed to createPermissionActionStats(
                sensor = SahhaSensor.floors_climbed,
                recordClass = FloorsClimbedRecord::class,
                metrics = setOf(FloorsClimbedRecord.FLOORS_CLIMBED_TOTAL),
                dataUnit = Constants.DataUnits.COUNT,
                extractValue = { result ->
                    result[StepsRecord.COUNT_TOTAL]?.toDouble() ?: 0.0
                }
            ),
            SahhaSensor.sleep to createPermissionActionStats(
                sensor = SahhaSensor.sleep,
                recordClass = SleepSessionRecord::class,
                metrics = setOf(SleepSessionRecord.SLEEP_DURATION_TOTAL),
                dataUnit = Constants.DataUnits.MINUTE,
                extractValue = { result ->
                    result[SleepSessionRecord.SLEEP_DURATION_TOTAL]
                        ?.toMillis()?.toDouble()?.div(1000)?.div(60)
                        ?: 0.0
                },
                sliceFrom6pm = true
            ),
            SahhaSensor.active_energy_burned to createPermissionActionStats(
                sensor = SahhaSensor.active_energy_burned,
                recordClass = ActiveCaloriesBurnedRecord::class,
                metrics = setOf(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL),
                dataUnit = Constants.DataUnits.KILOCALORIE,
                extractValue = { result ->
                    result[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]
                        ?.inKilocalories
                        ?: 0.0
                }
            ),
            SahhaSensor.basal_metabolic_rate to createPermissionActionStats(
                sensor = SahhaSensor.basal_metabolic_rate,
                recordClass = BasalMetabolicRateRecord::class,
                metrics = setOf(BasalMetabolicRateRecord.BASAL_CALORIES_TOTAL),
                dataUnit = Constants.DataUnits.KILOCALORIE,
                extractValue = { result ->
                    result[BasalMetabolicRateRecord.BASAL_CALORIES_TOTAL]
                        ?.inKilocalories
                        ?: 0.0
                }
            ),
            SahhaSensor.blood_pressure_diastolic to createPermissionActionStats(
                sensor = SahhaSensor.blood_pressure_diastolic,
                recordClass = BloodPressureRecord::class,
                metrics = setOf(BloodPressureRecord.DIASTOLIC_AVG),
                dataUnit = Constants.DataUnits.MMHG,
                extractValue = { result ->
                    result[BloodPressureRecord.DIASTOLIC_AVG]
                        ?.inMillimetersOfMercury
                        ?: 0.0
                }
            ),
            SahhaSensor.blood_pressure_systolic to createPermissionActionStats(
                sensor = SahhaSensor.blood_pressure_systolic,
                recordClass = BloodPressureRecord::class,
                metrics = setOf(BloodPressureRecord.SYSTOLIC_AVG),
                dataUnit = Constants.DataUnits.MMHG,
                extractValue = { result ->
                    result[BloodPressureRecord.SYSTOLIC_AVG]
                        ?.inMillimetersOfMercury
                        ?: 0.0
                }
            ),
            SahhaSensor.weight to createPermissionActionStats(
                sensor = SahhaSensor.weight,
                recordClass = WeightRecord::class,
                metrics = setOf(WeightRecord.WEIGHT_AVG),
                dataUnit = Constants.DataUnits.KILOGRAM,
                extractValue = { result ->
                    result[WeightRecord.WEIGHT_AVG]
                        ?.inKilograms
                        ?: 0.0
                }
            ),
            SahhaSensor.height to createPermissionActionStats(
                sensor = SahhaSensor.height,
                recordClass = HeightRecord::class,
                metrics = setOf(HeightRecord.HEIGHT_AVG),
                dataUnit = Constants.DataUnits.METRE,
                extractValue = { result ->
                    result[HeightRecord.HEIGHT_AVG]
                        ?.inMeters
                        ?: 0.0
                }
            ),
            SahhaSensor.exercise to createPermissionActionStats(
                sensor = SahhaSensor.exercise,
                recordClass = ExerciseSessionRecord::class,
                metrics = setOf(ExerciseSessionRecord.EXERCISE_DURATION_TOTAL),
                dataUnit = Constants.DataUnits.MINUTE,
                extractValue = { result ->
                    result[ExerciseSessionRecord.EXERCISE_DURATION_TOTAL]
                        ?.toMillis()
                        ?.toDouble()?.div(1000)?.div(60)
                        ?: 0.0
                }
            ),
            SahhaSensor.heart_rate to createPermissionActionStats(
                sensor = SahhaSensor.heart_rate,
                recordClass = HeartRateRecord::class,
                metrics = setOf(HeartRateRecord.BPM_AVG),
                dataUnit = Constants.DataUnits.BEAT_PER_MIN,
                extractValue = { result ->
                    result[HeartRateRecord.BPM_AVG]?.toDouble() ?: 0.0
                }
            ),
            SahhaSensor.resting_heart_rate to createPermissionActionStats(
                sensor = SahhaSensor.resting_heart_rate,
                recordClass = RestingHeartRateRecord::class,
                metrics = setOf(RestingHeartRateRecord.BPM_AVG),
                dataUnit = Constants.DataUnits.BEAT_PER_MIN,
                extractValue = { result ->
                    result[RestingHeartRateRecord.BPM_AVG]?.toDouble() ?: 0.0
                }
            ),
            SahhaSensor.total_energy_burned to createPermissionActionStats(
                sensor = SahhaSensor.total_energy_burned,
                recordClass = TotalCaloriesBurnedRecord::class,
                metrics = setOf(TotalCaloriesBurnedRecord.ENERGY_TOTAL),
                dataUnit = Constants.DataUnits.KILOCALORIE,
                extractValue = { result ->
                    result[TotalCaloriesBurnedRecord.ENERGY_TOTAL]
                        ?.inKilocalories
                        ?: 0.0
                }
            ),
            SahhaSensor.energy_consumed to createPermissionActionStats(
                sensor = SahhaSensor.energy_consumed,
                recordClass = NutritionRecord::class,
                metrics = setOf(NutritionRecord.ENERGY_TOTAL),
                dataUnit = Constants.DataUnits.KILOCALORIE,
                extractValue = { result ->
                    result[NutritionRecord.ENERGY_TOTAL]
                        ?.inKilocalories
                        ?: 0.0
                }
            )
        )

    override val permissionActionsSamples: Map<SahhaSensor, suspend (ZonedDateTime, ZonedDateTime) -> Pair<String?, List<SahhaSample>?>> =
        mapOf(
            SahhaSensor.sleep to createPermissionActionSamples(
                recordClass = SleepSessionRecord::class,
                extractSample = { record ->
                    val sleepRecord = record as SleepSessionRecord
                    val sessionSample = sleepRecord
                        .toSahhaDataLogDto()
                        .toSahhaSample(SahhaSensor.sleep.category)
                    val stageSamples = sleepRecord
                        .stages
                        .map {
                            it.toSahhaDataLog(record)
                                .toSahhaSample(SahhaSensor.sleep.category)
                        }
                    listOf(sessionSample) + stageSamples
                }
            ),
            SahhaSensor.steps to createPermissionActionSamples(
                recordClass = StepsRecord::class,
                extractSample = { record ->
                    val sample = (record as StepsRecord)
                        .toStepsHealthConnect()
                        .toSahhaDataLogAsParentLog()
                        .toSahhaSample(SahhaSensor.steps.category)
                    listOf(sample)
                }
            ),
            SahhaSensor.floors_climbed to createPermissionActionSamples(
                recordClass = FloorsClimbedRecord::class,
                extractSample = { record ->
                    val sample = (record as FloorsClimbedRecord)
                        .toSahhaDataLogDto()
                        .toSahhaSample(SahhaSensor.floors_climbed.category)
                    listOf(sample)
                }
            ),
            SahhaSensor.heart_rate to createPermissionActionSamples(
                recordClass = HeartRateRecord::class,
                extractSample = { record ->
                    (record as HeartRateRecord)
                        .samples.map { heartSample ->
                            heartSample
                                .toSahhaDataLog(record)
                                .toSahhaSample(SahhaSensor.heart_rate.category)
                        }
                }
            ),
            SahhaSensor.resting_heart_rate to createPermissionActionSamples(
                recordClass = RestingHeartRateRecord::class,
                extractSample = { record ->
                    val sample = (record as RestingHeartRateRecord)
                        .toSahhaLogDto()
                        .toSahhaSample(SahhaSensor.resting_heart_rate.category)
                    listOf(sample)
                }
            ),
            SahhaSensor.heart_rate_variability_rmssd to createPermissionActionSamples(
                recordClass = HeartRateVariabilityRmssdRecord::class,
                extractSample = { record ->
                    val sample = (record as HeartRateVariabilityRmssdRecord)
                        .toSahhaDataLogDto()
                        .toSahhaSample(SahhaSensor.heart_rate_variability_rmssd.category)
                    listOf(sample)
                }
            ),
            SahhaSensor.blood_pressure_systolic to createPermissionActionSamples(
                recordClass = BloodPressureRecord::class,
                extractSample = { record ->
                    val sample = (record as BloodPressureRecord)
                        .toBloodPressureSystolic()
                        .toSahhaSample(SahhaSensor.blood_pressure_systolic.category)
                    listOf(sample)
                }
            ),
            SahhaSensor.blood_pressure_diastolic to createPermissionActionSamples(
                recordClass = BloodPressureRecord::class,
                extractSample = { record ->
                    val sample = (record as BloodPressureRecord)
                        .toBloodPressureDiastolic()
                        .toSahhaSample(SahhaSensor.blood_pressure_diastolic.category)
                    listOf(sample)
                }
            ),
            SahhaSensor.blood_glucose to createPermissionActionSamples(
                recordClass = BloodGlucoseRecord::class,
                extractSample = { record ->
                    val sample = (record as BloodGlucoseRecord)
                        .toSahhaDataLogDto()
                        .toSahhaSample(SahhaSensor.blood_glucose.category)
                    listOf(sample)
                }
            ),
            SahhaSensor.vo2_max to createPermissionActionSamples(
                recordClass = Vo2MaxRecord::class,
                extractSample = { record ->
                    val sample = (record as Vo2MaxRecord)
                        .toSahhaDataLogDto()
                        .toSahhaSample(SahhaSensor.vo2_max.category)
                    listOf(sample)
                }
            ),
            SahhaSensor.oxygen_saturation to createPermissionActionSamples(
                recordClass = OxygenSaturationRecord::class,
                extractSample = { record ->
                    val sample = (record as OxygenSaturationRecord)
                        .toSahhaDataLogDto()
                        .toSahhaSample(SahhaSensor.oxygen_saturation.category)
                    listOf(sample)
                }
            ),
            SahhaSensor.respiratory_rate to createPermissionActionSamples(
                recordClass = RespiratoryRateRecord::class,
                extractSample = { record ->
                    val sample = (record as RespiratoryRateRecord)
                        .toSahhaDataLogDto()
                        .toSahhaSample(SahhaSensor.respiratory_rate.category)
                    listOf(sample)
                }
            ),
            SahhaSensor.active_energy_burned to createPermissionActionSamples(
                recordClass = ActiveCaloriesBurnedRecord::class,
                extractSample = { record ->
                    val sample = (record as ActiveCaloriesBurnedRecord)
                        .toSahhaDataLogDto()
                        .toSahhaSample(SahhaSensor.active_energy_burned.category)
                    listOf(sample)
                }
            ),
            SahhaSensor.total_energy_burned to createPermissionActionSamples(
                recordClass = TotalCaloriesBurnedRecord::class,
                extractSample = { record ->
                    val sample = (record as TotalCaloriesBurnedRecord)
                        .toSahhaDataLogDto()
                        .toSahhaSample(SahhaSensor.total_energy_burned.category)
                    listOf(sample)
                }
            ),
            SahhaSensor.basal_metabolic_rate to createPermissionActionSamples(
                recordClass = BasalMetabolicRateRecord::class,
                extractSample = { record ->
                    val sample = (record as BasalMetabolicRateRecord)
                        .toSahhaDataLogDto()
                        .toSahhaSample(SahhaSensor.basal_metabolic_rate.category)
                    listOf(sample)
                }
            ),
            SahhaSensor.body_temperature to createPermissionActionSamples(
                recordClass = BodyTemperatureRecord::class,
                extractSample = { record ->
                    val sample = (record as BodyTemperatureRecord)
                        .toSahhaDataLogDto()
                        .toSahhaSample(SahhaSensor.body_temperature.category)
                    listOf(sample)
                }
            ),
            SahhaSensor.basal_body_temperature to createPermissionActionSamples(
                recordClass = BasalBodyTemperatureRecord::class,
                extractSample = { record ->
                    val sample = (record as BasalBodyTemperatureRecord)
                        .toSahhaDataLogDto()
                        .toSahhaSample(SahhaSensor.basal_body_temperature.category)
                    listOf(sample)
                }
            ),
            SahhaSensor.height to createPermissionActionSamples(
                recordClass = HeightRecord::class,
                extractSample = { record ->
                    val sample = (record as HeightRecord)
                        .toSahhaDataLogDto()
                        .toSahhaSample(SahhaSensor.height.category)
                    listOf(sample)
                }
            ),
            SahhaSensor.weight to createPermissionActionSamples(
                recordClass = WeightRecord::class,
                extractSample = { record ->
                    val sample = (record as WeightRecord)
                        .toSahhaDataLogDto()
                        .toSahhaSample(SahhaSensor.weight.category)
                    listOf(sample)
                }
            ),
            SahhaSensor.lean_body_mass to createPermissionActionSamples(
                recordClass = LeanBodyMassRecord::class,
                extractSample = { record ->
                    val sample = (record as LeanBodyMassRecord)
                        .toSahhaDataLogDto()
                        .toSahhaSample(SahhaSensor.lean_body_mass.category)
                    listOf(sample)
                }
            ),
            SahhaSensor.body_fat to createPermissionActionSamples(
                recordClass = BodyFatRecord::class,
                extractSample = { record ->
                    val sample = (record as BodyFatRecord)
                        .toSahhaDataLogDto()
                        .toSahhaSample(SahhaSensor.body_fat.category)
                    listOf(sample)
                }
            ),
            SahhaSensor.body_water_mass to createPermissionActionSamples(
                recordClass = BodyWaterMassRecord::class,
                extractSample = { record ->
                    val sample = (record as BodyWaterMassRecord)
                        .toSahhaDataLogDto()
                        .toSahhaSample(SahhaSensor.body_water_mass.category)
                    listOf(sample)
                }
            ),
            SahhaSensor.bone_mass to createPermissionActionSamples(
                recordClass = BoneMassRecord::class,
                extractSample = { record ->
                    val sample = (record as BoneMassRecord)
                        .toSahhaDataLogDto()
                        .toSahhaSample(SahhaSensor.bone_mass.category)
                    listOf(sample)
                }
            ),
            SahhaSensor.exercise to createPermissionActionSamples(
                recordClass = ExerciseSessionRecord::class,
                extractSample = { record ->
                    val exerciseSession = record as ExerciseSessionRecord
                    val sessions = exerciseSession
                        .toSahhaDataLogDto()
                        .toSahhaSample(SahhaSensor.exercise.category)
                    val segments = exerciseSession.segments.map {
                        it.toSahhaDataLogDto(record)
                            .toSahhaSample(SahhaSensor.exercise.category)
                    }
                    val laps = exerciseSession.laps.map {
                        it.toSahhaDataLogDto(record)
                            .toSahhaSample(SahhaSensor.exercise.category)
                    }
                    listOf(sessions) + segments + laps
                }
            ),
            SahhaSensor.energy_consumed to createPermissionActionSamples(
                recordClass = NutritionRecord::class,
                extractSample = { record ->
                    val sample = (record as NutritionRecord)
                        .toSahhaDataLogDto()
                        .toSahhaSample(SahhaSensor.energy_consumed.category)
                    listOf(sample)
                }
            )
        )

    override val permissionActionsLogs:
            Map<SahhaSensor, suspend (Duration, ZonedDateTime, ZonedDateTime, String, String) -> Pair<String?, List<SahhaDataLog>?>> =
        mapOf(
            SahhaSensor.steps to createPermissionActionLogs(
                sensor = SahhaSensor.steps,
                recordClass = StepsRecord::class,
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                dataUnit = Constants.DataUnits.COUNT,
                aggregation = AggregationType.SUM.value,
                extractValue = { result ->
                    result[StepsRecord.COUNT_TOTAL]?.toDouble() ?: 0.0
                }
            ),
            SahhaSensor.floors_climbed to createPermissionActionLogs(
                sensor = SahhaSensor.floors_climbed,
                recordClass = FloorsClimbedRecord::class,
                metrics = setOf(FloorsClimbedRecord.FLOORS_CLIMBED_TOTAL),
                dataUnit = Constants.DataUnits.COUNT,
                aggregation = AggregationType.SUM.value,
                extractValue = { result ->
                    result[StepsRecord.COUNT_TOTAL]?.toDouble() ?: 0.0
                }
            ),
            SahhaSensor.sleep to createPermissionActionLogs(
                sensor = SahhaSensor.sleep,
                recordClass = SleepSessionRecord::class,
                metrics = setOf(SleepSessionRecord.SLEEP_DURATION_TOTAL),
                dataUnit = Constants.DataUnits.MINUTE,
                aggregation = AggregationType.SUM.value,
                extractValue = { result ->
                    result[SleepSessionRecord.SLEEP_DURATION_TOTAL]
                        ?.toMillis()?.toDouble()?.div(1000)?.div(60)
                        ?: 0.0
                },
            ),
            SahhaSensor.active_energy_burned to createPermissionActionLogs(
                sensor = SahhaSensor.active_energy_burned,
                recordClass = ActiveCaloriesBurnedRecord::class,
                metrics = setOf(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL),
                dataUnit = Constants.DataUnits.KILOCALORIE,
                aggregation = AggregationType.SUM.value,
                extractValue = { result ->
                    result[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]
                        ?.inKilocalories
                        ?: 0.0
                }
            ),
            SahhaSensor.basal_metabolic_rate to createPermissionActionLogs(
                sensor = SahhaSensor.basal_metabolic_rate,
                recordClass = BasalMetabolicRateRecord::class,
                metrics = setOf(BasalMetabolicRateRecord.BASAL_CALORIES_TOTAL),
                dataUnit = Constants.DataUnits.KILOCALORIE,
                aggregation = AggregationType.SUM.value,
                extractValue = { result ->
                    result[BasalMetabolicRateRecord.BASAL_CALORIES_TOTAL]
                        ?.inKilocalories
                        ?: 0.0
                }
            ),
            SahhaSensor.blood_pressure_diastolic to createPermissionActionLogs(
                sensor = SahhaSensor.blood_pressure_diastolic,
                recordClass = BloodPressureRecord::class,
                metrics = setOf(BloodPressureRecord.DIASTOLIC_AVG),
                dataUnit = Constants.DataUnits.MMHG,
                aggregation = AggregationType.AVG.value,
                extractValue = { result ->
                    result[BloodPressureRecord.DIASTOLIC_AVG]
                        ?.inMillimetersOfMercury
                        ?: 0.0
                }
            ),
            SahhaSensor.blood_pressure_systolic to createPermissionActionLogs(
                sensor = SahhaSensor.blood_pressure_systolic,
                recordClass = BloodPressureRecord::class,
                metrics = setOf(BloodPressureRecord.SYSTOLIC_AVG),
                dataUnit = Constants.DataUnits.MMHG,
                aggregation = AggregationType.AVG.value,
                extractValue = { result ->
                    result[BloodPressureRecord.SYSTOLIC_AVG]
                        ?.inMillimetersOfMercury
                        ?: 0.0
                }
            ),
            SahhaSensor.weight to createPermissionActionLogs(
                sensor = SahhaSensor.weight,
                recordClass = WeightRecord::class,
                metrics = setOf(WeightRecord.WEIGHT_AVG),
                dataUnit = Constants.DataUnits.KILOGRAM,
                aggregation = AggregationType.AVG.value,
                extractValue = { result ->
                    result[WeightRecord.WEIGHT_AVG]
                        ?.inKilograms
                        ?: 0.0
                }
            ),
            SahhaSensor.height to createPermissionActionLogs(
                sensor = SahhaSensor.height,
                recordClass = HeightRecord::class,
                metrics = setOf(HeightRecord.HEIGHT_AVG),
                dataUnit = Constants.DataUnits.METRE,
                aggregation = AggregationType.AVG.value,
                extractValue = { result ->
                    result[HeightRecord.HEIGHT_AVG]
                        ?.inMeters
                        ?: 0.0
                }
            ),
            SahhaSensor.exercise to createPermissionActionLogs(
                sensor = SahhaSensor.exercise,
                recordClass = ExerciseSessionRecord::class,
                metrics = setOf(ExerciseSessionRecord.EXERCISE_DURATION_TOTAL),
                dataUnit = Constants.DataUnits.MINUTE,
                aggregation = AggregationType.SUM.value,
                extractValue = { result ->
                    result[ExerciseSessionRecord.EXERCISE_DURATION_TOTAL]
                        ?.toMillis()
                        ?.toDouble()?.div(1000)?.div(60)
                        ?: 0.0
                }
            ),
            SahhaSensor.heart_rate to createPermissionActionLogs(
                sensor = SahhaSensor.heart_rate,
                recordClass = HeartRateRecord::class,
                metrics = setOf(HeartRateRecord.BPM_AVG),
                dataUnit = Constants.DataUnits.BEAT_PER_MIN,
                aggregation = AggregationType.AVG.value,
                extractValue = { result ->
                    result[HeartRateRecord.BPM_AVG]?.toDouble() ?: 0.0
                }
            ),
            SahhaSensor.resting_heart_rate to createPermissionActionLogs(
                sensor = SahhaSensor.resting_heart_rate,
                recordClass = RestingHeartRateRecord::class,
                metrics = setOf(RestingHeartRateRecord.BPM_AVG),
                dataUnit = Constants.DataUnits.BEAT_PER_MIN,
                aggregation = AggregationType.AVG.value,
                extractValue = { result ->
                    result[RestingHeartRateRecord.BPM_AVG]?.toDouble() ?: 0.0
                }
            ),
            SahhaSensor.total_energy_burned to createPermissionActionLogs(
                sensor = SahhaSensor.total_energy_burned,
                recordClass = TotalCaloriesBurnedRecord::class,
                metrics = setOf(TotalCaloriesBurnedRecord.ENERGY_TOTAL),
                dataUnit = Constants.DataUnits.KILOCALORIE,
                aggregation = AggregationType.AVG.value,
                extractValue = { result ->
                    result[TotalCaloriesBurnedRecord.ENERGY_TOTAL]
                        ?.inKilocalories
                        ?: 0.0
                }
            ),
            SahhaSensor.energy_consumed to createPermissionActionLogs(
                sensor = SahhaSensor.energy_consumed,
                recordClass = NutritionRecord::class,
                metrics = setOf(NutritionRecord.ENERGY_TOTAL),
                dataUnit = Constants.DataUnits.KILOCALORIE,
                aggregation = AggregationType.AVG.value,
                extractValue = { result ->
                    result[NutritionRecord.ENERGY_TOTAL]
                        ?.inKilocalories
                        ?: 0.0
                }
            )
        )


    private fun <T : Any, R : Record> createPermissionActionStats(
        sensor: SahhaSensor,
        recordClass: KClass<R>,
        metrics: Set<AggregateMetric<T>>,
        dataUnit: String,
        extractValue: (AggregationResult) -> Double,
        sliceFrom6pm: Boolean = false
    ): suspend (Duration, ZonedDateTime, ZonedDateTime) -> Pair<String?, List<SahhaStat>?> {
        return { duration, start, end ->
            val permissionGranted = grantedPermissions().contains(
                HealthPermission.getReadPermission(recordClass)
            )
            println(permissionGranted)
            if (permissionGranted) {
                try {
                    val aggregates = repository.getAggregateRecordsByDuration(
                        metrics = metrics,
                        timeRangeFilter = TimeRangeFilter.between(
                            startTime = if (sliceFrom6pm) start.toSpecificHour(18, -1)
                                .toLocalDateTime()
                            else start.toLocalDateTime(),
                            endTime = if (sliceFrom6pm) end.toSpecificHour(18, -1)
                                .toLocalDateTime()
                            else end.toLocalDateTime()
                        ),
                        interval = duration
                    )

                    val stats = aggregates?.map { stat ->
                        stat.toSahhaStat(
                            category = sensor.category,
                            sensor = sensor,
                            value = extractValue(stat.result),
                            unit = dataUnit,
                            sources = stat.result.dataOrigins.map { it.packageName }
                        )
                    }

                    Pair(null, stats)
                } catch (e: Exception) {
                    Pair(e.message, null)
                }
            } else Pair("Error: HealthConnect permission for this sensor was not granted", null)
        }
    }

    private fun <R : Record> createPermissionActionSamples(
        recordClass: KClass<R>,
        extractSample: (Record) -> List<SahhaSample>
    ): suspend (ZonedDateTime, ZonedDateTime) -> Pair<String?, List<SahhaSample>?> {
        return { start, end ->
            val permissionGranted = grantedPermissions().contains(
                HealthPermission.getReadPermission(recordClass)
            )
            if (permissionGranted) {
                try {
                    val records = repository.getRecords(
                        recordClass,
                        TimeRangeFilter.Companion.between(
                            start.toLocalDateTime(),
                            end.toLocalDateTime()
                        )
                    )

                    val samples = records?.map { sample ->
                        extractSample(sample)
                    }

                    val flattenedSamples = samples?.flatten()

                    Pair(null, flattenedSamples)
                } catch (e: Exception) {
                    Pair(e.message, null)
                }
            } else Pair("Error: HealthConnect permission for this sensor was not granted", null)
        }
    }

    private fun <T : Any, R : Record> createPermissionActionLogs(
        sensor: SahhaSensor,
        recordClass: KClass<R>,
        metrics: Set<AggregateMetric<T>>,
        dataUnit: String,
        aggregation: String,
        extractValue: (AggregationResult) -> Double,
    ): suspend (
        duration: Duration,
        start: ZonedDateTime,
        end: ZonedDateTime,
        postDateTime: String,
        periodicity: String
    ) -> Pair<String?, List<SahhaDataLog>?> {
        return { duration, start, end, postDateTime, periodicity ->
            val permissionGranted = grantedPermissions().contains(
                HealthPermission.getReadPermission(recordClass)
            )
            if (permissionGranted) {
                try {
                    val aggregates = repository.getAggregateRecordsByDuration(
                        metrics = metrics,
                        timeRangeFilter = TimeRangeFilter.between(
                            startTime = start.toLocalDateTime(),
                            endTime = end.toLocalDateTime()
                        ),
                        interval = duration
                    )

                    val logs = aggregates?.map { aggregate ->
                        aggregate.toSahhaDataLog(
                            category = sensor.category,
                            sensor = sensor,
                            value = extractValue(aggregate.result),
                            unit = dataUnit,
                            periodicity = periodicity,
                            aggregation = aggregation,
                            postDateTime = postDateTime
                        )
                    }?.filter { it.value > 0.0 }

                    Pair(null, logs)
                } catch (e: Exception) {
                    Pair(e.message, null)
                }
            } else Pair("Error: HealthConnect permission for this sensor was not granted", null)
        }
    }
}