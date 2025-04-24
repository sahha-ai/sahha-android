package sdk.sahha.android.suite.batched

import androidx.activity.ComponentActivity
import androidx.test.core.app.ActivityScenario
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.BeforeClass
import org.junit.Test
import sdk.sahha.android.common.Constants
import sdk.sahha.android.common.SahhaSetupUtil
import sdk.sahha.android.domain.internal_enum.RecordingMethods
import sdk.sahha.android.domain.model.data_log.SahhaDataLog
import sdk.sahha.android.domain.model.dto.QueryTime
import sdk.sahha.android.domain.model.dto.send.toSahhaDataLogDto
import sdk.sahha.android.domain.repository.HealthConnectRepo
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaBiomarkerCategory
import sdk.sahha.android.source.SahhaConverterUtility
import sdk.sahha.android.source.SahhaEnvironment
import sdk.sahha.android.source.SahhaSensor
import sdk.sahha.android.source.SahhaSettings
import sdk.sahha.android.source.SahhaStatInterval
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.UUID
import kotlin.random.Random

class BatchAggregateLogsTest {
    companion object {
        private lateinit var repository: HealthConnectRepo

        @BeforeClass
        @JvmStatic
        fun beforeClass() = runTest {
            ActivityScenario.launch(ComponentActivity::class.java).onActivity { activity ->
                launch {
                    SahhaSetupUtil.configureSahha(activity, SahhaSettings(SahhaEnvironment.sandbox))
                    repository = Sahha.di.healthConnectRepo

                    Sahha.enableSensors(activity, SahhaSensor.values().toSet()) { error, status ->
                        println(status.name)
                    }
                }
            }
        }
    }

    private fun generateLogs(amount: Int): List<SahhaDataLog> {
        val logs = mutableListOf<SahhaDataLog>()
        for (i in 0 until amount) {
            val randomTime = LocalDateTime.now().minusHours(Random.nextInt(720).toLong())
            logs += SahhaDataLog(
                id = UUID.randomUUID().toString(),
                logType = SahhaBiomarkerCategory.values()[Random.nextInt(
                    SahhaBiomarkerCategory.values().count()
                )].name,
                dataType = SahhaSensor.values()[Random.nextInt(SahhaSensor.values().count())].name,
                value = 123.4,
                source = "com.test",
                startDateTime = Sahha.di.timeManager.localDateTimeToISO(randomTime),
                endDateTime = Sahha.di.timeManager.localDateTimeToISO(
                    randomTime.plusMinutes(
                        Random.nextInt(
                            30
                        ).toLong()
                    )
                ),
                unit = Constants.DataUnits.COUNT,
                recordingMethod = RecordingMethods.manual_entry.name,
                deviceId = null,
                deviceType = "PHONE",
                additionalProperties = hashMapOf(),
                parentId = null,
                postDateTimes = arrayListOf(Sahha.di.timeManager.nowInISO()),
                modifiedDateTime = Sahha.di.timeManager.nowInISO(),
            )
        }
        return logs
    }

    @Test
    fun observe_day() = runTest {
        SahhaSensor.values().forEach {
            val result = Sahha.di.batchAggregateLogs(
                it,
                SahhaStatInterval.day,
                QueryTime(
                    Constants.AGGREGATE_QUERY_ID_DAY,
                    ZonedDateTime.now().minusDays(7).toInstant()
                        .toEpochMilli()
                )
            )

            val json = result.second?.let { SahhaConverterUtility.convertToJsonString(it.map { it.toSahhaDataLogDto() }) }

            if (result.first != null) println(result.first)
            else println(json)
        }

    }

    @Test
    fun observe_hour() = runTest {
        SahhaSensor.values().forEach {
            val result = Sahha.di.batchAggregateLogs(
                it,
                SahhaStatInterval.hour,
                QueryTime(
                    Constants.AGGREGATE_QUERY_ID_HOUR,
                    ZonedDateTime.now().minusDays(1).toInstant()
                        .toEpochMilli()
                )
            )

            val json = result.second?.let { SahhaConverterUtility.convertToJsonString(it) }
            println(json)
        }
    }
}