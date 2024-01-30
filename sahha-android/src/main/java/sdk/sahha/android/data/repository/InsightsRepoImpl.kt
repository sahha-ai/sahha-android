package sdk.sahha.android.data.repository

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import sdk.sahha.android.common.ResponseCode
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaResponseHandler
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.common.TokenBearer
import sdk.sahha.android.data.remote.SahhaApi
import sdk.sahha.android.di.IoScope
import sdk.sahha.android.domain.internal_enum.InsightPermission
import sdk.sahha.android.domain.model.insight.InsightData
import sdk.sahha.android.domain.repository.InsightsRepo
import javax.inject.Inject
import kotlin.coroutines.resume

private const val tag = "InsightsRepoImpl"

internal class InsightsRepoImpl @Inject constructor(
    private val sahhaTimeManager: SahhaTimeManager,
    private val api: SahhaApi,
    private val sahhaErrorLogger: SahhaErrorLogger,
    private val client: HealthConnectClient?,
    @IoScope private val ioScope: CoroutineScope
) : InsightsRepo {
    override fun getSleepStageSummary(sleepRecords: List<SleepSessionRecord>): HashMap<Int, Double> {
        val summaryHashMap = hashMapOf<Int, Double>()
        sleepRecords.forEach { session ->
            session.stages.forEach { stage ->
                val duration = stage.endTime.toEpochMilli() - stage.startTime.toEpochMilli()
                summaryHashMap[stage.stage] =
                    summaryHashMap[stage.stage]?.let { it + duration }?.toDouble() ?: duration.toDouble()
            }
        }
        return summaryHashMap
    }

    private fun getHcPermission(insightPermission: InsightPermission): String {
        return when(insightPermission) {
            InsightPermission.sleep -> HealthPermission.getReadPermission(SleepSessionRecord::class)
            InsightPermission.steps -> HealthPermission.getReadPermission(StepsRecord::class)
            InsightPermission.total_energy -> HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class)
            InsightPermission.active_energy -> HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class)
        }
    }

    override suspend fun hasPermission(insightPermission: InsightPermission): Boolean {
        val granted = client?.permissionController?.getGrantedPermissions() ?: setOf()
        val permission = getHcPermission(insightPermission)
        return granted.contains(permission)
    }

    override fun getMinutesSlept(sleepRecords: List<SleepSessionRecord>): Double {
        val summary = getSleepStageSummary(sleepRecords)
        var sleptDuration = 0.0
        summary.forEach {
            when (it.key) {
                SleepSessionRecord.STAGE_TYPE_DEEP -> {
                    sleptDuration += it.value
                }

                SleepSessionRecord.STAGE_TYPE_LIGHT -> {
                    sleptDuration += it.value
                }

                SleepSessionRecord.STAGE_TYPE_REM -> {
                    sleptDuration += it.value
                }

                SleepSessionRecord.STAGE_TYPE_SLEEPING -> {
                    sleptDuration += it.value
                }

                SleepSessionRecord.STAGE_TYPE_UNKNOWN -> {
                    sleptDuration += it.value
                }
            }
        }

        return sleptDuration.toMinutes()
    }

    override fun getMinutesInBed(sleepRecords: List<SleepSessionRecord>): Double {
        val summary = getSleepStageSummary(sleepRecords)
        var inBedDuration = 0.0
        summary.forEach {
            when (it.key) {
                SleepSessionRecord.STAGE_TYPE_DEEP -> {
                    inBedDuration += it.value
                }

                SleepSessionRecord.STAGE_TYPE_LIGHT -> {
                    inBedDuration += it.value
                }

                SleepSessionRecord.STAGE_TYPE_REM -> {
                    inBedDuration += it.value
                }

                SleepSessionRecord.STAGE_TYPE_SLEEPING -> {
                    inBedDuration += it.value
                }

                SleepSessionRecord.STAGE_TYPE_UNKNOWN -> {
                    inBedDuration += it.value
                }

                SleepSessionRecord.STAGE_TYPE_AWAKE -> {
                    inBedDuration += it.value
                }

                SleepSessionRecord.STAGE_TYPE_AWAKE_IN_BED -> {
                    inBedDuration += it.value
                }
            }
        }

        return inBedDuration.toMinutes()
    }

    override fun getMinutesInSleepStage(summary: HashMap<Int, Double>, sleepStage: Int): Double {
        return summary[sleepStage]?.toMinutes() ?: 0.0
    }
  
    override fun getStepCount(stepsRecords: List<StepsRecord>): Double {
        var count = 0.0
        stepsRecords.forEach {
            count += it.count
        }

        return count
    }

    override suspend fun postInsights(
        token: String,
        insights: List<InsightData>,
        refreshedToken: Boolean,
        callback: suspend (error: String?, successful: Boolean) -> Unit
    ) {
        val response = api.postInsightsData(TokenBearer(token), insights)
        val code = response.code()

        try {
            if (ResponseCode.isUnauthorized(code)) {
                if (!refreshedToken) {
                    suspendCancellableCoroutine { cont ->
                        ioScope.launch {
                            SahhaResponseHandler.newTokenOnExpired(code) { newToken ->
                                postInsights(
                                    newToken ?: token,
                                    insights,
                                    true,
                                ) { error, successful ->
                                    callback(error, successful)
                                    cont.resume(Unit)
                                }
                            }
                        }
                    }
                    return
                }

                callback(SahhaErrors.invalidToken, false)
                sahhaErrorLogger.api(response)
                return
            }

            if (ResponseCode.isSuccessful(code)) {
                callback(null, true)
                return
            }

            // Other error
            callback("${response.code()}: ${response.message()}", false)
            sahhaErrorLogger.api(response)
        } catch (e: Exception) {
            callback(e.message, false)
            sahhaErrorLogger.application(
                e.message ?: SahhaErrors.somethingWentWrong,
                tag,
                "postInsights",
            )
        }
    }
}

fun Long.toMinutes(): Long {
    return this / 1000 / 60
}

fun Double.toMinutes(): Double {
    return this / 1000 / 60
}