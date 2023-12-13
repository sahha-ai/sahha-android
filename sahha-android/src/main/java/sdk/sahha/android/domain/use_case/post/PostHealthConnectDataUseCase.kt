package sdk.sahha.android.domain.use_case.post

import android.util.Log
import androidx.health.connect.client.aggregate.AggregateMetric
import androidx.health.connect.client.aggregate.AggregationResultGroupedByDuration
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.BasalMetabolicRateRecord
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.BodyWaterMassRecord
import androidx.health.connect.client.records.BoneMassRecord
import androidx.health.connect.client.records.FloorsClimbedRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.HeightRecord
import androidx.health.connect.client.records.LeanBodyMassRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.RespiratoryRateRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.Vo2MaxRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Response
import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.Session
import sdk.sahha.android.common.TokenBearer
import sdk.sahha.android.data.mapper.toSahhaDataLogDto
import sdk.sahha.android.data.mapper.toStepsHealthConnect
import sdk.sahha.android.data.remote.SahhaApi
import sdk.sahha.android.di.IoScope
import sdk.sahha.android.domain.model.dto.SahhaDataLogDto
import sdk.sahha.android.domain.model.steps.StepsHealthConnect
import sdk.sahha.android.domain.repository.AuthRepo
import sdk.sahha.android.domain.repository.HealthConnectRepo
import sdk.sahha.android.source.SahhaConverterUtility
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.reflect.KClass

private const val tag = "PostHealthConnectDataUseCase"

