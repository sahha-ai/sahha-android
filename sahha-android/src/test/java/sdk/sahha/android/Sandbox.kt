package sdk.sahha.android

//import sdk.sahha.android.domain.use_case.background.LogAppAliveState
import android.icu.util.Calendar
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.common.toMidnight
import sdk.sahha.android.common.toNoon
import sdk.sahha.android.di.AppModule
import sdk.sahha.android.domain.model.data_log.SahhaDataLog
import sdk.sahha.android.domain.use_case.CalculateBatchLimit
import sdk.sahha.android.source.SahhaConverterUtility
import sdk.sahha.android.source.SahhaSensor
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.random.Random

class Sandbox {
//    @Test
//    fun appAlive() = runTest {
//        val now = ZonedDateTime.now()
//        val appAliveState = LogAppAliveState(mockk(), mockk())
//        var alreadyLogged = appAliveState(
//            HealthConnectQuery(
//                Constants.APP_ALIVE_QUERY_ID,
//                now.toInstant().toEpochMilli()
//            )
//        )
//        println(alreadyLogged)
//        Assert.assertEquals(true, alreadyLogged)
//
//        alreadyLogged = appAliveState(
//            HealthConnectQuery(
//                Constants.APP_ALIVE_QUERY_ID,
//                ZonedDateTime.of(
//                    LocalDateTime.of(
//                        LocalDate.now(),
//                        LocalTime.MIDNIGHT.minusSeconds(1)
//                    ),
//                    now.zone
//                ).toInstant().toEpochMilli()
//            )
//        )
//
//
//        println(alreadyLogged)
//        Assert.assertEquals(false, alreadyLogged)
//
//        alreadyLogged = appAliveState(
//            HealthConnectQuery(
//                Constants.APP_ALIVE_QUERY_ID,
//                now.minusDays(1).toInstant().toEpochMilli()
//            )
//        )
//        println(alreadyLogged)
//        Assert.assertEquals(false, alreadyLogged)
//
//        alreadyLogged = appAliveState(
//            HealthConnectQuery(
//                Constants.APP_ALIVE_QUERY_ID,
//                now.plusDays(1).toInstant().toEpochMilli()
//            )
//        )
//        println(alreadyLogged)
//        Assert.assertEquals(false, alreadyLogged)
//    }

    //    @Test
//    fun test_isoToZdt() {
//        val time = SahhaTimeManager()
//        val zdt = ZonedDateTime.now()
//        val iso = time.instantToIsoTime(zdt.toInstant())
//        println(iso)
//        val zdtFromIso = time.ISOToZonedDateTime(iso)
//
//        Assert.assertEquals(zdt.withZoneSameInstant(zdt.offset), zdtFromIso)
//    }

