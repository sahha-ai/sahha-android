package sdk.sahha.android.common

import junit.framework.TestCase
import org.junit.Test
import java.sql.Date
import java.time.LocalDateTime

class SahhaTimeManagerTest : TestCase() {
    val sahhaTimeManager = SahhaTimeManager()

    @Test
    fun test_offsetDateTimeToISO() {
        val value = sahhaTimeManager.localDateTimeToISO(
            LocalDateTime.of(
                2022,
                5,
                3,
                0,
                0,
                0
            )
        )
        assertEquals("2022-05-03T00:00:00+12:00", value)
    }

    @Test
    fun test_epochToISO() {
        val value = sahhaTimeManager.epochMillisToISO(0)
        assertEquals("1970-01-01T12:00:00+12:00", value)
    }

    @Test
    fun test_dateToISO() {
        val date = Date(0)
        val formatted = sahhaTimeManager.dateToISO(date)
        assertEquals("1970-01-01T12:00:00.000+12:00", formatted)
    }
}