class PostHealthConnectDataUseCase @Inject constructor(
    private val authRepo: AuthRepo,
    private val repo: HealthConnectRepo,
    private val sahhaErrorLogger: SahhaErrorLogger,
    private val api: SahhaApi,
    @IoScope private val ioScope: CoroutineScope
) {
    private val results = mutableListOf<Boolean>()
    private val errors = mutableListOf<String>()

    suspend operator fun invoke(
        callback: ((error: String?, successful: Boolean) -> Unit)
    ) {
        if (!Session.hcQueryInProgress)
            queryAndPostHealthConnectData(callback)
    }

    private suspend fun queryAndPostHealthConnectData(
        callback: ((error: String?, successful: Boolean) -> Unit)
    ) {
        Session.hcQueryInProgress = true
        val granted = repo.getGrantedPermissions()
        results.clear()
        errors.clear()

        granted.forEach {
            when (it) {
                HealthPermission.getReadPermission(StepsRecord::class) -> {
                    suspendCoroutine<Unit> { cont ->
                        ioScope.launch {
                            repo.getCurrentDayRecords(StepsRecord::class)?.also { records ->
                                var postData = mutableListOf<StepsHealthConnect>()
                                val local = repo.getAllStepsHc()

                                val queries = records.map { qr -> qr.toStepsHealthConnect() }

                                for (record in queries) {
                                    val localMatch =
                                        local.find { l -> l.metaId == record.metaId }

                                    if (localMatch == null) {
                                        postData = saveLocallyAndPrepPost(postData, record)
                                        continue
                                    }
                                    if (localMatch.modifiedDateTime == record.modifiedDateTime) {
                                        continue
                                    }

                                    // Modified time is different
                                    postData =
                                        saveLocalAndPrepDiffPost(
                                            postData,
                                            localMatch,
                                            record
                                        )
                                }


                                if (postData.isEmpty()) {
                                    cont.resume(Unit)

                                    return@launch
                                }

                                repo.postStepData(postData) { error, successful ->
                                    if (successful) clearLastMidnightSteps()
                                    processPostResponse(
                                        error, successful,
                                        "Posted step data successfully.",
                                        records, StepsRecord::class
                                    )
                                    cont.resume(Unit)
                                }
                            } ?: cont.resume(Unit)
                        }
                    }
                }

                HealthPermission.getReadPermission(SleepSessionRecord::class) -> {
                    suspendCoroutine<Unit> { cont ->
                        ioScope.launch {
                            repo.getNewRecords(SleepSessionRecord::class)?.also { records ->
                                repo.postSleepSessionData(records) { error, successful ->
                                    processPostResponse(
                                        error, successful,
                                        "Posted sleep data successfully.",
                                        records, SleepSessionRecord::class
                                    )
                                    cont.resume(Unit)
                                }
                            } ?: cont.resume(Unit)
                        }
                    }
                }

                HealthPermission.getReadPermission(HeartRateRecord::class) -> {
                    suspendCoroutine<Unit> { cont ->
                        ioScope.launch {
                            repo.getNewRecords(HeartRateRecord::class)?.also { records ->
                                repo.postHeartRateData(records) { error, successful ->
                                    processPostResponse(
                                        error, successful,
                                        "Posted heart rate data successfully.",
                                        records, HeartRateRecord::class
                                    )
                                    cont.resume(Unit)
                                }
                            } ?: cont.resume(Unit)
                        }
                    }
                }

                HealthPermission.getReadPermission(RestingHeartRateRecord::class) -> {
                    suspendCoroutine<Unit> { cont ->
                        ioScope.launch {
                            repo.getNewRecords(RestingHeartRateRecord::class)
                                ?.also { records ->
                                    repo.postRestingHeartRateData(records) { error, successful ->
                                        processPostResponse(
                                            error, successful,
                                            "Posted resting heart rate data successfully.",
                                            records, RestingHeartRateRecord::class
                                        )
                                        cont.resume(Unit)
                                    }
                                } ?: cont.resume(Unit)
                        }
                    }
                }

                HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class) -> {
                    suspendCoroutine<Unit> { cont ->
                        ioScope.launch {
                            repo.getNewRecords(HeartRateVariabilityRmssdRecord::class)
                                ?.also { records ->
                                    repo.postHeartRateVariabilityRmssdData(records) { error, successful ->
                                        processPostResponse(
                                            error, successful,
                                            "Posted heart rate variability data successfully.",
                                            records, HeartRateVariabilityRmssdRecord::class
                                        )
                                        cont.resume(Unit)
                                    }
                                } ?: cont.resume(Unit)
                        }
                    }
                }

                HealthPermission.getReadPermission(BloodGlucoseRecord::class) -> {
                    suspendCoroutine<Unit> { cont ->
                        ioScope.launch {
                            repo.getNewRecords(BloodGlucoseRecord::class)?.also { records ->
                                repo.postBloodGlucoseData(records) { error, successful ->
                                    processPostResponse(
                                        error,
                                        successful,
                                        "Posted blood glucose data successfully.",
                                        records,
                                        BloodGlucoseRecord::class
                                    )
                                    cont.resume(Unit)
                                }
                            } ?: cont.resume(Unit)
                        }
                    }
                }

                HealthPermission.getReadPermission(BloodPressureRecord::class) -> {
                    suspendCoroutine<Unit> { cont ->
                        ioScope.launch {
                            repo.getNewRecords(BloodPressureRecord::class)
                                ?.also { records ->
                                    repo.postBloodPressureData(records) { error, successful ->
                                        processPostResponse(
                                            error,
                                            successful,
                                            "Posted blood pressure data successfully.",
                                            records,
                                            BloodPressureRecord::class
                                        )
                                        cont.resume(Unit)
                                    }
                                } ?: cont.resume(Unit)
                        }
                    }
                }

                HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class) -> {
                    val recordType = ActiveCaloriesBurnedRecord::class
                    suspendCoroutine<Unit> { cont ->
                        ioScope.launch {
                            repo.getNewRecords(recordType)?.also { records ->
                                repo.postActiveEnergyBurned(
                                    records,
                                ) { error, successful ->
                                    processPostResponse(
                                        error, successful,
                                        "Posted active energy burned successfully.",
                                        records,
                                        recordType
                                    )
                                    cont.resume(Unit)
                                }
                            } ?: cont.resume(Unit)
                        }
                    }
                }

                HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class) -> {
                    val recordType = TotalCaloriesBurnedRecord::class
                    suspendCoroutine<Unit> { cont ->
                        ioScope.launch {
                            repo.getNewRecords(recordType)?.also { records ->
                                repo.postTotalEnergyBurned(
                                    records,
                                ) { error, successful ->
                                    processPostResponse(
                                        error, successful,
                                        "Posted total energy burned successfully.",
                                        records,
                                        recordType
                                    )
                                    cont.resume(Unit)
                                }
                            } ?: cont.resume(Unit)
                        }
                    }
                }

                HealthPermission.getReadPermission(OxygenSaturationRecord::class) -> {
                    suspendCoroutine<Unit> { cont ->
                        ioScope.launch {
                            repo.getNewRecords(OxygenSaturationRecord::class)?.also { records ->
                                repo.postOxygenSaturation(records) { error, successful ->
                                    processPostResponse(
                                        error, successful,
                                        "Posted oxygen saturation data successfully.",
                                        records, OxygenSaturationRecord::class
                                    )
                                    cont.resume(Unit)
                                }
                            } ?: cont.resume(Unit)
                        }
                    }
                }

                HealthPermission.getReadPermission(Vo2MaxRecord::class) -> {
                    suspendCoroutine<Unit> { cont ->
                        ioScope.launch {
                            repo.getNewRecords(Vo2MaxRecord::class)?.also { records ->
                                repo.postVo2MaxData(records) { error, successful ->
                                    processPostResponse(
                                        error, successful,
                                        "Posted VO2 max data successfully.",
                                        records, Vo2MaxRecord::class
                                    )
                                    cont.resume(Unit)
                                }
                            } ?: cont.resume(Unit)
                        }
                    }
                }

                HealthPermission.getReadPermission(BasalMetabolicRateRecord::class) -> {
                    val recordType = BasalMetabolicRateRecord::class
                    suspendCoroutine { cont ->
                        ioScope.launch {
                            repo.getNewRecords(recordType)?.also { records ->
                                repo.postData(
                                    data = records,
                                    getResponse = { chunk ->
                                        val token = authRepo.getToken() ?: ""
                                        val chunked = chunk.map { it.toSahhaDataLogDto() }
                                        api.postBasalMetabolicRate(
                                            TokenBearer(token),
                                            chunked
                                        )
                                    }
                                ) { error, successful ->
                                    processPostResponse(
                                        error, successful,
                                        "Posted basal metabolic rate successfully.",
                                        records, recordType
                                    )
                                    cont.resume(Unit)
                                }
                            } ?: cont.resume(Unit)
                        }
                    }
                }

                HealthPermission.getReadPermission(BodyFatRecord::class) -> {
                    val recordType = BodyFatRecord::class
                    suspendCoroutine { cont ->
                        ioScope.launch {
                            repo.getNewRecords(recordType)?.also { records ->
                                repo.postData(
                                    data = records,
                                    getResponse = { chunk ->
                                        val token = authRepo.getToken() ?: ""
                                        val chunked = chunk.map { it.toSahhaDataLogDto() }
                                        api.postBodyFat(
                                            TokenBearer(token),
                                            chunked
                                        )
                                    }
                                ) { error, successful ->
                                    processPostResponse(
                                        error, successful,
                                        "Posted body fat successfully.",
                                        records, recordType
                                    )
                                    cont.resume(Unit)
                                }
                            } ?: cont.resume(Unit)
                        }
                    }
                }

                HealthPermission.getReadPermission(BodyWaterMassRecord::class) -> {
                    val recordType = BodyWaterMassRecord::class
                    suspendCoroutine { cont ->
                        ioScope.launch {
                            repo.getNewRecords(recordType)?.also { records ->
                                repo.postData(
                                    data = records,
                                    getResponse = { chunk ->
                                        val token = authRepo.getToken() ?: ""
                                        val chunked = chunk.map { it.toSahhaDataLogDto() }
                                        api.postBodyWaterMass(
                                            TokenBearer(token),
                                            chunked
                                        )
                                    }
                                ) { error, successful ->
                                    processPostResponse(
                                        error, successful,
                                        "Posted body water mass successfully.",
                                        records, recordType
                                    )
                                    cont.resume(Unit)
                                }
                            } ?: cont.resume(Unit)
                        }
                    }
                }

                HealthPermission.getReadPermission(LeanBodyMassRecord::class) -> {
                    val recordType = LeanBodyMassRecord::class
                    suspendCoroutine { cont ->
                        ioScope.launch {
                            repo.getNewRecords(recordType)?.also { records ->
                                repo.postData(
                                    data = records,
                                    getResponse = { chunk ->
                                        val token = authRepo.getToken() ?: ""
                                        val chunked = chunk.map { it.toSahhaDataLogDto() }
                                        api.postLeanBodyMass(
                                            TokenBearer(token),
                                            chunked
                                        )
                                    }
                                ) { error, successful ->
                                    processPostResponse(
                                        error, successful,
                                        "Posted lean body mass successfully.",
                                        records, recordType
                                    )
                                    cont.resume(Unit)
                                }
                            } ?: cont.resume(Unit)
                        }
                    }
                }

                HealthPermission.getReadPermission(HeightRecord::class) -> {
                    val recordType = HeightRecord::class
                    suspendCoroutine { cont ->
                        ioScope.launch {
                            repo.getNewRecords(recordType)?.also { records ->
                                repo.postData(
                                    data = records,
                                    getResponse = { chunk ->
                                        val token = authRepo.getToken() ?: ""
                                        val chunked = chunk.map { it.toSahhaDataLogDto() }
                                        api.postHeight(
                                            TokenBearer(token),
                                            chunked
                                        )
                                    }
                                ) { error, successful ->
                                    processPostResponse(
                                        error, successful,
                                        "Posted height successfully.",
                                        records, recordType
                                    )
                                    cont.resume(Unit)
                                }
                            } ?: cont.resume(Unit)
                        }
                    }
                }

                HealthPermission.getReadPermission(WeightRecord::class) -> {
                    val recordType = WeightRecord::class
                    suspendCoroutine { cont ->
                        ioScope.launch {
                            repo.getNewRecords(recordType)?.also { records ->
                                repo.postData(
                                    data = records,
                                    getResponse = { chunk ->
                                        val token = authRepo.getToken() ?: ""
                                        val chunked = chunk.map { it.toSahhaDataLogDto() }
                                        api.postWeight(
                                            TokenBearer(token),
                                            chunked
                                        )
                                    }
                                ) { error, successful ->
                                    processPostResponse(
                                        error, successful,
                                        "Posted weight successfully.",
                                        records, recordType
                                    )
                                    cont.resume(Unit)
                                }
                            } ?: cont.resume(Unit)
                        }
                    }
                }

                HealthPermission.getReadPermission(RespiratoryRateRecord::class) -> {
                    val recordType = RespiratoryRateRecord::class
                    suspendCoroutine { cont ->
                        ioScope.launch {
                            repo.getNewRecords(recordType)?.also { records ->
                                repo.postData(
                                    data = records,
                                    getResponse = { chunk ->
                                        val token = authRepo.getToken() ?: ""
                                        val chunked = chunk.map { it.toSahhaDataLogDto() }
                                        api.postRespiratoryRate(
                                            TokenBearer(token),
                                            chunked
                                        )
                                    }
                                ) { error, successful ->
                                    processPostResponse(
                                        error, successful,
                                        "Posted respiratory rate successfully.",
                                        records, recordType
                                    )
                                    cont.resume(Unit)
                                }
                            } ?: cont.resume(Unit)
                        }
                    }
                }

                HealthPermission.getReadPermission(BoneMassRecord::class) -> {
                    val recordType = BoneMassRecord::class
                    suspendCoroutine { cont ->
                        ioScope.launch {
                            repo.getNewRecords(recordType)?.also { records ->
                                repo.postData(
                                    data = records,
                                    getResponse = { chunk ->
                                        val token = authRepo.getToken() ?: ""
                                        val chunked = chunk.map { it.toSahhaDataLogDto() }
                                        api.postBoneMass(
                                            TokenBearer(token),
                                            chunked
                                        )
                                    }
                                ) { error, successful ->
                                    processPostResponse(
                                        error, successful,
                                        "Posted bone mass successfully.",
                                        records, recordType
                                    )
                                    cont.resume(Unit)
                                }
                            } ?: cont.resume(Unit)
                        }
                    }
                }

                HealthPermission.getReadPermission(FloorsClimbedRecord::class) -> {
                    val recordType = FloorsClimbedRecord::class
                    suspendCoroutine { cont ->
                        ioScope.launch {
                            repo.getNewRecords(recordType)?.also { records ->
                                repo.postData(
                                    data = records,
                                    getResponse = { chunk ->
                                        val token = authRepo.getToken() ?: ""
                                        val chunked = chunk.map { it.toSahhaDataLogDto() }
                                        api.postSahhaDataLogs(
                                            TokenBearer(token),
                                            chunked
                                        )
                                    }
                                ) { error, successful ->
                                    processPostResponse(
                                        error, successful,
                                        "Posted floors climbed successfully.",
                                        records, recordType
                                    )
                                    cont.resume(Unit)
                                }
                            } ?: cont.resume(Unit)
                        }
                    }
                }
            }
        }

        Session.hcQueryInProgress = false
        if (checkIsAllTrue(results))
            callback(null, true)
        else callback(sumErrors(errors), false)
    }

    internal suspend fun <R : Record, A> processPostResponse(
        error: String?,
        successful: Boolean,
        successfulLog: String,
        records: List<A>,
        recordType: KClass<R>
    ) {
        if (successful) {
            saveQuery(recordType, true)
            Log.i(tag, successfulLog)
            results.add(true)
            return
        }

        results.add(false)
        error?.also { e -> errors.add(e) }
        checkAndLogError(
            error,
            "queryAndPostHealthConnectData",
            SahhaConverterUtility.convertToJsonString(records, false)
        )
    }

    private fun checkIsAllTrue(results: List<Boolean>): Boolean {
        results.forEach { isTrue ->
            if (!isTrue) return false
        }
        return true
    }

    private fun sumErrors(errors: List<String>): String? {
        if (errors.isEmpty()) return null

        var summed = ""
        errors.forEach { e ->
            summed += "$e\n"
        }
        return summed
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
                deviceType = newRecord.deviceType,
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

    private fun checkAndLogError(error: String?, method: String, body: String) {
        error?.also { e ->
            sahhaErrorLogger.application(
                e, tag,
                method,
                body
            )
        }
    }

    private suspend fun postAggregateData(
        metrics: Set<AggregateMetric<*>>,
        lastQuery: ZonedDateTime?,
        lastDays: Long = 3,
        duration: Duration = Duration.ofMinutes(15),
        postData: suspend (records: List<AggregationResultGroupedByDuration>) -> Unit
    ) {
        ioScope.launch {
            val now = ZonedDateTime.now()
            repo.getAggregateRecordsByDuration(
                metrics,
                lastQuery
                    ?.let { query ->
                        TimeRangeFilter.Companion.after(query.toLocalDateTime())
                    } ?: TimeRangeFilter.Companion.between(
                    now.minusDays(lastDays).toLocalDateTime(), now.toLocalDateTime()
                ),
                duration
            )?.also { records -> postData(records) }
        }
    }
}