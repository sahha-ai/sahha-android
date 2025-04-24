package sdk.sahha.android.suite.batched

import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.metadata.Metadata
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import sdk.sahha.android.common.SahhaSetupUtil
import sdk.sahha.android.data.mapper.toSahhaDataLogDto
import sdk.sahha.android.source.SahhaEnvironment
import sdk.sahha.android.source.SahhaSettings
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class RecordingMethodTrimmed {
    companion object {
        @JvmStatic
        @BeforeClass
        fun before() = runTest {
            SahhaSetupUtil.configureSahha(
                ApplicationProvider.getApplicationContext(),
                SahhaSettings(SahhaEnvironment.sandbox)
            )
        }
    }

    private fun mockRecord(recordingMethod: Int): SleepSessionRecord {
        val now = ZonedDateTime.now()
        return SleepSessionRecord(
            startTime = now.toInstant().minus(8, ChronoUnit.HOURS),
            startZoneOffset = now.offset,
            endTime = now.toInstant(),
            endZoneOffset = now.offset,
            metadata = Metadata(
                recordingMethod = recordingMethod
            )
        )
    }

    @Test
    fun unknown_trimmed() {
        val record = mockRecord(Metadata.RECORDING_METHOD_UNKNOWN)
        val sahhaLog = record.toSahhaDataLogDto()

        Assert.assertEquals(
            "UNKNOWN",
            sahhaLog.recordingMethod
        )
    }

    @Test
    fun automaticallyRecorded_trimmed() {
        val record = mockRecord(Metadata.RECORDING_METHOD_AUTOMATICALLY_RECORDED)
        val sahhaLog = record.toSahhaDataLogDto()

        Assert.assertEquals(
            "AUTOMATICALLY_RECORDED",
            sahhaLog.recordingMethod
        )
    }

    @Test
    fun activelyRecord_trimmed() {
        val record = mockRecord(Metadata.RECORDING_METHOD_ACTIVELY_RECORDED)
        val sahhaLog = record.toSahhaDataLogDto()

        Assert.assertEquals(
            "ACTIVELY_RECORDED",
            sahhaLog.recordingMethod
        )
    }

    @Test
    fun manualEntry_trimmed() {
        val record = mockRecord(Metadata.RECORDING_METHOD_MANUAL_ENTRY)
        val sahhaLog = record.toSahhaDataLogDto()

        Assert.assertEquals(
            "MANUAL_ENTRY",
            sahhaLog.recordingMethod
        )
    }
}