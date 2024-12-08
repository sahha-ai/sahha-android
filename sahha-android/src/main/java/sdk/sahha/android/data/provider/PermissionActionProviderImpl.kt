package sdk.sahha.android.data.provider

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
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.RespiratoryRateRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.Vo2MaxRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.time.TimeRangeFilter
import sdk.sahha.android.common.Constants
import sdk.sahha.android.domain.model.stats.SahhaStat
import sdk.sahha.android.domain.provider.PermissionActionProvider
import sdk.sahha.android.domain.repository.HealthConnectRepo
import sdk.sahha.android.source.SahhaSensor
import java.time.Period
import java.time.ZonedDateTime
import java.util.UUID
import javax.inject.Inject

internal class PermissionActionProviderImpl @Inject constructor(
    private val repository: HealthConnectRepo
) : PermissionActionProvider {
    private fun toSahhaStat(
        id: String,
        sensor: SahhaSensor,
        value: Long,
        unit: String,
        startDateTime: ZonedDateTime,
        endDateTime: ZonedDateTime,
        sources: List<String>? = null,
    ): SahhaStat {

        return SahhaStat(
            id = "",
            sensor = SahhaSensor.sleep,
            value = 0,
            unit = "",
            startDate = ZonedDateTime.now(),
            endDate = ZonedDateTime.now(),
            sources = sources,
        )
    }

    val permissionActions:
            Map<String, suspend (Period, ZonedDateTime, ZonedDateTime) -> Unit> =
        mapOf(
            HealthPermission.getReadPermission(StepsRecord::class) to { period, start, end ->
                val aggregates = repository.getAggregateRecordsByPeriod(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.Companion.between(
                        startTime = start.toLocalDateTime(),
                        endTime = end.toLocalDateTime()
                    ),
                    interval = period
                )
                val stats = aggregates?.map { stat ->
                    toSahhaStat(
                        UUID.randomUUID().toString(),
                        SahhaSensor.step_count,
                        stat.result[StepsRecord.COUNT_TOTAL] ?: 0,
                        Constants.DataUnits.COUNT,
                        start,
                        end,
                        stat.result.dataOrigins.map { it.packageName }
                    )
                }
            },
            HealthPermission.getReadPermission(SleepSessionRecord::class) to { period, start, end -> },
            HealthPermission.getReadPermission(HeartRateRecord::class) to { period, start, end -> },
            HealthPermission.getReadPermission(RestingHeartRateRecord::class) to { period, start, end -> },
            HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class) to { period, start, end -> },
            HealthPermission.getReadPermission(BloodGlucoseRecord::class) to { period, start, end -> },
            HealthPermission.getReadPermission(BloodPressureRecord::class) to { period, start, end -> },
            HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class) to { period, start, end -> },
            HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class) to { period, start, end -> },
            HealthPermission.getReadPermission(OxygenSaturationRecord::class) to { period, start, end -> },
            HealthPermission.getReadPermission(Vo2MaxRecord::class) to { period, start, end -> },
            HealthPermission.getReadPermission(BasalMetabolicRateRecord::class) to { period, start, end -> },
            HealthPermission.getReadPermission(BodyFatRecord::class) to { period, start, end -> },
            HealthPermission.getReadPermission(BodyWaterMassRecord::class) to { period, start, end -> },
            HealthPermission.getReadPermission(LeanBodyMassRecord::class) to { period, start, end -> },
            HealthPermission.getReadPermission(HeightRecord::class) to { period, start, end -> },
            HealthPermission.getReadPermission(WeightRecord::class) to { period, start, end -> },
            HealthPermission.getReadPermission(RespiratoryRateRecord::class) to { period, start, end -> },
            HealthPermission.getReadPermission(BoneMassRecord::class) to { period, start, end -> },
            HealthPermission.getReadPermission(FloorsClimbedRecord::class) to { period, start, end -> },
            HealthPermission.getReadPermission(BodyTemperatureRecord::class) to { period, start, end -> },
            HealthPermission.getReadPermission(BasalBodyTemperatureRecord::class) to { period, start, end -> },
            HealthPermission.getReadPermission(ExerciseSessionRecord::class) to { period, start, end -> }
        )

}