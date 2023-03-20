package sdk.sahha.android.domain.use_case.post

import androidx.health.connect.client.permission.Permission
import androidx.health.connect.client.records.*
import androidx.health.connect.client.request.ReadRecordsRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.data.Constants
import sdk.sahha.android.domain.repository.HealthConnectRepo
import sdk.sahha.android.source.HealthConnectSensor
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

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
        try {
            repo?.also { repo ->
                this.grantedPermissions = repo.getGrantedPermissions()
                this.healthConnectSensors = healthConnectSensors

                resetData()
                val latch = CountDownLatch(4)

                runBlocking {
                    launch {
                        postHealthConnectData(
                            HealthConnectSensor.step,
                            StepsRecord::class
                        ) { error, successful ->
                            summariseCallback(error, successful)
                            latch.countDown()
                        }
                    }

                    launch {
                        postHealthConnectData(
                            HealthConnectSensor.sleep_session,
                            SleepSessionRecord::class
                        ) { error, successful ->
                            summariseCallback(error, successful)
                            latch.countDown()
                        }
                    }

                    launch {
                        postHealthConnectData(
                            HealthConnectSensor.sleep_stage,
                            SleepStageRecord::class
                        ) { error, successful ->
                            summariseCallback(error, successful)
                            latch.countDown()
                        }
                    }

                    launch {
                        postHealthConnectData(
                            HealthConnectSensor.heart_rate,
                            HeartRateRecord::class
                        ) { error, successful ->
                            summariseCallback(error, successful)
                            latch.countDown()
                        }
                    }
                }

                latch.await(10, TimeUnit.SECONDS)

                if (successes.contains(false)) {
                    callback?.invoke(errorSummary, false)
                    return
                }

                callback?.invoke(null, true)
            } ?: callback?.invoke(SahhaErrors.healthConnect.unavailable, false)
        } catch (e: Exception) {
            callback?.invoke(e.message, false)
        }
    }

    private fun resetData() {
        errorSummary = ""
        nowEpoch = timeManager.nowInEpoch()
        successes.clear()
    }

    private suspend fun <T : Record> postHealthConnectData(
        sensor: Enum<HealthConnectSensor>,
        recordClass: KClass<T>,
        callback: ((error: String?, successful: Boolean) -> Unit)
    ) {
        repo?.also { repo ->
            // Block heart rate until API implemented
//            if (sensor == HealthConnectSensor.heart_rate) {
//                callback(
//                    "${sensor.name.uppercase()} Error: Heart rate posting is not yet available",
//                    false
//                )
//                return
//            }

            val startTime = repo.getLastPostTimestamp(sensor.ordinal)?.let { lastPost ->
                val oneDaySinceLastPost =
                    nowEpoch > (lastPost + Constants.MINIMUM_POST_INTERVAL)

                if (oneDaySinceLastPost)
                    lastPost
                else {
                    callback(SahhaErrors.healthConnect.minimumPostInterval(sensor.name), false)
                    return
                }
            } ?: timeManager.getEpochMillisFrom(7)
            val timeRangeFilter = timeManager.getTimeRangeFilter(startTime, nowEpoch)

            // Guards
            if (!healthConnectSensors.contains(sensor)) return
            if (!grantedPermissions.contains(Permission.createReadPermission(recordClass))) {
                callback(SahhaErrors.healthConnect.noPermissions(sensor.name), false)
                return
            }
            if (repo.getSensorData(ReadRecordsRequest(recordClass, timeRangeFilter))
                    .isEmpty()
            ) {
                callback(SahhaErrors.healthConnect.localDataIsEmpty(sensor), false)
                return
            }

            repo.postHealthConnectData(sensor, timeRangeFilter) { error, successful ->
                if (successful) runBlocking {
                    repo.saveLastPost(sensor.ordinal, nowEpoch)
                    callback(null, true)
                    return@runBlocking
                }

                error?.also { e ->
                    callback(e, false)
                }
            }
        }
    }

    private fun summariseCallback(error: String?, successful: Boolean) {
        error?.also { errorSummary += "$it\n" }
        successes.add(successful)
    }
}