package sdk.sahha.android.suite

import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.metadata.DataOrigin
import androidx.health.connect.client.records.metadata.Metadata
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.di.AppModule
import sdk.sahha.android.di.MockAppComponent
import sdk.sahha.android.domain.use_case.background.BatchDataLogs
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaConverterUtility
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

internal class BatchStepsEdgeCaseTest {
    companion object {
        @JvmStatic
        @BeforeClass
        fun beforeClass() {
            Sahha.di = MockAppComponent()
        }
    }

    val time = SahhaTimeManager()

    val now = ZonedDateTime.now()
    val start = ZonedDateTime.of(
        LocalDateTime.of(
            LocalDate.now(),
            LocalTime.MIDNIGHT
        ),
        now.zone
    )
    val end = ZonedDateTime.of(
        LocalDateTime.of(
            LocalDate.now(),
            LocalTime.of(23, 59, 59, 999999999)
        ),
        now.zone
    )

    var data = mutableListOf<StepsRecord>()

    fun mockStepsPhase1(): List<StepsRecord> {
        data += StepsRecord(
            startTime = start.toInstant(),
            startZoneOffset = start.offset,
            endTime = end.toInstant(),
            endZoneOffset = end.offset,
            metadata = Metadata(
                id = "placeholder_0",
                dataOrigin = DataOrigin("com.placeholder.source"),
                lastModifiedTime = start.plusHours(8).toInstant()
            ),
            count = 1000,
        )

        data += StepsRecord(
            startTime = now.minusMinutes(30).toInstant(),
            startZoneOffset = start.offset,
            endTime = now.minusMinutes(15).toInstant(),
            endZoneOffset = end.offset,
            metadata = Metadata(
                id = "placeholder_1",
                dataOrigin = DataOrigin("com.placeholder.source.non.daily"),
                lastModifiedTime = now.minusMinutes(15).toInstant()
            ),
            count = 100,
        )

        data += StepsRecord(
            startTime = now.minusMinutes(15).toInstant(),
            startZoneOffset = start.offset,
            endTime = now.toInstant(),
            endZoneOffset = end.offset,
            metadata = Metadata(
                id = "placeholder_2",
                dataOrigin = DataOrigin("com.placeholder.source.non.daily"),
                lastModifiedTime = now.toInstant()
            ),
            count = 100,
        )

        data += StepsRecord(
            startTime = start.minusDays(3).toInstant(),
            startZoneOffset = start.offset,
            endTime = end.minusDays(3).toInstant(),
            endZoneOffset = end.offset,
            metadata = Metadata(
                id = "placeholder_3",
                dataOrigin = DataOrigin("com.placeholder.source"),
                lastModifiedTime = start.plusHours(8).toInstant()
            ),
            count = 1000,
        )

        return data
    }

    fun mockStepsPhase2(): List<StepsRecord> {
        data += StepsRecord(
            startTime = start.toInstant(),
            startZoneOffset = start.offset,
            endTime = end.toInstant(),
            endZoneOffset = end.offset,
            metadata = Metadata(
                id = "placeholder_0",
                dataOrigin = DataOrigin("com.placeholder.source"),
                lastModifiedTime = start.plusHours(8).plusMinutes(15).toInstant()
            ),
            count = 1050,
        )

        data += StepsRecord(
            startTime = start.minusDays(2).toInstant(),
            startZoneOffset = start.offset,
            endTime = end.minusDays(2).toInstant(),
            endZoneOffset = end.offset,
            metadata = Metadata(
                id = "placeholder_4",
                dataOrigin = DataOrigin("com.placeholder.source"),
                lastModifiedTime = start.plusHours(8).toInstant()
            ),
            count = 1000,
        )

        return data
    }

    fun mockStepsPhase3(): List<StepsRecord> {
        data += StepsRecord(
            startTime = start.toInstant(),
            startZoneOffset = start.offset,
            endTime = end.toInstant(),
            endZoneOffset = end.offset,
            metadata = Metadata(
                id = "placeholder_0",
                dataOrigin = DataOrigin("com.placeholder.source"),
                lastModifiedTime = start.plusHours(8).plusMinutes(30).toInstant()
            ),
            count = 1100,
        )

        data += StepsRecord(
            startTime = start.minusDays(1).toInstant(),
            startZoneOffset = start.offset,
            endTime = end.minusDays(1).toInstant(),
            endZoneOffset = end.offset,
            metadata = Metadata(
                id = "placeholder_5",
                dataOrigin = DataOrigin("com.placeholder.source"),
                lastModifiedTime = start.plusHours(8).toInstant()
            ),
            count = 1000,
        )

        return data
    }

