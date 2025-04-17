package sdk.sahha.android.suite

import androidx.health.connect.client.records.metadata.DeviceTypes
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import sdk.sahha.android.common.Constants
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.domain.internal_enum.RecordingMethods
import sdk.sahha.android.domain.model.data_log.SahhaDataLog
import sdk.sahha.android.domain.use_case.background.FilterActivityOverlaps
import sdk.sahha.android.source.SahhaSensor
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime

// Also tests other data types being ignored
class ActivityOverlaps {
    companion object {
        private val filterOverlaps = FilterActivityOverlaps(mockk(), SahhaTimeManager())

        private val time = SahhaTimeManager()
        private val today = ZonedDateTime.now()
        private var id = 0
    }

    private fun mockNormalData(): List<SahhaDataLog> {
        val data = listOf(
            SahhaDataLog(
                id = id++.toString(),
                logType = Constants.DataLogs.ACTIVITY,
                dataType = SahhaSensor.steps.name,
                value = 100.0,
                source = "mock.steps.regular",
                startDateTime = time.localDateTimeToISO(
                    LocalDateTime.of(
                        today.toLocalDate(),
                        LocalTime.of(0, 0)
                    )
                ),
                endDateTime = time.localDateTimeToISO(
                    LocalDateTime.of(
                        today.toLocalDate(),
                        LocalTime.of(9, 30)
                    )
                ),
                unit = Constants.DataUnits.COUNT,
                recordingMethod = RecordingMethods.manual_entry.name,
                deviceType = DeviceTypes.WATCH,
                deviceId = null
            ),
            SahhaDataLog(
                id = id++.toString(),
                logType = Constants.DataLogs.ACTIVITY,
                dataType = SahhaSensor.steps.name,
                value = 50.0,
                source = "mock.steps.regular",
                startDateTime = time.localDateTimeToISO(
                    LocalDateTime.of(
                        today.toLocalDate(),
                        LocalTime.of(9, 30)
                    )
                ),
                endDateTime = time.localDateTimeToISO(
                    LocalDateTime.of(
                        today.toLocalDate(),
                        LocalTime.of(9, 45)
                    )
                ),
                unit = Constants.DataUnits.COUNT,
                recordingMethod = RecordingMethods.manual_entry.name,
                deviceType = DeviceTypes.WATCH,
                deviceId = null
            ),
            SahhaDataLog(
                id = id++.toString(),
                logType = Constants.DataLogs.ACTIVITY,
                dataType = SahhaSensor.steps.name,
                value = 100.0,
                source = "mock.steps.regular",
                startDateTime = time.localDateTimeToISO(
                    LocalDateTime.of(
                        today.toLocalDate(),
                        LocalTime.of(13, 0)
                    )
                ),
                endDateTime = time.localDateTimeToISO(
                    LocalDateTime.of(
                        today.toLocalDate(),
                        LocalTime.of(14, 0)
                    )
                ),
                unit = Constants.DataUnits.COUNT,
                recordingMethod = RecordingMethods.manual_entry.name,
                deviceType = DeviceTypes.WATCH,
                deviceId = null
            ),
            // Add other data types
            SahhaDataLog(
                id = id++.toString(),
                logType = Constants.DataLogs.SLEEP,
                dataType = SahhaSensor.sleep.name,
                value = 100.0,
                source = "mock.steps.regular",
                startDateTime = time.localDateTimeToISO(
                    LocalDateTime.of(
                        today.toLocalDate(),
                        LocalTime.of(0, 0)
                    )
                ),
                endDateTime = time.localDateTimeToISO(
                    LocalDateTime.of(
                        today.toLocalDate(),
                        LocalTime.of(9, 30)
                    )
                ),
                unit = Constants.DataUnits.COUNT,
                recordingMethod = RecordingMethods.manual_entry.name,
                deviceType = DeviceTypes.WATCH,
                deviceId = null
            ),
            SahhaDataLog(
                id = id++.toString(),
                logType = Constants.DataLogs.SLEEP,
                dataType = SahhaSensor.sleep.name,
                value = 50.0,
                source = "mock.steps.regular",
                startDateTime = time.localDateTimeToISO(
                    LocalDateTime.of(
                        today.toLocalDate(),
                        LocalTime.of(9, 30)
                    )
                ),
                endDateTime = time.localDateTimeToISO(
                    LocalDateTime.of(
                        today.toLocalDate(),
                        LocalTime.of(9, 45)
                    )
                ),
                unit = Constants.DataUnits.COUNT,
                recordingMethod = RecordingMethods.manual_entry.name,
                deviceType = DeviceTypes.WATCH,
                deviceId = null
            ),
            SahhaDataLog(
                id = id++.toString(),
                logType = Constants.DataLogs.SLEEP,
                dataType = SahhaSensor.sleep.name,
                value = 100.0,
                source = "mock.steps.regular",
                startDateTime = time.localDateTimeToISO(
                    LocalDateTime.of(
                        today.toLocalDate(),
                        LocalTime.of(13, 0)
                    )
                ),
                endDateTime = time.localDateTimeToISO(
                    LocalDateTime.of(
                        today.toLocalDate(),
                        LocalTime.of(14, 0)
                    )
                ),
                unit = Constants.DataUnits.COUNT,
                recordingMethod = RecordingMethods.manual_entry.name,
                deviceType = DeviceTypes.WATCH,
                deviceId = null
            ),
        )

        return data
    }

