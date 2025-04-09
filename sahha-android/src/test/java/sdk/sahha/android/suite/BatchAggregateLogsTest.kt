package sdk.sahha.android.suite

import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import sdk.sahha.android.common.Constants
import sdk.sahha.android.di.AppModule
import sdk.sahha.android.di.MockAppComponent
import sdk.sahha.android.domain.model.data_log.SahhaDataLog
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensor
import sdk.sahha.android.source.SahhaStatInterval
import java.time.Duration
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

internal class BatchAggregateLogsTest {
    companion object {
        private val now = ZonedDateTime.now()
        private lateinit var testLogs: List<SahhaDataLog>

        @BeforeClass
        @JvmStatic
        fun beforeClass() = runTest {
            Sahha.di = MockAppComponent()
            testLogs =
                AppModule.mockPermissionActionProvider.permissionActionsLogs[SahhaSensor.heart_rate]?.invoke(
                    Duration.ofDays(1),
                    now.truncatedTo(ChronoUnit.DAYS),
                    now.plusDays(1).truncatedTo(ChronoUnit.DAYS),
                    AppModule.mockSahhaTimeManager.nowInISO(),
                    SahhaStatInterval.day.name
                )?.second ?: listOf()
        }
    }

    @Test
    fun logWithNoSources_isUnknown() = runTest {
        Assert.assertEquals(Constants.UNKNOWN, testLogs?.get(2)?.source)
    }

    @Test
    fun logWithOneSource_isSourceName() = runTest {
        Assert.assertEquals("TEST_PACKAGE_NAME_1", testLogs?.get(0)?.source)
    }

    @Test
    fun logWithMultipleSources_isMixed() = runTest {
        Assert.assertEquals("mixed", testLogs?.get(1)?.source)
    }
}