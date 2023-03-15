package sdk.sahha.android.domain.use_case.post

import androidx.health.connect.client.permission.Permission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.SleepStageRecord
import androidx.health.connect.client.records.StepsRecord
import kotlinx.coroutines.runBlocking
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.common.enums.HealthConnectSensor
import sdk.sahha.android.data.Constants
import sdk.sahha.android.domain.repository.HealthConnectRepo

class PostHealthConnectDataUseCase constructor(
    private val timeManager: SahhaTimeManager,
    private val repo: HealthConnectRepo?,
) {
    private var nowEpoch = timeManager.nowInEpoch()
    private var errorSummary = ""

    private val successes = mutableListOf<Boolean>()

    private lateinit var grantedPermissions: Set<Permission>
    private lateinit var healthConnectSensors: Set<Enum<HealthConnectSensor>>

    suspend operator fun invoke(
        healthConnectSensors: Set<Enum<HealthConnectSensor>> = HealthConnectSensor.values().toSet(),
        callback: ((error: String?, successful: Boolean) -> Unit)? = null
    ) {
        repo?.also { repo ->
            this.grantedPermissions = repo.getGrantedPermissions()
            this.healthConnectSensors = healthConnectSensors

            resetData()

            postSleepSessionData()
            postSleepStageData()
            postStepData()
            postHeartRateData()

            if (successes.contains(false)) {
                callback?.invoke(errorSummary, false)
                return
            }

            callback?.invoke(null, true)
        } ?: callback?.invoke(SahhaErrors.healthConnect.unavailable, false)
    }

    private fun resetData() {
        errorSummary = ""
        nowEpoch = timeManager.nowInEpoch()
        successes.clear()
    }

    private suspend fun postSleepSessionData() {
        repo?.also { repo ->
            val sleepSensor = HealthConnectSensor.sleep_session
            val startTime = repo.getLastPostTimestamp(sleepSensor.ordinal)?.let { lastPost ->
                val oneDaySinceLastPost =
                    nowEpoch > (lastPost + Constants.ONE_DAY_IN_MILLIS)

                if (oneDaySinceLastPost)
                    lastPost
                else {
                    successes.add(false)
                    errorSummary += "${sleepSensor.name} ${SahhaErrors.healthConnect.minimumPostInterval}\n"
                    return
                }
            } ?: timeManager.getEpochMillisFrom(7)
            val timeRangeFilter = timeManager.getTimeRangeFilter(startTime, nowEpoch)

            if (healthConnectSensors.contains(sleepSensor))
                if (grantedPermissions.contains(Permission.createReadPermission(SleepSessionRecord::class)))
                    if (repo.getSleepData(timeRangeFilter).isNotEmpty())
                        repo.postSleepSessions(timeRangeFilter) { error, successful ->
                            successes.add(successful)

                            if (successful) runBlocking {
                                repo.saveLastPost(sleepSensor.ordinal, nowEpoch)
                            }

                            error?.also { e ->
                                errorSummary += "$e\n"
                            }
                        } else errorSummary += "${
                        SahhaErrors.healthConnect.localDataIsEmpty(sleepSensor)
                    }\n"
        }
    }

    private suspend fun postSleepStageData() {
        repo?.also { repo ->
            val sleepStageSensor = HealthConnectSensor.sleep_stage
            val startTime = repo.getLastPostTimestamp(sleepStageSensor.ordinal)?.let { lastPost ->
                val oneDaySinceLastPost =
                    nowEpoch > (lastPost + Constants.ONE_DAY_IN_MILLIS)

                if (oneDaySinceLastPost)
                    lastPost
                else {
                    successes.add(false)
                    errorSummary += "${sleepStageSensor.name} ${SahhaErrors.healthConnect.minimumPostInterval}\n"
                    return
                }
            } ?: timeManager.getEpochMillisFrom(7)
            val timeRangeFilter = timeManager.getTimeRangeFilter(startTime, nowEpoch)

            if (healthConnectSensors.contains(sleepStageSensor))
                if (grantedPermissions.contains(Permission.createReadPermission(SleepStageRecord::class)))
                    if (repo.getSleepStageData(timeRangeFilter).isNotEmpty())
                        repo.postSleepStages(timeRangeFilter) { error, successful ->
                            successes.add(successful)

                            if (successful) runBlocking {
                                repo.saveLastPost(
                                    sleepStageSensor.ordinal,
                                    nowEpoch
                                )
                            }

                            error?.also { e ->
                                errorSummary += "$e\n"
                            }
                        } else errorSummary += "${
                        SahhaErrors.healthConnect.localDataIsEmpty(sleepStageSensor)
                    }\n"
        }
    }

    private suspend fun postStepData() {
        repo?.also { repo ->
            val stepSensor = HealthConnectSensor.step
            val startTime = repo.getLastPostTimestamp(stepSensor.ordinal)?.let { lastPost ->
                val oneDaySinceLastPost =
                    nowEpoch > (lastPost + Constants.ONE_DAY_IN_MILLIS)

                if (oneDaySinceLastPost)
                    lastPost
                else {
                    successes.add(false)
                    errorSummary += "${stepSensor.name} ${SahhaErrors.healthConnect.minimumPostInterval}\n"
                    return
                }
            } ?: timeManager.getEpochMillisFrom(7)
            val timeRangeFilter = timeManager.getTimeRangeFilter(startTime, nowEpoch)

            if (healthConnectSensors.contains(stepSensor))
                if (grantedPermissions.contains(Permission.createReadPermission(StepsRecord::class)))
                    if (repo.getStepData(timeRangeFilter).isNotEmpty())
                        repo.postSteps(timeRangeFilter) { error, successful ->
                            successes.add(successful)

                            if (successful) runBlocking {
                                repo.saveLastPost(stepSensor.ordinal, nowEpoch)
                            }

                            error?.also { e ->
                                errorSummary += "$e\n"
                            }
                        } else errorSummary += "${
                        SahhaErrors.healthConnect.localDataIsEmpty(stepSensor)
                    }\n"
        }
    }

    private suspend fun postHeartRateData() {
        repo?.also { repo ->
            val heartRateSensor = HealthConnectSensor.heart_rate
            val startTime = repo.getLastPostTimestamp(heartRateSensor.ordinal)?.let { lastPost ->
                val oneDaySinceLastPost =
                    nowEpoch > (lastPost + Constants.ONE_DAY_IN_MILLIS)

                if (oneDaySinceLastPost)
                    lastPost
                else {
                    successes.add(false)
                    errorSummary += "${heartRateSensor.name} ${SahhaErrors.healthConnect.minimumPostInterval}\n"
                    return
                }
            } ?: timeManager.getEpochMillisFrom(7)
            val timeRangeFilter = timeManager.getTimeRangeFilter(startTime, nowEpoch)

            if (healthConnectSensors.contains(heartRateSensor))
                if (grantedPermissions.contains(Permission.createReadPermission(HeartRateRecord::class)))
                    if (repo.getHeartRateData(timeRangeFilter).isNotEmpty())
                        repo.postHeartRates(timeRangeFilter) { error, successful ->
                            successes.add(successful)

                            if (successful) runBlocking {
                                repo.saveLastPost(heartRateSensor.ordinal, nowEpoch)
                            }

                            error?.also { e ->
                                errorSummary += "$e\n"
                            }
                        }
                    else errorSummary += "${
                        SahhaErrors.healthConnect.localDataIsEmpty(heartRateSensor)
                    }\n"
        }
    }
}