    private fun mockOverlappingData(): List<SahhaDataLog> {
        val data = listOf(
            SahhaDataLog(
                id = id++.toString(),
                logType = Constants.DataLogs.ACTIVITY,
                dataType = SahhaSensor.steps.name,
                value = 78.0,
                source = "mock.steps",
                startDateTime = "2024-07-08T14:00:58.00+12:00",
                endDateTime = "2024-07-08T14:02:56.00+12:00",
                unit = Constants.DataUnits.COUNT,
                recordingMethod = RecordingMethods.manual_entry.name,
                deviceType = DeviceTypes.WATCH,
                deviceId = null
            ),
            SahhaDataLog(
                id = id++.toString(),
                logType = Constants.DataLogs.ACTIVITY,
                dataType = SahhaSensor.steps.name,
                value = 2.0,
                source = "mock.steps",
                startDateTime = "2024-07-08T14:01:03.00+12:00",
                endDateTime = "2024-07-08T14:01:07.00+12:00",
                unit = Constants.DataUnits.COUNT,
                recordingMethod = RecordingMethods.manual_entry.name,
                deviceType = DeviceTypes.WATCH,
                deviceId = null
            ),
            SahhaDataLog(
                id = id++.toString(),
                logType = Constants.DataLogs.ACTIVITY,
                dataType = SahhaSensor.steps.name,
                value = 17.0,
                source = "mock.steps",
                startDateTime = "2024-07-08T14:01:07.00+12:00",
                endDateTime = "2024-07-08T14:02:07.00+12:00",
                unit = Constants.DataUnits.COUNT,
                recordingMethod = RecordingMethods.manual_entry.name,
                deviceType = DeviceTypes.WATCH,
                deviceId = null
            ),
            SahhaDataLog(
                id = id++.toString(),
                logType = Constants.DataLogs.ACTIVITY,
                dataType = SahhaSensor.steps.name,
                value = 11.0,
                source = "mock.steps",
                startDateTime = "2024-07-08T14:02:07.00+12:00",
                endDateTime = "2024-07-08T14:02:10.00+12:00",
                unit = Constants.DataUnits.COUNT,
                recordingMethod = RecordingMethods.manual_entry.name,
                deviceType = DeviceTypes.WATCH,
                deviceId = null
            ),
            SahhaDataLog(
                id = id++.toString(),
                logType = Constants.DataLogs.ACTIVITY,
                dataType = SahhaSensor.steps.name,
                value = 1.0,
                source = "mock.steps",
                startDateTime = "2024-07-08T14:02:10.00+12:00",
                endDateTime = "2024-07-08T14:02:12.00+12:00",
                unit = Constants.DataUnits.COUNT,
                recordingMethod = RecordingMethods.manual_entry.name,
                deviceType = DeviceTypes.WATCH,
                deviceId = null
            ),
            // Add other data types
            SahhaDataLog(
                id = id++.toString(),
                logType = Constants.DataLogs.SLEEP,
                dataType = SahhaSensor.sleep.name,
                value = 78.0,
                source = "mock.steps",
                startDateTime = "2024-07-08T14:00:58.00+12:00",
                endDateTime = "2024-07-08T14:02:56.00+12:00",
                unit = Constants.DataUnits.COUNT,
                recordingMethod = RecordingMethods.manual_entry.name,
                deviceType = DeviceTypes.WATCH,
                deviceId = null
            ),
            SahhaDataLog(
                id = id++.toString(),
                logType = Constants.DataLogs.SLEEP,
                dataType = SahhaSensor.sleep.name,
                value = 2.0,
                source = "mock.steps",
                startDateTime = "2024-07-08T14:01:03.00+12:00",
                endDateTime = "2024-07-08T14:01:07.00+12:00",
                unit = Constants.DataUnits.COUNT,
                recordingMethod = RecordingMethods.manual_entry.name,
                deviceType = DeviceTypes.WATCH,
                deviceId = null
            ),
            SahhaDataLog(
                id = id++.toString(),
                logType = Constants.DataLogs.SLEEP,
                dataType = SahhaSensor.sleep.name,
                value = 17.0,
                source = "mock.steps",
                startDateTime = "2024-07-08T14:01:07.00+12:00",
                endDateTime = "2024-07-08T14:02:07.00+12:00",
                unit = Constants.DataUnits.COUNT,
                recordingMethod = RecordingMethods.manual_entry.name,
                deviceType = DeviceTypes.WATCH,
                deviceId = null
            ),
            SahhaDataLog(
                id = id++.toString(),
                logType = Constants.DataLogs.SLEEP,
                dataType = SahhaSensor.sleep.name,
                value = 11.0,
                source = "mock.steps",
                startDateTime = "2024-07-08T14:02:07.00+12:00",
                endDateTime = "2024-07-08T14:02:10.00+12:00",
                unit = Constants.DataUnits.COUNT,
                recordingMethod = RecordingMethods.manual_entry.name,
                deviceType = DeviceTypes.WATCH,
                deviceId = null
            ),
            SahhaDataLog(
                id = id++.toString(),
                logType = Constants.DataLogs.SLEEP,
                dataType = SahhaSensor.sleep.name,
                value = 1.0,
                source = "mock.steps",
                startDateTime = "2024-07-08T14:02:10.00+12:00",
                endDateTime = "2024-07-08T14:02:12.00+12:00",
                unit = Constants.DataUnits.COUNT,
                recordingMethod = RecordingMethods.manual_entry.name,
                deviceType = DeviceTypes.WATCH,
                deviceId = null
            ),
        )

        return data
    }

