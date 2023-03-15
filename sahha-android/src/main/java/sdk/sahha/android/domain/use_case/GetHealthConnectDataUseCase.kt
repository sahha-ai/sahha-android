package sdk.sahha.android.domain.use_case

import androidx.health.connect.client.permission.Permission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.SleepStageRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.time.TimeRangeFilter
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.common.enums.HealthConnectSensor
import sdk.sahha.android.data.Constants
import sdk.sahha.android.domain.repository.HealthConnectRepo
import sdk.sahha.android.source.SahhaConverterUtility

class GetHealthConnectDataUseCase constructor(
    private val healthConnectRepo: HealthConnectRepo?,
    private val timeManager: SahhaTimeManager
) {
    suspend operator fun invoke(
        healthConnectSensor: HealthConnectSensor,
        callback: ((error: String?, success: String?) -> Unit)
    ) {
        if (healthConnectRepo == null) {
            callback("repo not init", null)
            return
        }

        val permissions = healthConnectRepo.getGrantedPermissions()
        val nowEpoch = timeManager.nowInEpoch()
        val timeRangeFilter = timeManager.getTimeRangeFilter(
            nowEpoch - Constants.ONE_DAY_IN_MILLIS,
            nowEpoch
        )

        when (healthConnectSensor) {
            HealthConnectSensor.heart_rate -> {
                if(!permissions.contains(Permission.createReadPermission(HeartRateRecord::class))) {
                    callback("Heart rate permissions not granted", null)
                    return
                }
                healthConnectRepo.getHeartRateData(timeRangeFilter).also {
                    val data = SahhaConverterUtility.heartRateToHeartRateSendDto(
                        it,
                        timeManager.nowInISO()
                    )
                    var dataString = ""
                    data.forEach { heartRateData ->
                        dataString += "start: ${heartRateData.startDateTime}" +
                                "\nend: ${heartRateData.endDateTime}"
                        heartRateData.samples.forEach { sample ->
                            dataString += "\nbpm: ${sample.beatsPerMinute}" +
                                    "\ncreated: ${sample.createdAt}" +
                                    "\nstamp: ${sample.timestamp}\n\n"
                        }
                    }
                    callback(null, dataString)
                }
            }
            HealthConnectSensor.sleep_session -> {
                if(!permissions.contains(Permission.createReadPermission(SleepSessionRecord::class))) {
                    callback("Sleep session permissions not granted", null)
                    return
                }
                healthConnectRepo.getSleepData(timeRangeFilter).also {
                    val data =
                        SahhaConverterUtility.sleepSessionToSleepDto(
                            it,
                            timeManager.nowInISO()
                        )
                    var dataString = ""
                    data.forEach { sleepData ->
                        dataString += "stage: ${sleepData.sleepStage}" +
                                "\nsource: ${sleepData.source}" +
                                "\nstart: ${sleepData.startDateTime}" +
                                "\nend: ${sleepData.endDateTime}" +
                                "\nduration mins: ${sleepData.durationInMinutes}\n\n"
                    }
                    callback(null, dataString)
                }
            }
            HealthConnectSensor.sleep_stage -> {
                if(!permissions.contains(Permission.createReadPermission(SleepStageRecord::class))) {
                    callback("Sleep stage permissions not granted", null)
                    return
                }
                healthConnectRepo.getSleepStageData(timeRangeFilter).also {
                    val data =
                        SahhaConverterUtility.sleepStageToSleepDto(it, timeManager.nowInISO())
                    var dataString = ""
                    data.forEach { sleepData ->
                        dataString += "stage: ${sleepData.sleepStage}" +
                                "\nsource: ${sleepData.source}" +
                                "\nstart: ${sleepData.startDateTime}" +
                                "\nend: ${sleepData.endDateTime}" +
                                "\nduration mins: ${sleepData.durationInMinutes}\n\n"
                    }
                    callback(null, dataString)
                }
            }
            HealthConnectSensor.step -> {
                if(!permissions.contains(Permission.createReadPermission(StepsRecord::class))) {
                    callback("Step permissions not granted", null)
                    return
                }
                healthConnectRepo.getStepData(timeRangeFilter).also {
                    val data =
                        SahhaConverterUtility.healthConnectStepToStepDto(
                            it,
                            timeManager.nowInISO()
                        )
                    var dataString = ""
                    data.forEach { step ->
                        dataString += "start: ${step.startDateTime}" +
                                "\nend: ${step.endDateTime}" +
                                "\ncount: ${step.count}\n\n"
                    }
                    callback(null, dataString)
                }
            }
        }
    }
}