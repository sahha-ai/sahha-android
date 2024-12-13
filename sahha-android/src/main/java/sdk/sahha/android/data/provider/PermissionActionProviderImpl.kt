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
import sdk.sahha.android.common.toNoon
import sdk.sahha.android.data.mapper.toSahhaDataLogDto
import sdk.sahha.android.data.mapper.toSahhaLogEvent
import sdk.sahha.android.data.mapper.toSahhaStat
import sdk.sahha.android.data.mapper.toStepsHealthConnect
import sdk.sahha.android.domain.model.local_logs.SahhaLogEvent
import sdk.sahha.android.domain.model.local_logs.SahhaStat
import sdk.sahha.android.domain.model.steps.toSahhaDataLogAsParentLog
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
            SahhaSensor.floor_count to createPermissionActionStats(
                sensor = SahhaSensor.floor_count,
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
                sliceFromNoon = true
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
                dataUnit = Constants.DataUnits.SECOND,
                extractValue = { result ->
                    result[ExerciseSessionRecord.EXERCISE_DURATION_TOTAL]
                        ?.toMillis()
                        ?.toDouble()?.div(1000)
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
        )

    override val permissionActionsEvents: Map<SahhaSensor, suspend (ZonedDateTime, ZonedDateTime) -> Pair<String?, List<SahhaLogEvent>?>> =
        mapOf(
            SahhaSensor.sleep to createPermissionActionEvents(
                recordClass = SleepSessionRecord::class,
                extractEvent = { record ->
                    (record as SleepSessionRecord)
                        .toSahhaDataLogDto()
                        .toSahhaLogEvent()
                }
            ),
            SahhaSensor.steps to createPermissionActionEvents(
                recordClass = StepsRecord::class,
                extractEvent = { record ->
                    (record as StepsRecord)
                        .toStepsHealthConnect()
                        .toSahhaDataLogAsParentLog()
                        .toSahhaLogEvent()
                }
            )
            // TODO: Add rest of the sensors in the future
        )

    private fun <T : Any, R : Record> createPermissionActionStats(
        sensor: SahhaSensor,
        recordClass: KClass<R>,
        metrics: Set<AggregateMetric<T>>,
        dataUnit: String,
        extractValue: (AggregationResult) -> Double,
        sliceFromNoon: Boolean = false
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
                            startTime = if (sliceFromNoon) start.toNoon(-1)
                                .toLocalDateTime() else start.toLocalDateTime(),
                            endTime = if (sliceFromNoon) end.toNoon(-1)
                                .toLocalDateTime() else end.toLocalDateTime()
                        ),
                        interval = duration
                    )

                    val stats = aggregates?.map { stat ->
                        stat.toSahhaStat(
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

    private fun <R : Record> createPermissionActionEvents(
        recordClass: KClass<R>,
        extractEvent: (Record) -> SahhaLogEvent
    ): suspend (ZonedDateTime, ZonedDateTime) -> Pair<String?, List<SahhaLogEvent>?> {
        return { start, end ->
            val permissionGranted = grantedPermissions().contains(
                HealthPermission.getReadPermission(recordClass)
            )
            if (permissionGranted) {
                val records = repository.getRecords(
                    recordClass,
                    TimeRangeFilter.Companion.between(
                        start.toLocalDateTime(),
                        end.toLocalDateTime()
                    )
                )

                val events = records?.map { event ->
                    extractEvent(event)
                }

                Pair(null, events)
            } else Pair("Error: HealthConnect permission for this sensor was not granted", null)
        }
    }
}