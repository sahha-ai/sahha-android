package sdk.sahha.android.data.provider

import androidx.health.connect.client.aggregate.AggregateMetric
import androidx.health.connect.client.aggregate.AggregationResult
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.BasalMetabolicRateRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.FloorsClimbedRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeightRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.time.TimeRangeFilter
import sdk.sahha.android.common.Constants
import sdk.sahha.android.data.mapper.toSahhaStat
import sdk.sahha.android.domain.model.stats.SahhaStat
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
        grantedCached ?: repository.getGrantedPermissions().also { granted ->
            grantedCached = granted
        }
    }
    private var grantedCached: Set<String>? = null

    override val permissionActions:
            Map<SahhaSensor, suspend (Duration, ZonedDateTime, ZonedDateTime) -> Pair<String?, List<SahhaStat>?>> =
        mapOf(
            SahhaSensor.step_count to createPermissionAction(
                sensor = SahhaSensor.step_count,
                recordClass = StepsRecord::class,
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                dataUnit = Constants.DataUnits.COUNT,
                extractValue = { result ->
                    result[StepsRecord.COUNT_TOTAL]?.toDouble() ?: 0.0
                }
            ),
            SahhaSensor.floor_count to createPermissionAction(
                sensor = SahhaSensor.floor_count,
                recordClass = FloorsClimbedRecord::class,
                metrics = setOf(FloorsClimbedRecord.FLOORS_CLIMBED_TOTAL),
                dataUnit = Constants.DataUnits.COUNT,
                extractValue = { result ->
                    result[StepsRecord.COUNT_TOTAL]?.toDouble() ?: 0.0
                }
            ),
            SahhaSensor.sleep to createPermissionAction(
                sensor = SahhaSensor.sleep,
                recordClass = SleepSessionRecord::class,
                metrics = setOf(SleepSessionRecord.SLEEP_DURATION_TOTAL),
                dataUnit = Constants.DataUnits.MINUTE,
                extractValue = { result ->
                    result[SleepSessionRecord.SLEEP_DURATION_TOTAL]
                        ?.toMillis()?.toDouble()?.div(1000)?.div(60)
                        ?: 0.0
                }
            ),
            SahhaSensor.active_energy_burned to createPermissionAction(
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
            SahhaSensor.basal_metabolic_rate to createPermissionAction(
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
            SahhaSensor.blood_pressure_diastolic to createPermissionAction(
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
            SahhaSensor.blood_pressure_systolic to createPermissionAction(
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
            SahhaSensor.weight to createPermissionAction(
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
            SahhaSensor.height to createPermissionAction(
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
            SahhaSensor.exercise to createPermissionAction(
                sensor = SahhaSensor.exercise,
                recordClass = ExerciseSessionRecord::class,
                metrics = setOf(ExerciseSessionRecord.EXERCISE_DURATION_TOTAL),
                dataUnit = Constants.DataUnits.SECOND,
                extractValue = { result ->
                    result[ExerciseSessionRecord.EXERCISE_DURATION_TOTAL]
                        ?.toMillis()
                        ?.toDouble()?.div(1000)
                        ?: 0.0
                }
            ),
            SahhaSensor.heart_rate to createPermissionAction(
                sensor = SahhaSensor.heart_rate,
                recordClass = HeartRateRecord::class,
                metrics = setOf(HeartRateRecord.BPM_AVG),
                dataUnit = Constants.DataUnits.BEAT_PER_MIN,
                extractValue = { result ->
                    result[HeartRateRecord.BPM_AVG]?.toDouble() ?: 0.0
                }
            ),
            SahhaSensor.resting_heart_rate to createPermissionAction(
                sensor = SahhaSensor.resting_heart_rate,
                recordClass = RestingHeartRateRecord::class,
                metrics = setOf(RestingHeartRateRecord.BPM_AVG),
                dataUnit = Constants.DataUnits.BEAT_PER_MIN,
                extractValue = { result ->
                    result[RestingHeartRateRecord.BPM_AVG]?.toDouble() ?: 0.0
                }
            ),
            SahhaSensor.total_energy_burned to createPermissionAction(
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
        )

    private fun <T : Any, R : Record> createPermissionAction(
        sensor: SahhaSensor,
        recordClass: KClass<R>,
        metrics: Set<AggregateMetric<T>>,
        dataUnit: String,
        extractValue: (AggregationResult) -> Double
    ): suspend (Duration, ZonedDateTime, ZonedDateTime) -> Pair<String?, List<SahhaStat>?> {
        return { duration, start, end ->
            val permissionGranted = grantedPermissions().contains(
                HealthPermission.getReadPermission(recordClass)
            )
            if (permissionGranted) {
                val aggregates = repository.getAggregateRecordsByDuration(
                    metrics = metrics,
                    timeRangeFilter = TimeRangeFilter.between(
                        startTime = start.toLocalDateTime(),
                        endTime = end.toLocalDateTime()
                    ),
                    interval = duration
                )

                val stats = aggregates?.map { stat ->
                    stat.toSahhaStat(
                        sensor,
                        extractValue(stat.result),
                        dataUnit,
                        stat.result.dataOrigins.map { it.packageName }
                    )
                }

                Pair(null, stats)
            } else Pair("Error: HealthConnect permission for this sensor was not granted", null)
        }
    }

}