    fun mockStepsPhase4(): List<StepsRecord> {
        data += StepsRecord(
            startTime = start.toInstant(),
            startZoneOffset = start.offset,
            endTime = end.toInstant(),
            endZoneOffset = end.offset,
            metadata = Metadata(
                id = "placeholder_0",
                dataOrigin = DataOrigin("com.placeholder.source"),
                lastModifiedTime = start.plusHours(8).plusMinutes(45).toInstant()
            ),
            count = 1150,
        )

        return data
    }

    @Test
    fun sameDay() = runTest {
        val now = ZonedDateTime.now()
        val midnight = ZonedDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT, now.offset)
//        val nowTruncated = now.toLocalDate()
        val startDateTimeTruncated =
            time.ISOToZonedDateTime(time.localDateTimeToISO(midnight.toLocalDateTime(), now.offset))
                .toLocalDate()
//        val isCurrentDay = nowTruncated == startDateTimeTruncated

        val nowIso = time.instantToIsoTime(now.toInstant())
        val nowTruncated = now.toInstant().truncatedTo(ChronoUnit.DAYS)
        val recTruncated = time.ISOToZonedDateTime(nowIso).toInstant().truncatedTo(
            ChronoUnit.DAYS
        )

        Assert.assertEquals(nowTruncated, recTruncated)
    }

    @Test
    fun test_phase1() = runTest {
        val batchDataLogs = BatchDataLogs(
            batchRepo = AppModule.mockBatchedDataRepo,
            healthConnectRepo = AppModule.mockHealthConnectRepo,
            timeManager = AppModule.mockSahhaTimeManager,
            mapper = AppModule.mockHealthConnectConstantsMapper
        )
        val phase1 = mockStepsPhase1()

        batchDataLogs.batchStepData(phase1)
        val batched = AppModule.mockBatchedDataRepo.getBatchedData()
        batched.forEach {
            val json = SahhaConverterUtility.convertToJsonString(it)
            println("$json\n\n")
        }
    }

    @Test
    fun test_phase2() = runTest {
        val batchDataLogs = BatchDataLogs(
            batchRepo = AppModule.mockBatchedDataRepo,
            healthConnectRepo = AppModule.mockHealthConnectRepo,
            timeManager = AppModule.mockSahhaTimeManager,
            mapper = AppModule.mockHealthConnectConstantsMapper
        )
        val phase2 = mockStepsPhase2()

        batchDataLogs.batchStepData(phase2)
        val batched = AppModule.mockBatchedDataRepo.getBatchedData()
        batched.forEach {
            val json = SahhaConverterUtility.convertToJsonString(it)
            println("$json\n\n")
        }
    }

    @Test
    fun test_phase3() = runTest {
        val batchDataLogs = BatchDataLogs(
            batchRepo = AppModule.mockBatchedDataRepo,
            healthConnectRepo = AppModule.mockHealthConnectRepo,
            timeManager = AppModule.mockSahhaTimeManager,
            mapper = AppModule.mockHealthConnectConstantsMapper
        )
        val phase3 = mockStepsPhase3()

        batchDataLogs.batchStepData(phase3)
        val batched = AppModule.mockBatchedDataRepo.getBatchedData()
        batched.forEach {
            val json = SahhaConverterUtility.convertToJsonString(it)
            println("$json\n\n")
        }
    }

    @Test
    fun test_phase4() = runTest {
        val batchDataLogs = BatchDataLogs(
            batchRepo = AppModule.mockBatchedDataRepo,
            healthConnectRepo = AppModule.mockHealthConnectRepo,
            timeManager = AppModule.mockSahhaTimeManager,
            mapper = AppModule.mockHealthConnectConstantsMapper
        )
        val phase4 = mockStepsPhase4()

        batchDataLogs.batchStepData(phase4)
        val batched = AppModule.mockBatchedDataRepo.getBatchedData()
        batched.forEach {
            val json = SahhaConverterUtility.convertToJsonString(it)
            println("$json\n\n")
        }
    }
}