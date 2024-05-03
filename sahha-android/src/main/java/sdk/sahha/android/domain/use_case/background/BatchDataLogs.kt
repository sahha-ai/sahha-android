package sdk.sahha.android.domain.use_case.background

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
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.RespiratoryRateRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.Vo2MaxRecord
import androidx.health.connect.client.records.WeightRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import sdk.sahha.android.common.Constants
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.common.Session
import sdk.sahha.android.data.mapper.toBloodPressureDiastolic
import sdk.sahha.android.data.mapper.toBloodPressureSystolic
import sdk.sahha.android.data.mapper.toSahhaDataLog
import sdk.sahha.android.data.mapper.toSahhaDataLogDto
import sdk.sahha.android.data.mapper.toSahhaLogDto
import sdk.sahha.android.data.mapper.toStepsHealthConnect
import sdk.sahha.android.domain.model.data_log.SahhaDataLog
import sdk.sahha.android.domain.model.health_connect.HealthConnectQuery
import sdk.sahha.android.domain.model.steps.StepsHealthConnect
import sdk.sahha.android.domain.model.steps.toSahhaDataLogAsChildLog
import sdk.sahha.android.domain.model.steps.toSahhaDataLogAsParentLog
import sdk.sahha.android.domain.repository.BatchedDataRepo
import sdk.sahha.android.domain.repository.HealthConnectRepo
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.reflect.KClass

