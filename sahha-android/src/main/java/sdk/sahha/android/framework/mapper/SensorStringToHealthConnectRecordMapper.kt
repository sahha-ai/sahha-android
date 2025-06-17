package sdk.sahha.android.framework.mapper

import androidx.health.connect.client.aggregate.AggregateMetric
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.BasalMetabolicRateRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.FloorsClimbedRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeightRecord
import androidx.health.connect.client.records.NutritionRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import sdk.sahha.android.source.SahhaSensor

internal class SensorToHealthConnectMetricMapper {
    private val aggregateMetricsMap = hashMapOf(
        SahhaSensor.sleep to SleepSessionRecord.SLEEP_DURATION_TOTAL,
        SahhaSensor.steps to StepsRecord.COUNT_TOTAL,
        SahhaSensor.floors_climbed to FloorsClimbedRecord.FLOORS_CLIMBED_TOTAL,
        SahhaSensor.heart_rate to HeartRateRecord.BPM_AVG,
        SahhaSensor.resting_heart_rate to RestingHeartRateRecord.BPM_AVG,
        SahhaSensor.active_energy_burned to ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL,
        SahhaSensor.total_energy_burned to TotalCaloriesBurnedRecord.ENERGY_TOTAL,
        SahhaSensor.basal_metabolic_rate to BasalMetabolicRateRecord.BASAL_CALORIES_TOTAL,
        SahhaSensor.height to HeightRecord.HEIGHT_AVG,
        SahhaSensor.weight to WeightRecord.WEIGHT_AVG,
        SahhaSensor.exercise to ExerciseSessionRecord.EXERCISE_DURATION_TOTAL,
        SahhaSensor.energy_consumed to NutritionRecord.ENERGY_TOTAL
    )

    fun sahhaSensorToMetric(sensor: SahhaSensor): AggregateMetric<*>? {
        return aggregateMetricsMap[sensor]
    }
}