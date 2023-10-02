package sdk.sahha.android.domain.use_case.post

import android.content.Context
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.data.mapper.toStepsHealthConnect
import sdk.sahha.android.domain.model.steps.StepsHealthConnect
import sdk.sahha.android.domain.repository.HealthConnectRepo
import sdk.sahha.android.source.Sahha
import java.time.Instant
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit
import javax.inject.Inject
import kotlin.reflect.KClass

private const val tag = "PostHealthConnectDataUseCase"

class PostHealthConnectDataUseCase @Inject constructor(
    private val context: Context,
    private val repo: HealthConnectRepo,
    private val sahhaErrorLogger: SahhaErrorLogger,
) {
    suspend operator fun invoke(
        callback: ((error: String?, successful: Boolean) -> Unit)
    ) {
        queryAndPostHealthConnectData(callback)
    }

    private fun setNextAlarmTime(
        amountToAdd: Long,
        timeUnit: TemporalUnit
    ) {
        val nextTimeStampEpochMillis = Instant.now().plus(amountToAdd, timeUnit).toEpochMilli()

        Sahha.di.sahhaAlarmManager.setAlarm(
            context, nextTimeStampEpochMillis
        )
    }

    private suspend fun queryAndPostHealthConnectData(
        callback: ((error: String?, successful: Boolean) -> Unit)
    ) {
        val granted = repo.getGrantedPermissions()

        granted.forEach {
            when (it) {
                HealthPermission.getReadPermission(StepsRecord::class) -> {
                    repo.getCurrentDayRecordsSteps(StepsRecord::class)?.also { r ->
                        var postData = mutableListOf<StepsHealthConnect>()
                        val local = repo.getAllStepsHc()

                        val queries = r.map { qr -> qr.toStepsHealthConnect() }

                        for (record in queries) {
                            val localMatch = local.find { l -> l.metaId == record.metaId }

                            if (localMatch == null) {
                                postData = saveLocallyAndPrepPost(postData, record)
                                continue
                            }
                            if (localMatch.modifiedDateTime == record.modifiedDateTime)
                                continue

                            // Modified time is different
                            postData = saveLocalAndPrepDiffPost(postData, localMatch, record)
                        }


                        repo.postStepData(postData) { error, successful ->
                            if (successful)
                                saveQuery(StepsRecord::class, successful)

                            logError(error, "queryAndPostHealthConnectData", postData.toString())
                        }
                    }
                }

                HealthPermission.getReadPermission(SleepSessionRecord::class) -> {
                    repo.getCurrentDayRecords(SleepSessionRecord::class)?.also { records ->
                        repo.postSleepSessionData(records) { error, successful ->
                            if (successful)
                                saveQuery(SleepSessionRecord::class, successful)

                            logError(
                                error,
                                "queryAndPostHealthConnectData",
                                records.toString()
                            )
                        }
                    }
                }

                HealthPermission.getReadPermission(HeartRateRecord::class) -> {

                }

                HealthPermission.getReadPermission(RestingHeartRateRecord::class) -> {}
                HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class) -> {}
                HealthPermission.getReadPermission(BloodGlucoseRecord::class) -> {}
                HealthPermission.getReadPermission(BloodPressureRecord::class) -> {}
            }

        }
        setNextAlarmTime(10, ChronoUnit.MINUTES)
        callback(null, true)
    }

    private suspend fun saveLocallyAndPrepPost(
        toPost: MutableList<StepsHealthConnect>,
        record: StepsHealthConnect
    ): MutableList<StepsHealthConnect> {
        repo.saveStepsHc(record)
        toPost.add(record)
        return toPost
    }

    private suspend fun saveLocalAndPrepDiffPost(
        toPost: MutableList<StepsHealthConnect>,
        local: StepsHealthConnect,
        newRecord: StepsHealthConnect
    ): MutableList<StepsHealthConnect> {
        repo.saveStepsHc(local)
        toPost.add(
            StepsHealthConnect(
                metaId = newRecord.metaId,
                dataType = newRecord.dataType,
                count = newRecord.count - local.count,
                source = newRecord.source,
                startDateTime = newRecord.startDateTime,
                endDateTime = newRecord.endDateTime,
                modifiedDateTime = newRecord.modifiedDateTime,
                recordingMethod = newRecord.recordingMethod,
                sourceDevice = newRecord.sourceDevice
            )
        )
        return toPost
    }

    private suspend fun <T : Record> saveQuery(dataType: KClass<T>, postIsSuccessful: Boolean) {
        if (postIsSuccessful)
            repo.saveLastSuccessfulQuery(
                StepsRecord::class,
                repo.successfulQueryTimestamps[
                        HealthPermission
                            .getReadPermission(StepsRecord::class)
                ] ?: LocalDateTime.now()
            )
    }

    private fun logError(error: String?, method: String, body: String) {
        error?.also { e ->
            sahhaErrorLogger.application(
                e, tag,
                method,
                body
            )
        }
    }
}