internal class BatchDataLogs @Inject constructor(
    private val batchRepo: BatchedDataRepo,
    private val healthConnectRepo: HealthConnectRepo,
    private val timeManager: SahhaTimeManager
) {
    private var batchJobs = emptyList<Job>()
    private val syncJob = CoroutineScope(Dispatchers.Default + Job())
    private val existingBatch by lazy { runBlocking { batchRepo.getBatchedData() } }
    suspend operator fun invoke() {
        if (Session.hcQueryInProgress) return

        val granted = healthConnectRepo.getGrantedPermissions()
        granted.forEach {
            when (it) {
                HealthPermission.getReadPermission(StepsRecord::class) -> {
                    val recordType = StepsRecord::class
                    batchJobs += newBatchJob().launch {
                        val records = healthConnectRepo.getChangedRecords(
                            recordType,
                            healthConnectRepo.getExistingChangesToken(recordType)
                        )
                            ?: healthConnectRepo.getCurrentDayRecords(recordType)
                        records?.also { batchStepData(records = it) }
                    }
                }

                HealthPermission.getReadPermission(SleepSessionRecord::class) -> {
                    val recordType = SleepSessionRecord::class
                    batchJobs += newBatchJob().launch {
                        val records = detectRecords(recordType)
                        records?.also { r ->
                            val sessions =
                                r.map { record -> (record as SleepSessionRecord).toSahhaDataLogDto() }
                            val stages = r.map { session ->
                                (session as SleepSessionRecord).stages.map {
                                    it.toSahhaDataLog(session)
                                }
                            }
                            val stagesFlattened = stages.flatten()

                            batchRepo.saveBatchedData(sessions + stagesFlattened)
                            saveQuery(recordType)
                        }
                    }
                }

                HealthPermission.getReadPermission(HeartRateRecord::class) -> {
                    val recordType = HeartRateRecord::class
                    batchJobs += newBatchJob().launch {
                        val records = detectRecords(recordType)
                        records?.also { r ->
                            val batched =
                                r.map { record ->
                                    (record as HeartRateRecord).samples.map { sample ->
                                        sample.toSahhaDataLog(record)
                                    }
                                }

                            batchRepo.saveBatchedData(batched.flatten())
                            saveQuery(recordType)
                        }
                    }
                }

                HealthPermission.getReadPermission(RestingHeartRateRecord::class) -> {
                    val recordType = RestingHeartRateRecord::class
                    batchJobs += newBatchJob().launch {
                        val records = detectRecords(recordType)
                        records?.also { r ->
                            batchRepo.saveBatchedData(r.map { record -> (record as RestingHeartRateRecord).toSahhaLogDto() })
                            saveQuery(recordType)
                        }
                    }
                }

                HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class) -> {
                    val recordType = HeartRateVariabilityRmssdRecord::class
                    batchJobs += newBatchJob().launch {
                        val records = detectRecords(recordType)
                        records?.also { r ->
                            batchRepo.saveBatchedData(r.map { record -> (record as HeartRateVariabilityRmssdRecord).toSahhaDataLogDto() })
                            saveQuery(recordType)
                        }
                    }
                }

                HealthPermission.getReadPermission(BloodGlucoseRecord::class) -> {
                    val recordType = BloodGlucoseRecord::class
                    batchJobs += newBatchJob().launch {
                        val records = detectRecords(recordType)
                        records?.also { r ->
                            batchRepo.saveBatchedData(r.map { record -> (record as BloodGlucoseRecord).toSahhaDataLogDto() })
                            saveQuery(recordType)
                        }
                    }
                }

                HealthPermission.getReadPermission(BloodPressureRecord::class) -> {
                    val recordType = BloodPressureRecord::class
                    batchJobs += newBatchJob().launch {
                        val records = detectRecords(recordType)
                        records?.also { r ->
                            val diastolic =
                                r.map { dr -> (dr as BloodPressureRecord).toBloodPressureDiastolic() }
                            val systolic =
                                r.map { sr -> (sr as BloodPressureRecord).toBloodPressureSystolic() }

                            batchRepo.saveBatchedData(diastolic + systolic)
                            saveQuery(recordType)
                        }
                    }
                }

                HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class) -> {
                    val recordType = ActiveCaloriesBurnedRecord::class
                    batchJobs += newBatchJob().launch {
                        val records = detectRecords(recordType)
                        records?.also { r ->
                            batchRepo.saveBatchedData(r.map { (it as ActiveCaloriesBurnedRecord).toSahhaDataLogDto() })
                            saveQuery(recordType)
                        }
                    }
                }

                HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class) -> {
                    val recordType = TotalCaloriesBurnedRecord::class
                    batchJobs += newBatchJob().launch {
                        val records = detectRecords(recordType)
                        records?.also { r ->
                            batchRepo.saveBatchedData(r.map { (it as TotalCaloriesBurnedRecord).toSahhaDataLogDto() })
                            saveQuery(recordType)
                        }
                    }
                }

                HealthPermission.getReadPermission(OxygenSaturationRecord::class) -> {
                    val recordType = OxygenSaturationRecord::class
                    batchJobs += newBatchJob().launch {
                        val records = detectRecords(recordType)
                        records?.also { r ->
                            batchRepo.saveBatchedData(r.map { (it as OxygenSaturationRecord).toSahhaDataLogDto() })
                            saveQuery(recordType)
                        }
                    }
                }

                HealthPermission.getReadPermission(Vo2MaxRecord::class) -> {
                    val recordType = Vo2MaxRecord::class
                    batchJobs += newBatchJob().launch {
                        val records = detectRecords(recordType)
                        records?.also { r ->
                            batchRepo.saveBatchedData(r.map { (it as Vo2MaxRecord).toSahhaDataLogDto() })
                            saveQuery(recordType)
                        }
                    }
                }

                HealthPermission.getReadPermission(BasalMetabolicRateRecord::class) -> {
                    val recordType = BasalMetabolicRateRecord::class
                    batchJobs += newBatchJob().launch {
                        val records = detectRecords(recordType)
                        records?.also { r ->
                            batchRepo.saveBatchedData(r.map { (it as BasalMetabolicRateRecord).toSahhaDataLogDto() })
                            saveQuery(recordType)
                        }
                    }
                }

                HealthPermission.getReadPermission(BodyFatRecord::class) -> {
                    val recordType = BodyFatRecord::class
                    batchJobs += newBatchJob().launch {
                        val records = detectRecords(recordType)
                        records?.also { r ->
                            batchRepo.saveBatchedData(r.map { (it as BodyFatRecord).toSahhaDataLogDto() })
                            saveQuery(recordType)
                        }
                    }
                }

                HealthPermission.getReadPermission(BodyWaterMassRecord::class) -> {
                    val recordType = BodyWaterMassRecord::class
                    batchJobs += newBatchJob().launch {
                        val records = detectRecords(recordType)
                        records?.also { r ->
                            batchRepo.saveBatchedData(r.map { (it as BodyWaterMassRecord).toSahhaDataLogDto() })
                            saveQuery(recordType)
                        }
                    }
                }

                HealthPermission.getReadPermission(LeanBodyMassRecord::class) -> {
                    val recordType = LeanBodyMassRecord::class
                    batchJobs += newBatchJob().launch {
                        val records = detectRecords(recordType)
                        records?.also { r ->
                            batchRepo.saveBatchedData(r.map { (it as LeanBodyMassRecord).toSahhaDataLogDto() })
                            saveQuery(recordType)
                        }
                    }
                }

                HealthPermission.getReadPermission(HeightRecord::class) -> {
                    val recordType = HeightRecord::class
                    batchJobs += newBatchJob().launch {
                        val records = detectRecords(recordType)
                        records?.also { r ->
                            batchRepo.saveBatchedData(r.map { (it as HeightRecord).toSahhaDataLogDto() })
                            saveQuery(recordType)
                        }
                    }
                }

                HealthPermission.getReadPermission(WeightRecord::class) -> {
                    val recordType = WeightRecord::class
                    batchJobs += newBatchJob().launch {
                        val records = detectRecords(recordType)
                        records?.also { r ->
                            batchRepo.saveBatchedData(r.map { (it as WeightRecord).toSahhaDataLogDto() })
                            saveQuery(recordType)
                        }
                    }
                }

                HealthPermission.getReadPermission(RespiratoryRateRecord::class) -> {
                    val recordType = RespiratoryRateRecord::class
                    batchJobs += newBatchJob().launch {
                        val records = detectRecords(recordType)
                        records?.also { r ->
                            batchRepo.saveBatchedData(r.map { (it as RespiratoryRateRecord).toSahhaDataLogDto() })
                            saveQuery(recordType)
                        }
                    }
                }

                HealthPermission.getReadPermission(BoneMassRecord::class) -> {
                    val recordType = BoneMassRecord::class
                    batchJobs += newBatchJob().launch {
                        val records = detectRecords(recordType)
                        records?.also { r ->
                            batchRepo.saveBatchedData(r.map { (it as BoneMassRecord).toSahhaDataLogDto() })
                            saveQuery(recordType)
                        }
                    }
                }

                HealthPermission.getReadPermission(FloorsClimbedRecord::class) -> {
                    val recordType = FloorsClimbedRecord::class
                    batchJobs += newBatchJob().launch {
                        val records = detectRecords(recordType)
                        records?.also { r ->
                            batchRepo.saveBatchedData(r.map { (it as FloorsClimbedRecord).toSahhaDataLogDto() })
                            saveQuery(recordType)
                        }
                    }
                }

                HealthPermission.getReadPermission(BodyTemperatureRecord::class) -> {
                    val recordType = BodyTemperatureRecord::class
                    batchJobs += newBatchJob().launch {
                        val records = detectRecords(recordType)
                        records?.also { r ->
                            batchRepo.saveBatchedData(r.map { (it as BodyTemperatureRecord).toSahhaDataLogDto() })
                            saveQuery(recordType)
                        }
                    }
                }

                HealthPermission.getReadPermission(BasalBodyTemperatureRecord::class) -> {
                    val recordType = BasalBodyTemperatureRecord::class
                    batchJobs += newBatchJob().launch {
                        val records = detectRecords(recordType)
                        records?.also { r ->
                            batchRepo.saveBatchedData(r.map { (it as BasalBodyTemperatureRecord).toSahhaDataLogDto() })
                            saveQuery(recordType)
                        }
                    }
                }

                HealthPermission.getReadPermission(ExerciseSessionRecord::class) -> {
                    val recordType = ExerciseSessionRecord::class
                    batchJobs += newBatchJob().launch {
                        val records = detectRecords(recordType)
                        records?.also { r ->
                            val batched = r.flatMap { exercise ->
                                listOf((exercise as ExerciseSessionRecord).toSahhaDataLogDto()) +
                                        exercise.laps.map { lap -> lap.toSahhaDataLogDto(exercise) } +
                                        exercise.segments.map { segment ->
                                            segment.toSahhaDataLogDto(
                                                exercise
                                            )
                                        }
                            }
                            batchRepo.saveBatchedData(batched)
                            saveQuery(recordType)
                        }
                    }
                }
            }
        }

        batchJobs.joinAll()
    }

    private suspend fun <T : Record> detectRecords(recordType: KClass<T>) =
        healthConnectRepo.getChangedRecords(
            recordType,
            healthConnectRepo.getExistingChangesToken(recordType)
        ) ?: healthConnectRepo.getNewRecords(recordType)

    private suspend fun getLastCustomQuery(customId: String): HealthConnectQuery? {
        return healthConnectRepo.getLastCustomQuery(customId)
    }

    private suspend fun saveCustomStepsQuery(customId: String) {
        healthConnectRepo.saveCustomSuccessfulQuery(customId, ZonedDateTime.now())
    }

    private suspend fun <T : Record> saveQuery(
        dataType: KClass<T>,
        timestamp: ZonedDateTime = ZonedDateTime.now()
    ) {
        healthConnectRepo.saveLastSuccessfulQuery(
            dataType,
            healthConnectRepo.successfulQueryTimestamps[
                HealthPermission
                    .getReadPermission(dataType)
            ] ?: timestamp
        )
    }

    private suspend fun batchStepData(records: List<Record>) {
        var batchData = mutableListOf<SahhaDataLog>()
        val typicalData: List<StepsHealthConnect>
        val edgeCaseData: List<StepsHealthConnect>
        val localSteps = healthConnectRepo.getAllStepsHc()
        val queries = records.map { qr -> (qr as StepsRecord).toStepsHealthConnect() }

        edgeCaseData =
            filterData(queries) { data ->
                isTotalDayTimestamps(data)
            }
        typicalData =
            filterData(queries) { data ->
                !isTotalDayTimestamps(data)
            }

        // Handles edge cases like daily total steps from Samsung Health
        batchData = processEdgeCase(
            edgeCaseData = edgeCaseData, localSteps = localSteps, batchData = batchData
        )

        // Handles duplicate steps for typical step data
        batchData =
            processSteps(
                queries = typicalData,
                localSteps = localSteps,
                batchData = batchData
            )

        saveCustomStepsQuery(Constants.CUSTOM_STEPS_QUERY_ID)
        if (batchData.isEmpty()) return

        batchRepo.saveBatchedData(batchData)
        saveQuery(StepsRecord::class)
        checkAndClearLastMidnightSteps()
    }

    private fun isTotalDayTimestamps(data: StepsHealthConnect): Boolean {
        val start = timeManager.ISOToZonedDateTime(data.startDateTime).toLocalTime()
        val end = timeManager.ISOToZonedDateTime(data.endDateTime).toLocalTime()
        val endOfDay = LocalTime.MIDNIGHT.minus(10, ChronoUnit.MILLIS)

        return start == LocalTime.MIDNIGHT
                && end == endOfDay
    }

    private fun filterData(
        data: List<StepsHealthConnect>,
        toCondition: (StepsHealthConnect) -> Boolean
    ): List<StepsHealthConnect> {
        return data.filter { d -> toCondition(d) }
    }

    private suspend fun processSteps(
        queries: List<StepsHealthConnect>,
        localSteps: List<StepsHealthConnect>,
        batchData: MutableList<SahhaDataLog>,
    ): MutableList<SahhaDataLog> {
        var processedData = batchData

        for (record in queries) {
            val localMatch =
                localSteps.find { local -> local.metaId == record.metaId }

            if (localMatch == null) {
                processedData = saveLocallyAndPrepBatch(batchData, record)
                continue
            }
            if (localMatch.modifiedDateTime == record.modifiedDateTime) {
                continue
            }
        }

        return processedData
    }

    private suspend fun processEdgeCase(
        edgeCaseData: List<StepsHealthConnect>,
        localSteps: List<StepsHealthConnect>,
        batchData: MutableList<SahhaDataLog>,
    ): MutableList<SahhaDataLog> {
        var processedData = batchData
        for (record in edgeCaseData) {
            val localMatch = localSteps.find { local ->
                local.metaId == record.metaId
            }

            if (localMatch == null) {
                processedData = saveLocalAndPrepDiffForBatch(
                    toPost = processedData,
                    newRecord = record
                )
                continue
            }

            if (localMatch.modifiedDateTime == record.modifiedDateTime) {
                continue
            }

            // Modified time is different
            processedData =
                saveLocalAndPrepDiffForBatch(
                    toPost = processedData,
                    newRecord = record,
                    local = localMatch
                )
        }
        return processedData
    }

    private fun newBatchJob(): CoroutineScope {
        return CoroutineScope(Dispatchers.Default + Job())
    }

    private suspend fun saveLocallyAndPrepBatch(
        toPost: MutableList<SahhaDataLog>,
        record: StepsHealthConnect
    ): MutableList<SahhaDataLog> {
        healthConnectRepo.saveStepsHc(record)
        toPost.add(record.toSahhaDataLogAsParentLog())
        return toPost
    }

    private suspend fun saveLocalAndPrepDiffForBatch(
        toPost: MutableList<SahhaDataLog>,
        newRecord: StepsHealthConnect,
        local: StepsHealthConnect? = null
    ): MutableList<SahhaDataLog> {
        healthConnectRepo.saveStepsHc(newRecord)
        val lastSuccessfulCustomQueryEpoch =
            getLastCustomQuery(Constants.CUSTOM_STEPS_QUERY_ID)?.lastSuccessfulTimeStampEpochMillis
        val lastSuccessfulCustomQuery =
            lastSuccessfulCustomQueryEpoch?.let { epoch -> timeManager.epochMillisToISO(epoch) }
        val now = ZonedDateTime.now()

        toPost.add(
            local?.let { loc ->
                StepsHealthConnect(
                    metaId = newRecord.metaId,
                    dataType = newRecord.dataType,
                    count = newRecord.count - loc.count,
                    source = newRecord.source,
                    startDateTime = loc.modifiedDateTime,
                    endDateTime = newRecord.modifiedDateTime,
                    modifiedDateTime = newRecord.modifiedDateTime,
                    recordingMethod = newRecord.recordingMethod,
                    deviceType = newRecord.deviceType,
                    deviceManufacturer = newRecord.deviceManufacturer,
                    deviceModel = newRecord.deviceModel
                ).toSahhaDataLogAsChildLog()
            } ?: StepsHealthConnect(
                metaId = newRecord.metaId,
                dataType = newRecord.dataType,
                count = newRecord.count,
                source = newRecord.source,
                startDateTime = lastSuccessfulCustomQuery?.let { query ->
                    if (query > newRecord.modifiedDateTime)
                        newRecord.startDateTime
                    else query
                } ?: newRecord.startDateTime,
                endDateTime = newRecord.modifiedDateTime,
                modifiedDateTime = newRecord.modifiedDateTime,
                recordingMethod = newRecord.recordingMethod,
                deviceType = newRecord.deviceType,
                deviceManufacturer = newRecord.deviceManufacturer,
                deviceModel = newRecord.deviceModel
            ).toSahhaDataLogAsParentLog()
        )
        return toPost
    }

    private suspend fun checkAndClearLastMidnightSteps(
        lastSuccessfulQuery: LocalDate? = runBlocking {
            healthConnectRepo.getLastSuccessfulQuery(StepsRecord::class)?.toLocalDate()
        },
        lastMidnight: LocalDateTime = LocalDateTime.of(lastSuccessfulQuery, LocalTime.MIDNIGHT),
        currentMidnight: LocalDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT)
    ) {
        lastSuccessfulQuery?.also { d ->
            if (currentMidnight > lastMidnight) healthConnectRepo.clearStepsBeforeHc(currentMidnight)
        }
    }
}