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
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.data.Constants
import sdk.sahha.android.data.mapper.toStepsHealthConnect
import sdk.sahha.android.di.IoScope
import sdk.sahha.android.domain.model.steps.StepsHealthConnect
import sdk.sahha.android.domain.repository.HealthConnectRepo
import sdk.sahha.android.source.Sahha
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.reflect.KClass

private const val tag = "PostHealthConnectDataUseCase"

class PostHealthConnectDataUseCase @Inject constructor(
    private val context: Context,
    private val repo: HealthConnectRepo,
    private val sahhaErrorLogger: SahhaErrorLogger,
    private val timeManager: SahhaTimeManager,
    @IoScope private val ioScope: CoroutineScope
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
            nextTimeStampEpochMillis
        )
    }

    private suspend fun queryAndPostHealthConnectData(
        callback: ((error: String?, successful: Boolean) -> Unit)
    ) {
        val granted = repo.getGrantedPermissions()

        granted.forEach {
            println("Querying... $it")
            when (it) {
                HealthPermission.getReadPermission(StepsRecord::class) -> {
                    suspendCoroutine<Unit> { cont ->
                        ioScope.launch {
                            repo.getCurrentDayRecords(StepsRecord::class)?.also { r ->
                                println("StepsRecord0000")
                                var postData = mutableListOf<StepsHealthConnect>()
                                val local = repo.getAllStepsHc()

                                val queries = r.map { qr -> qr.toStepsHealthConnect() }

                                println("StepsRecord0001")
                                for (record in queries) {
                                    val localMatch = local.find { l -> l.metaId == record.metaId }

                                    if (localMatch == null) {
                                        postData = saveLocallyAndPrepPost(postData, record)
                                        continue
                                    }
                                    if (localMatch.modifiedDateTime == record.modifiedDateTime) {
                                        continue
                                    }

                                    // Modified time is different
                                    postData =
                                        saveLocalAndPrepDiffPost(postData, localMatch, record)
                                }


                                if (postData.isEmpty()) {
                                    cont.resume(Unit)
                                    this.cancel()
                                    return@launch
                                }

                                repo.postStepData(postData) { error, successful ->
                                    if (successful) {
                                        println("StepsRecord0005")
                                        clearLastMidnightSteps()
                                        saveQuery(StepsRecord::class, successful)
                                    }

                                    logError(
                                        error,
                                        "queryAndPostHealthConnectData",
                                        postData.toString()
                                    )
                                    cont.resume(Unit)
                                    this.cancel()
                                }
                            } ?: {
                                println("StepsRecord0006")
                                cont.resume(Unit)
                                this.cancel()
                            }
                        }
                    }
                }

                HealthPermission.getReadPermission(SleepSessionRecord::class) -> {
                    suspendCoroutine<Unit> { cont ->
                        ioScope.launch {
                            repo.getNewRecords(SleepSessionRecord::class)?.also { records ->
                                repo.postSleepSessionData(records) { error, successful ->
                                    if (successful)
                                        saveQuery(SleepSessionRecord::class, successful)

                                    logError(
                                        error,
                                        "queryAndPostHealthConnectData",
                                        records.toString()
                                    )
                                    cont.resume(Unit)
                                    this.cancel()
                                }
                            } ?: cont.resume(Unit)
                            this.cancel()
                        }
                    }
                }

                HealthPermission.getReadPermission(HeartRateRecord::class) -> {
                    println("HealthPermission.getReadPermission(HeartRateRecord::class)0001")
                    suspendCoroutine<Unit> { cont ->
                        println("HealthPermission.getReadPermission(HeartRateRecord::class)0002")
                        ioScope.launch {
                            println("HealthPermission.getReadPermission(HeartRateRecord::class)0003")
                            repo.getNewRecords(HeartRateRecord::class)?.also { records ->
                                println("HealthPermission.getReadPermission(HeartRateRecord::class)0004")
                                repo.postHeartRateData(records) { error, successful ->
                                    println("HealthPermission.getReadPermission(HeartRateRecord::class)0005")
                                    if (successful)
                                        saveQuery(HeartRateRecord::class, successful)

                                    logError(
                                        error,
                                        "queryAndPostHealthConnectData",
                                        records.toString()
                                    )
                                    println("HealthPermission.getReadPermission(HeartRateRecord::class)0006")
                                    cont.resume(Unit)
                                    this.cancel()
                                }
                                println("HealthPermission.getReadPermission(HeartRateRecord::class)0007")
                            } ?: cont.resume(Unit)
                            println("HealthPermission.getReadPermission(HeartRateRecord::class)0008")
                            this.cancel()
                        }
                        println("HealthPermission.getReadPermission(HeartRateRecord::class)0009")
                    }
                }

                HealthPermission.getReadPermission(RestingHeartRateRecord::class) -> {
                    suspendCoroutine<Unit> { cont ->
                        ioScope.launch {
                            repo.getNewRecords(RestingHeartRateRecord::class)?.also { records ->
                                repo.postRestingHeartRateData(records) { error, successful ->
                                    if (successful)
                                        saveQuery(HeartRateRecord::class, successful)

                                    logError(
                                        error,
                                        "queryAndPostHealthConnectData",
                                        records.toString()
                                    )
                                    cont.resume(Unit)
                                    this.cancel()
                                }
                            } ?: cont.resume(Unit)
                            this.cancel()
                        }
                    }
                }

                HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class) -> {
                    suspendCoroutine<Unit> { cont ->
                        ioScope.launch {
                            repo.getNewRecords(HeartRateVariabilityRmssdRecord::class)
                                ?.also { records ->
                                    repo.postHeartRateVariabilityRmssdData(records) { error, successful ->
                                        if (successful)
                                            saveQuery(
                                                HeartRateVariabilityRmssdRecord::class,
                                                successful
                                            )

                                        logError(
                                            error,
                                            "queryAndPostHealthConnectData",
                                            records.toString()
                                        )
                                        cont.resume(Unit)
                                        this.cancel()
                                    }
                                } ?: cont.resume(Unit)
                            this.cancel()
                        }
                    }
                }

                HealthPermission.getReadPermission(BloodGlucoseRecord::class) -> {
                    suspendCoroutine<Unit> { cont ->
                        ioScope.launch {
                            repo.getNewRecords(BloodGlucoseRecord::class)?.also { records ->
                                repo.postBloodGlucoseData(records) { error, successful ->
                                    if (successful)
                                        saveQuery(BloodGlucoseRecord::class, successful)

                                    logError(
                                        error,
                                        "queryAndPostHealthConnectData",
                                        records.toString()
                                    )
                                    cont.resume(Unit)
                                    this.cancel()
                                }
                            } ?: cont.resume(Unit)
                            this.cancel()
                        }
                    }
                }

                HealthPermission.getReadPermission(BloodPressureRecord::class) -> {
                    suspendCoroutine<Unit> { cont ->
                        ioScope.launch {
                            repo.getNewRecords(BloodPressureRecord::class)?.also { records ->
                                repo.postBloodPressureData(records) { error, successful ->
                                    if (successful)
                                        saveQuery(BloodPressureRecord::class, successful)

                                    cont.resume(Unit)
                                    this.cancel()
                                }
                            } ?: cont.resume(Unit)
                            this.cancel()
                        }
                    }
                }
            }
        }

        setNextAlarmTime(Constants.DEFAULT_ALARM_INTERVAL_MINS, ChronoUnit.MINUTES)
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
        repo.saveStepsHc(newRecord)
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
                sourceDevice = newRecord.sourceDevice,
                deviceManufacturer = newRecord.deviceManufacturer,
                deviceModel = newRecord.deviceModel
            )
        )
        return toPost
    }

    private suspend fun <T : Record> saveQuery(
        dataType: KClass<T>,
        postIsSuccessful: Boolean,
        timestamp: ZonedDateTime = ZonedDateTime.now()
    ) {
        if (postIsSuccessful)
            repo.saveLastSuccessfulQuery(
                dataType,
                repo.successfulQueryTimestamps[
                        HealthPermission
                            .getReadPermission(dataType)
                ] ?: timestamp
            )
    }

    private suspend fun clearLastMidnightSteps() {
        val date = repo.getLastSuccessfulQuery(StepsRecord::class)?.toLocalDate()
        date?.also { d ->
            val lastMidnight = LocalDateTime.of(d, LocalTime.MIDNIGHT)
            val currentMidnight = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT)
            if (currentMidnight > lastMidnight) repo.clearStepsBeforeHc(currentMidnight)
        }
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

    // Potentially re-use in the future
    private suspend fun awaitAggregateHeartRatePost() {
        suspendCoroutine<Unit> { cont ->
            ioScope.launch {
                val now = ZonedDateTime.now()
                repo.getAggregateRecordsByDuration(
                    setOf(
                        HeartRateRecord.BPM_MIN,
                        HeartRateRecord.BPM_MAX,
                        HeartRateRecord.BPM_AVG,
                    ),
                    repo.getLastSuccessfulQuery(HeartRateRecord::class)
                        ?.let { lastQuery ->
                            TimeRangeFilter.Companion.after(lastQuery.toLocalDateTime())
                        } ?: TimeRangeFilter.Companion.between(
                        now.minusDays(1).toLocalDateTime(), now.toLocalDateTime()
                    ),
                    Duration.ofMinutes(15)
                )?.also { records ->
                    repo.postHeartRateAggregateData(
                        records,
                        HeartRateRecord::class
                    ) { error, successful ->
                        if (successful)
                            saveQuery(HeartRateRecord::class, successful, now)

                        cont.resume(Unit)
                        this.cancel()
                    }
                } ?: cont.resume(Unit)
                this.cancel()
            }
        }
    }

    private suspend fun awaitAggregateRestingHeartRatePost() {
        suspendCoroutine<Unit> { cont ->
            ioScope.launch {
                val now = ZonedDateTime.now()
                repo.getAggregateRecordsByDuration(
                    setOf(
                        RestingHeartRateRecord.BPM_MIN,
                        RestingHeartRateRecord.BPM_MAX,
                        RestingHeartRateRecord.BPM_AVG,
                    ),
                    repo.getLastSuccessfulQuery(RestingHeartRateRecord::class)
                        ?.let { lastQuery ->
                            TimeRangeFilter.Companion.after(lastQuery.toLocalDateTime())
                        } ?: TimeRangeFilter.Companion.between(
                        now.minusHours(1).toLocalDateTime(), now.toLocalDateTime()
                    ),
                    Duration.ofMinutes(15)
                )?.also { records ->
                    repo.postHeartRateAggregateData(
                        records,
                        RestingHeartRateRecord::class
                    ) { error, successful ->
                        if (successful)
                            saveQuery(RestingHeartRateRecord::class, successful)

                        cont.resume(Unit)
                        this.cancel()
                    }
                } ?: cont.resume(Unit)
                this.cancel()
            }
        }
    }
}