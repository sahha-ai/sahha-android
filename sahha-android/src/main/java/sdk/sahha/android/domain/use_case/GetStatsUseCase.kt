package sdk.sahha.android.domain.use_case

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
import sdk.sahha.android.domain.repository.HealthConnectRepo
import java.time.ZonedDateTime
import javax.inject.Inject

internal class GetStatsUseCase @Inject constructor(
    private val repository: HealthConnectRepo
) {
    suspend operator fun invoke(
        startDateTime: ZonedDateTime,
        endDateTime: ZonedDateTime
    ) {
        val granted = repository.getGrantedPermissions()
        granted.forEach {
            when (it) {
                HealthPermission.getReadPermission(StepsRecord::class) -> {

                }

                HealthPermission.getReadPermission(SleepSessionRecord::class) -> {

                }

                HealthPermission.getReadPermission(HeartRateRecord::class) -> {

                }

                HealthPermission.getReadPermission(RestingHeartRateRecord::class) -> {

                }

                HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class) -> {

                }

                HealthPermission.getReadPermission(BloodGlucoseRecord::class) -> {

                }

                HealthPermission.getReadPermission(BloodPressureRecord::class) -> {

                }

                HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class) -> {

                }

                HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class) -> {

                }

                HealthPermission.getReadPermission(OxygenSaturationRecord::class) -> {

                }

                HealthPermission.getReadPermission(Vo2MaxRecord::class) -> {

                }

                HealthPermission.getReadPermission(BasalMetabolicRateRecord::class) -> {

                }

                HealthPermission.getReadPermission(BodyFatRecord::class) -> {

                }

                HealthPermission.getReadPermission(BodyWaterMassRecord::class) -> {

                }

                HealthPermission.getReadPermission(LeanBodyMassRecord::class) -> {

                }

                HealthPermission.getReadPermission(HeightRecord::class) -> {

                }

                HealthPermission.getReadPermission(WeightRecord::class) -> {

                }

                HealthPermission.getReadPermission(RespiratoryRateRecord::class) -> {

                }

                HealthPermission.getReadPermission(BoneMassRecord::class) -> {

                }

                HealthPermission.getReadPermission(FloorsClimbedRecord::class) -> {

                }

                HealthPermission.getReadPermission(BodyTemperatureRecord::class) -> {

                }

                HealthPermission.getReadPermission(BasalBodyTemperatureRecord::class) -> {

                }

                HealthPermission.getReadPermission(ExerciseSessionRecord::class) -> {

                }
            }
        }
    }
}