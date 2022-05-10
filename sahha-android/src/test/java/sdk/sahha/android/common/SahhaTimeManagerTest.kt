package sdk.sahha.android.common

import junit.framework.TestCase
import org.junit.Test
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
        assertEquals("2022-05-03T00:00:00Z", value)
    }
}