package sdk.sahha.android.data.repository

import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.SleepStageRecord
import androidx.health.connect.client.records.StepsRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import sdk.sahha.android.common.ResponseCode
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaResponseHandler
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.data.remote.SahhaApi
import sdk.sahha.android.di.IoScope
import sdk.sahha.android.domain.model.insight.InsightData
import sdk.sahha.android.domain.repository.InsightsRepo
import sdk.sahha.android.source.SahhaConverterUtility
import javax.inject.Inject
import kotlin.coroutines.resume

private const val tag = "InsightsRepoImpl"

class InsightsRepoImpl @Inject constructor(
    private val sahhaTimeManager: SahhaTimeManager,
    private val api: SahhaApi,
    private val sahhaErrorLogger: SahhaErrorLogger,
    @IoScope private val ioScope: CoroutineScope
) : InsightsRepo {
    internal fun getSleepStageSummary(sleepRecords: List<SleepSessionRecord>): HashMap<Int, Long> {
        val summaryHashMap = hashMapOf<Int, Long>()
        sleepRecords.forEach { session ->
            session.stages.forEach { stage ->
                val duration = stage.endTime.toEpochMilli() - stage.startTime.toEpochMilli()
                summaryHashMap[stage.stage] =
                    summaryHashMap[stage.stage]?.let { it + duration } ?: duration
            }
        }
        return summaryHashMap
    }

    override fun getMinutesSlept(sleepRecords: List<SleepSessionRecord>): Long {
        val summary = getSleepStageSummary(sleepRecords)
        var sleptDuration = 0L
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

    override fun getMinutesInBed(sleepRecords: List<SleepSessionRecord>): Long {
        val summary = getSleepStageSummary(sleepRecords)
        var inBedDuration = 0L
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

    override fun getStepCount(stepsRecords: List<StepsRecord>): Long {
        var count = 0L
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
        val response = api.postInsightsData(token, insights)
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
                                    callback
                                )
                                cont.resume(Unit)
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