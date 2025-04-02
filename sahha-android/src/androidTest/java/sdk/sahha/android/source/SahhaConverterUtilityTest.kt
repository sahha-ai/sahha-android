package sdk.sahha.android.source

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class SahhaConverterUtilityTest {
    @Test
    fun test_timeFormat() = runTest {
        val zoneOffset = ZonedDateTime.now().offset
        val instant = Instant.ofEpochMilli(0).atOffset(zoneOffset)
        val iso = instant.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        assertEquals("", iso)
    }
}