package sdk.sahha.android.sandbox

import android.icu.text.DateFormat
import androidx.activity.ComponentActivity
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.test.core.app.ApplicationProvider
import com.google.gson.GsonBuilder
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import kotlinx.coroutines.test.runTest
import org.junit.BeforeClass
import org.junit.Test
import sdk.sahha.android.common.SahhaSetupUtil
import sdk.sahha.android.data.mapper.toSahhaDataLogAsParentLog
import sdk.sahha.android.data.mapper.toSahhaDataLogDto
import sdk.sahha.android.data.mapper.toStepsHealthConnect
import sdk.sahha.android.domain.repository.HealthConnectRepo
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaEnvironment
import sdk.sahha.android.source.SahhaSettings
import java.time.Instant
import java.time.ZoneOffset

internal class RecordMappingTest {
    companion object {
        private lateinit var activity: ComponentActivity
        private lateinit var repo: HealthConnectRepo
        private val spacer = "\n--------------------------------\n"

        @JvmStatic
        @BeforeClass
        fun before() = runTest {
            activity = ApplicationProvider.getApplicationContext()
            val settings = SahhaSettings(environment = SahhaEnvironment.sandbox)
            SahhaSetupUtil.configureSahha(activity, settings)
            repo = Sahha.di.healthConnectRepo
            Sahha.di.batchDataLogs()
        }
    }

    private fun <T> convertToJsonString(anyObject: T): String {
        return GsonBuilder()
//            .setPrettyPrinting()
            .registerTypeAdapter(
                Instant::class.java,
                JsonSerializer<Instant> { src, _, _ ->
                    JsonPrimitive(src.toString())
                }
            )
            .registerTypeAdapter(
                ZoneOffset::class.java,
                JsonSerializer<ZoneOffset> { src, _, _ ->
                    JsonPrimitive(src.toString())
                }
            )
            .setDateFormat(DateFormat.TIMEZONE_ISO_FIELD)
            .create()
            .toJson(anyObject)
    }

    @Test
    fun directMapSteps() = runTest {
        val firstSample = repo.getRecords(
            StepsRecord::class,
            TimeRangeFilter.Companion.before(Instant.now())
        )?.first()
        firstSample?.also {
            val firstString = convertToJsonString(it)
            println(firstString)
            println(spacer)
        }

        val secondSample = repo.getRecords(
            TotalCaloriesBurnedRecord::class,
            TimeRangeFilter.Companion.before(Instant.now())
        )?.first()
        secondSample?.also {
            val secondString = convertToJsonString(it)
            println(secondString)
            println(spacer)
        }

        val thirdSample = repo.getRecords(
            OxygenSaturationRecord::class,
            TimeRangeFilter.Companion.before(Instant.now())
        )?.first()
        thirdSample?.also {
            val thirdString = convertToJsonString(it)
            println(thirdString)
            println(spacer)
        }
    }

    @Test
    fun sahhaMap() = runTest {
        val firstSample =  repo.getRecords(
            StepsRecord::class,
            TimeRangeFilter.Companion.before(Instant.now())
        )?.first()
        firstSample?.also {
            val sahhaLog = it.toStepsHealthConnect().toSahhaDataLogAsParentLog()
            val firstString = convertToJsonString(sahhaLog)
            println(firstString)
            println(spacer)
        }

        val secondSample =  repo.getRecords(
            TotalCaloriesBurnedRecord::class,
            TimeRangeFilter.Companion.before(Instant.now())
        )?.first()
        secondSample?.also {
            val sahhaLog = it.toSahhaDataLogDto()
            val firstString = convertToJsonString(sahhaLog)
            println(firstString)
            println(spacer)
        }

        val thirdSample = repo.getRecords(
            OxygenSaturationRecord::class,
            TimeRangeFilter.Companion.before(Instant.now())
        )?.first()
        thirdSample?.also {
            val sahhaLog = it.toSahhaDataLogDto()
            val thirdString = convertToJsonString(sahhaLog)
            println(thirdString)
            println(spacer)
        }
    }
}