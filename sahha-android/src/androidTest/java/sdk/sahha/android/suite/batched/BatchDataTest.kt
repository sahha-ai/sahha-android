package sdk.sahha.android.suite.batched

import androidx.activity.ComponentActivity
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import sdk.sahha.android.common.Constants
import sdk.sahha.android.common.SahhaSetupUtil
import sdk.sahha.android.common.appId
import sdk.sahha.android.common.appSecret
import sdk.sahha.android.common.externalId
import sdk.sahha.android.domain.model.data_log.SahhaDataLog
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaConverterUtility
import sdk.sahha.android.source.SahhaEnvironment
import sdk.sahha.android.source.SahhaSensor
import sdk.sahha.android.source.SahhaSettings
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.math.ceil

class BatchDataTest {
    companion object {
        private lateinit var activity: ComponentActivity

        @JvmStatic
        @BeforeClass
        fun beforeClass() = runTest {
            activity = ApplicationProvider.getApplicationContext()
            val settings = SahhaSettings(environment = SahhaEnvironment.sandbox)
            SahhaSetupUtil.configureSahha(activity, settings)
            SahhaSetupUtil.authenticateSahha(
                appId = appId,
                appSecret = appSecret,
                externalId = externalId
            )
        }
    }

    private fun getMockBatchData(amount: Int): List<SahhaDataLog> {
        var data = emptyList<SahhaDataLog>()
        for (i in 0 until amount) {
            data += listOf(
                SahhaDataLog(
                    id = UUID.randomUUID().toString(),
                    logType = Constants.DataLogs.ACTIVITY,
                    dataType = SahhaSensor.steps.name,
                    value = i.toDouble(),
                    source = "sahha.beast.wars",
                    startDateTime = Sahha.di.timeManager.nowInISO(),
                    endDateTime = Sahha.di.timeManager.nowInISO(),
                    unit = Constants.DataUnits.COUNT,
                    deviceId = null
                )
            )
        }
        return data
    }

//    @Ignore("Failing, need to revisit this test")
    @Test
    fun posting150Logs_equatesTo4Chunks() = runTest {
        val scope = CoroutineScope(Dispatchers.Default + Job())

        val mockDataAmount = 150
        val data = getMockBatchData(mockDataAmount)
        val sample = data.random()
        val approximateSize = SahhaConverterUtility.convertToJsonString(sample).toByteArray().size
        val expectedLimit = Constants.DATA_LOG_LIMIT_BYTES / approximateSize

        suspendCancellableCoroutine { cont ->
            scope.launch {
                Sahha.sim.sensor.postBatchData(
                    data
                ) { error, successful ->
                    println(error)
                    println(successful)
                    if (cont.isActive) cont.resume(Unit)
                }
            }
        }


        val chunksAmount = mockDataAmount.toDouble() / expectedLimit.toDouble()
        Assert.assertEquals(
            ceil(chunksAmount).toInt(),
            Sahha.di.postChunkManager.postedChunkCount
        )
    }
}