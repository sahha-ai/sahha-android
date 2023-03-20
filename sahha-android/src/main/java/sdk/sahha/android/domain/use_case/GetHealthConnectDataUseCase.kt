package sdk.sahha.android.domain.use_case

import androidx.health.connect.client.permission.Permission
import androidx.health.connect.client.records.*
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.source.HealthConnectSensor
import sdk.sahha.android.data.Constants
import sdk.sahha.android.data.remote.dto.send.HeartRateSendDto
import sdk.sahha.android.data.remote.dto.send.SleepSendDto
import sdk.sahha.android.data.remote.dto.send.StepSendDto
import sdk.sahha.android.data.remote.dto.toSleepSendDto
import sdk.sahha.android.domain.repository.HealthConnectRepo
import sdk.sahha.android.source.SahhaConverterUtility
import kotlin.reflect.KClass

class GetHealthConnectDataUseCase constructor(
    private val healthConnectRepo: HealthConnectRepo?,
    private val timeManager: SahhaTimeManager
) {
    private lateinit var permissions: Set<Permission>
    private lateinit var timeRangeFilter: TimeRangeFilter
    suspend operator fun invoke(
        healthConnectSensor: HealthConnectSensor,
        callback: ((error: String?, success: String?) -> Unit)
    ) {
        if (healthConnectRepo == null) {
            callback("repo not init", null)
            return
        }

        permissions = healthConnectRepo.getGrantedPermissions()
        val nowEpoch = timeManager.nowInEpoch()
        timeRangeFilter = timeManager.getTimeRangeFilter(
            nowEpoch - Constants.ONE_DAY_IN_MILLIS,
            nowEpoch
        )

        when (healthConnectSensor) {
            HealthConnectSensor.heart_rate -> {
                getSensorData(
                    HealthConnectSensor.heart_rate,
                    HeartRateRecord::class,
                    callback
                )
            }
            HealthConnectSensor.sleep_session -> {
                getSensorData(
                    HealthConnectSensor.sleep_session,
                    SleepSessionRecord::class,
                    callback
                )
            }
            HealthConnectSensor.sleep_stage -> {
                getSensorData(
                    HealthConnectSensor.sleep_stage,
                    SleepStageRecord::class,
                    callback
                )
            }
            HealthConnectSensor.step -> {
                getSensorData(
                    HealthConnectSensor.step,
                    StepsRecord::class,
                    callback
                )
            }
        }
    }

    private suspend fun <T : Record> getSensorData(
        sensor: Enum<HealthConnectSensor>,
        recordClass: KClass<T>,
        callback: (error: String?, success: String?) -> Unit
    ) {
        healthConnectRepo ?: return
        if (!permissions.contains(Permission.createReadPermission(recordClass))) {
            callback(SahhaErrors.healthConnect.noPermissions(sensor.name), null)
            return
        }
        healthConnectRepo.getSensorData(
            ReadRecordsRequest(recordClass, timeRangeFilter)
        ).also {
            val data = when (sensor) {
                HealthConnectSensor.sleep_session -> getSleepSendDtoFromSleepSessions(it)
                HealthConnectSensor.sleep_stage -> getSleepSendDtoFromSleepStages(it)
                HealthConnectSensor.step -> getStepSendDto(it)
                HealthConnectSensor.heart_rate -> getHeartRateSendDto(it)
                else -> {
                    callback("Something went wrong retrieving data", null)
                    return
                }
            }

            if(data.isEmpty()) {
                callback(SahhaErrors.healthConnect.localDataIsEmpty(sensor), null)
                return
            }

            var dataString = ""
            data.forEach { d ->
                dataString += "$d\n\n"
                if(d is HeartRateSendDto) d.samples.forEach { sample ->
                    dataString += "$sample\n\n"
                }
            }
            callback(null, dataString)
        }
    }

    private fun <T:Record> getHeartRateSendDto(records: List<T>): List<HeartRateSendDto> {
        return SahhaConverterUtility.heartRateToHeartRateSendDto(
            records as List<HeartRateRecord>,
            timeManager.nowInISO()
        )
    }

    private fun <T:Record> getSleepSendDtoFromSleepSessions(records: List<T>): List<SleepSendDto> {
        return SahhaConverterUtility.sleepSessionToSleepDto(
            records as List<SleepSessionRecord>,
            timeManager.nowInISO()
        ).map { it.toSleepSendDto() }
    }

    private fun <T:Record> getSleepSendDtoFromSleepStages(records: List<T>): List<SleepSendDto> {
        return SahhaConverterUtility.sleepStageToSleepDto(
            records as List<SleepStageRecord>,
            timeManager.nowInISO()
        ).map { it.toSleepSendDto() }
    }

    private fun <T:Record> getStepSendDto(records: List<T>): List<StepSendDto> {
        return SahhaConverterUtility.healthConnectStepToStepDto(
            records as List<StepsRecord>,
            timeManager.nowInISO()
        )
    }
}