    private fun mockMatchingStartAndEnd(): List<SahhaDataLog> {
        val data = listOf(
            SahhaDataLog(
                id = id++.toString(),
                logType = Constants.DataLogs.ACTIVITY,
                dataType = SahhaSensor.steps.name,
                value = 30.0,
                source = "mock.steps",
                startDateTime = time.localDateTimeToISO(
                    LocalDateTime.of(
                        today.toLocalDate(),
                        LocalTime.of(13, 0)
                    )
                ),
                endDateTime = time.localDateTimeToISO(
                    LocalDateTime.of(
                        today.toLocalDate(),
                        LocalTime.of(13, 1)
                    )
                ),
                unit = Constants.DataUnits.COUNT,
                recordingMethod = RecordingMethods.manual_entry.name,
                deviceType = DeviceTypes.WATCH,
                deviceId = null
            ),
            SahhaDataLog(
                id = id++.toString(),
                logType = Constants.DataLogs.ACTIVITY,
                dataType = SahhaSensor.steps.name,
                value = 5.0,
                source = "mock.steps",
                startDateTime = time.localDateTimeToISO(
                    LocalDateTime.of(
                        today.toLocalDate(),
                        LocalTime.of(13, 1)
                    )
                ),
                endDateTime = time.localDateTimeToISO(
                    LocalDateTime.of(
                        today.toLocalDate(),
                        LocalTime.of(13, 2)
                    )
                ),
                unit = Constants.DataUnits.COUNT,
                recordingMethod = RecordingMethods.manual_entry.name,
                deviceType = DeviceTypes.WATCH,
                deviceId = null
            ),
            // Add other data types
            SahhaDataLog(
                id = id++.toString(),
                logType = Constants.DataLogs.SLEEP,
                dataType = SahhaSensor.sleep.name,
                value = 30.0,
                source = "mock.steps",
                startDateTime = time.localDateTimeToISO(
                    LocalDateTime.of(
                        today.toLocalDate(),
                        LocalTime.of(13, 0)
                    )
                ),
                endDateTime = time.localDateTimeToISO(
                    LocalDateTime.of(
                        today.toLocalDate(),
                        LocalTime.of(13, 1)
                    )
                ),
                unit = Constants.DataUnits.COUNT,
                recordingMethod = RecordingMethods.manual_entry.name,
                deviceType = DeviceTypes.WATCH,
                deviceId = null
            ),
            SahhaDataLog(
                id = id++.toString(),
                logType = Constants.DataLogs.SLEEP,
                dataType = SahhaSensor.sleep.name,
                value = 5.0,
                source = "mock.steps",
                startDateTime = time.localDateTimeToISO(
                    LocalDateTime.of(
                        today.toLocalDate(),
                        LocalTime.of(13, 1)
                    )
                ),
                endDateTime = time.localDateTimeToISO(
                    LocalDateTime.of(
                        today.toLocalDate(),
                        LocalTime.of(13, 2)
                    )
                ),
                unit = Constants.DataUnits.COUNT,
                recordingMethod = RecordingMethods.manual_entry.name,
                deviceType = DeviceTypes.WATCH,
                deviceId = null
            ),
        )

        return data
    }