    fun saveBatchedLogs(amount: Int) = runTest {
        for (i in 0..amount) {
            AppModule.mockBatchedDataRepo.saveBatchedData(
                listOf(
                    SahhaDataLog(
                        id = UUID.randomUUID().toString(),
                        logType = "test_type",
                        dataType = SahhaSensor.values()[Random.nextInt(
                            SahhaSensor.values().count()
                        )].name,
                        value = Random.nextDouble(1000.0),
                        source = "unit.test",
                        startDateTime = ZonedDateTime.now()
                            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        endDateTime = ZonedDateTime.now()
                            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        unit = "test_unit",
                        recordingMethod = "test",
                        deviceId = UUID.randomUUID().toString(),
                        deviceType = "test_type",
                        additionalProperties = null,
                        parentId = UUID.randomUUID().toString(),
                        postDateTimes = arrayListOf(ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)),
                        modifiedDateTime = ZonedDateTime.now()
                            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                    )
                )
            )
        }
    }

    @Test
    fun emptyChunk() = runTest {
        val manager = AppModule.postChunkManager
        AppModule.mockBatchedDataRepo.deleteAllBatchedData()
        saveBatchedLogs(1000)
        val limit = AppModule.calculateBatchLimit()
        println(limit)
        manager.postAllChunks(
//            allData = emptyList<SahhaDataLog>(),
            allData = AppModule.mockBatchedDataRepo.getBatchedData(),
            limit = limit,
//            limit = 0,
            postData = { data ->
                println(data.count())
                true
            },
            callback = { err, success ->
                println(err)
                println(success)
            }
        )
    }


    @Test
    fun epochMillisToIsoCheck() {
        val epochMillis = Instant.now().toEpochMilli()
        val tm = SahhaTimeManager()
        val iso = tm.epochMillisToISO(epochMillis)
        println(iso)

        val offset = ZonedDateTime.now().offset
        val instant = Instant.now()
        val iso2 = tm.instantToIsoTime(instant, offset)
        println(iso2)
    }

    @Test
    fun epochMillisToZdt() {
        val epochMillis = Instant.now().toEpochMilli()
        val tm = SahhaTimeManager()
        val zdt = tm.epochMillisToZdt(epochMillis)
        println(zdt)
    }

    @Test
    fun testNoonConversions() {
        val zoneId = ZoneId.systemDefault()
        val start = ZonedDateTime.of(
            LocalDateTime.of(
                LocalDate.now(),
                LocalTime.MIDNIGHT
            ),
            zoneId
        )
        val end = ZonedDateTime.of(
            LocalDateTime.of(
                LocalDate.now().plusDays(1),
                LocalTime.MIDNIGHT
            ),
            zoneId
        )
        Assert.assertEquals(
            ZonedDateTime.of(
                LocalDateTime.of(
                    LocalDate.now().minusDays(1),
                    LocalTime.NOON
                ),
                zoneId
            ),
            start.toNoon(-1)
        )

        Assert.assertEquals(
            ZonedDateTime.of(
                LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.NOON
                ),
                zoneId
            ),
            end.toNoon(-1)
        )
    }

    @Test
    fun testMidnightConversions() {
        val zoneId = ZoneId.systemDefault()
        var start = ZonedDateTime.of(
            LocalDateTime.of(
                LocalDate.now(),
                LocalTime.NOON
            ),
            zoneId
        )
        var end = ZonedDateTime.of(
            LocalDateTime.of(
                LocalDate.now().plusDays(1),
                LocalTime.NOON
            ),
            zoneId
        )

        var startConverted = start.toMidnight()
        var endConverted = end.toMidnight(1)

        Assert.assertEquals(
            ZonedDateTime.of(
                LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.MIDNIGHT
                ),
                zoneId
            ),
            startConverted
        )

        Assert.assertEquals(
            ZonedDateTime.of(
                LocalDateTime.of(
                    LocalDate.now().plusDays(2),
                    LocalTime.MIDNIGHT
                ),
                zoneId
            ),
            endConverted
        )
    }

    @Test
    fun minusNano() {
        val time = LocalTime.MIDNIGHT.minusNanos(1)
        println(time)
    }

    @Test
    fun test_batchLimit() {
        val calculateBatchLimit = CalculateBatchLimit(mockk())
        val limit = calculateBatchLimit(emptyList())
        println(limit)
    }

    @Test
    fun test() {
        val time = LocalDateTime.of(
            LocalDate.now(),
            LocalTime.of(23, 59, 59, 999999999)
        )
        println(SahhaTimeManager().localDateTimeToISO(time))
    }

    private fun logic(list: List<Boolean>): Boolean {
        list.forEach { isTrue ->
            if (!isTrue) return false
        }
        return true
    }

    @Test
    fun test_timestamps() = runTest {
        val endOfDay = LocalTime.MIDNIGHT.minus(10, ChronoUnit.MILLIS)

        println("************************")
        println(endOfDay)
        println("************************")
    }

    @Test
    fun dateTime() = runTest {
        val tm = SahhaTimeManager()
        println(tm.nowInISO())
    }

    private val list = mutableMapOf<String, String>()
    private val tm = SahhaTimeManager()
    private var calendar: Calendar? = null
    private val instant: Instant get() = calendar?.time?.toInstant() ?: Instant.now()
    private val time: String get() = tm.instantToIsoTime(instant)

    @Test
    fun cultureConversion() = runTest {
        println("*********************************")

        calendar = Calendar.getInstance()
//            println("type: $type")
        println("calendar time: ${calendar?.time}")
        println("instant: $instant")
        println("iso time: $time")
        println("*********************************")

        ZoneOffset.getAvailableZoneIds().forEach { id ->
            val zdt = tm.ISOToZonedDateTime(time)
            val time = tm.localDateTimeToISO(zdt.toLocalDateTime(), ZoneId.of(id))
            list[id] = (time)
//            println(SahhaConverterUtility.convertToJsonString(cal))
        }


        val sorted = list.toList().sortedBy { (_, value) -> value }.toMap()
        sorted.forEach {
            println("${it.value}\t${it.key}")
        }
        println("*********************************")

        val list2 = mutableListOf<String>()
        list.forEach {
            val tm = SahhaTimeManager()
            val time = tm.ISOToZonedDateTime(it.value)
            list2.add(time.toString())
            list2.sortBy { it }
        }
        list2.forEach { println(it) }
    }
}