    private fun mockSlightOverlap(): List<SahhaDataLog> {
        val data = listOf(
            SahhaDataLog(
                id = id++.toString(),
                logType = Constants.DataLogs.ACTIVITY,
                dataType = SahhaSensor.steps.name,
                value = 25.0,
                source = "mock.steps",
                startDateTime = "2024-07-08T12:25:25.00+12:00",
                endDateTime = "2024-07-08T12:26:25.01+12:00",
                unit = Constants.DataUnits.COUNT,
                recordingMethod = RecordingMethods.manual_entry.name,
                deviceType = DeviceTypes.WATCH,
                deviceId = null
            ),
            SahhaDataLog(
                id = id++.toString(),
                logType = Constants.DataLogs.ACTIVITY,
                dataType = SahhaSensor.steps.name,
                value = 4.0,
                source = "mock.steps",
                startDateTime = "2024-07-08T12:26:25.00+12:00",
                endDateTime = "2024-07-08T12:26:28.09+12:00",
                unit = Constants.DataUnits.COUNT,
                recordingMethod = RecordingMethods.manual_entry.name,
                deviceType = DeviceTypes.WATCH,
                deviceId = null
            ),
            SahhaDataLog(
                id = id++.toString(),
                logType = Constants.DataLogs.SLEEP,
                dataType = SahhaSensor.sleep.name,
                value = 25.0,
                source = "mock.steps",
                startDateTime = "2024-07-08T12:25:25.00+12:00",
                endDateTime = "2024-07-08T12:26:25.01+12:00",
                unit = Constants.DataUnits.COUNT,
                recordingMethod = RecordingMethods.manual_entry.name,
                deviceType = DeviceTypes.WATCH,
                deviceId = null
            ),
            SahhaDataLog(
                id = id++.toString(),
                logType = Constants.DataLogs.SLEEP,
                dataType = SahhaSensor.sleep.name,
                value = 4.0,
                source = "mock.steps",
                startDateTime = "2024-07-08T12:26:25.00+12:00",
                endDateTime = "2024-07-08T12:26:28.09+12:00",
                unit = Constants.DataUnits.COUNT,
                recordingMethod = RecordingMethods.manual_entry.name,
                deviceType = DeviceTypes.WATCH,
                deviceId = null
            ),
        )

        return data
    }

    @Test
    fun overlapped_isolated_areFiltered() = runTest {
        val overlapped = mockOverlappingData()
        println(overlapped.count().toString())

        val filtered = filterOverlaps(overlapped)
        filtered.forEach { println(it.toString()) }
        Assert.assertEquals(6, filtered.count())
    }

    @Test
    fun regular_isolated_doesNotFilter() = runTest {
        val regular = mockNormalData()
        println(regular.count().toString())

        val filtered = filterOverlaps(regular)
        filtered.forEach { println(it.toString()) }
        Assert.assertEquals(6, filtered.count())
    }

    @Test
    fun overlapped_withRegularData_onlyFiltersOverlapped() = runTest {
        val overlapped = mockOverlappingData()
        println(overlapped.count().toString())
        val regular = mockNormalData()
        println(regular.count().toString())

        val filtered = filterOverlaps(overlapped + regular)
        filtered.forEach { println(it.toString()) }
        Assert.assertEquals(12, filtered.count())
    }

    @Test
    fun matching_endAndNextStartTimes_areNotFiltered() = runTest {
        val matching = mockMatchingStartAndEnd()
        println(matching.count().toString())
        matching.forEach { println(it.toString()) }

        val filtered = filterOverlaps(matching)
        println(filtered.count())
        filtered.forEach { println(it.toString()) }
        Assert.assertEquals(4, filtered.count())
    }

    @Test
    fun slightOverlap_lastEndAndCurrentStart_isNotFiltered() = runTest {
        val slightOverlap = mockSlightOverlap()
        println(slightOverlap.count().toString())

        val filtered = filterOverlaps(slightOverlap)
        filtered.forEach { println(it.toString()) }
        Assert.assertEquals(4, filtered.count())
    }
}