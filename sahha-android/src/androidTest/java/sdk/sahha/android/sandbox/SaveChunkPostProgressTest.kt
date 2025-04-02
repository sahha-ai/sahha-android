//package sdk.sahha.android.sandbox
//
//import androidx.activity.ComponentActivity
//import androidx.health.connect.client.records.HeartRateRecord
//import androidx.test.core.app.ApplicationProvider
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.cancel
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.test.runTest
//import org.junit.Assert
//import org.junit.BeforeClass
//import org.junit.Test
//import sdk.sahha.android.common.SahhaSetupUtil
//import sdk.sahha.android.common.TokenBearer
//import sdk.sahha.android.common.appId
//import sdk.sahha.android.common.appSecret
//import sdk.sahha.android.common.externalId
//import sdk.sahha.android.common.profileToken
//import sdk.sahha.android.domain.model.data_log.SahhaDataLogDto
//import sdk.sahha.android.source.Sahha
//import sdk.sahha.android.source.SahhaEnvironment
//import sdk.sahha.android.source.SahhaSettings
//import java.time.LocalDate
//import java.time.LocalDateTime
//import java.time.LocalTime
//import java.time.ZonedDateTime
//import kotlin.coroutines.resume
//import kotlin.coroutines.suspendCoroutine
//
//class SaveChunkPostProgressTest {
//    companion object {
//        lateinit var application: Application
//
//        @BeforeClass
//        @JvmStatic
//        fun beforeClass() = runTest {
//            activity = ApplicationProvider.getApplicationContext()
//            val settings = SahhaSettings(environment = SahhaEnvironment.sandbox)
//            SahhaSetupUtil.configureSahha(activity, settings)
//            SahhaSetupUtil.authenticateSahha(appId, appSecret, externalId)
//        }
//
//    }
//
//    @Test
//    fun postProgress_resumesAfterFailure() = runTest {
//        val repo = Sahha.di.healthConnectRepo
//        val api = Sahha.di.api
//        val timeMgr = Sahha.di.timeManager
//        val chunkMgr = Sahha.di.postChunkManager
//        val offset = ZonedDateTime.now().offset
//        val scope = CoroutineScope(Dispatchers.IO)
//        val data = mutableListOf(
//            SahhaDataLogDto(
//                id = "test",
//                logType = "test",
//                dataType = "test",
//                value = 1.0,
//                source = "test",
//                startDateTime = timeMgr.localDateTimeToISO(
//                    LocalDateTime.of(
//                        LocalDate.now(),
//                        LocalTime.of(0, 0)
//                    )
//                ),
//                endDateTime = timeMgr.localDateTimeToISO(
//                    LocalDateTime.of(
//                        LocalDate.now(),
//                        LocalTime.of(0, 0)
//                    )
//                ),
//                unit = "test"
//            ),
//            SahhaDataLogDto(
//                id = "test",
//                logType = "test",
//                dataType = "test",
//                value = 2.0,
//                source = "test",
//                startDateTime = timeMgr.localDateTimeToISO(
//                    LocalDateTime.of(
//                        LocalDate.now(),
//                        LocalTime.of(1, 0)
//                    )
//                ),
//                endDateTime = timeMgr.localDateTimeToISO(
//                    LocalDateTime.of(
//                        LocalDate.now(),
//                        LocalTime.of(1, 0)
//                    )
//                ),
//                unit = "test"
//            ),
//            SahhaDataLogDto(
//                id = "test",
//                logType = "test",
//                dataType = "test",
//                value = 3.0,
//                source = "test",
//                startDateTime = timeMgr.localDateTimeToISO(
//                    LocalDateTime.of(
//                        LocalDate.now(),
//                        LocalTime.of(2, 0)
//                    )
//                ),
//                endDateTime = timeMgr.localDateTimeToISO(
//                    LocalDateTime.of(
//                        LocalDate.now(),
//                        LocalTime.of(2, 0)
//                    )
//                ),
//                unit = "test"
//            ),
//            SahhaDataLogDto(
//                id = "test",
//                logType = "test",
//                dataType = "test",
//                value = 4.0,
//                source = "test",
//                startDateTime = timeMgr.localDateTimeToISO(
//                    LocalDateTime.of(
//                        LocalDate.now(),
//                        LocalTime.of(3, 0)
//                    )
//                ),
//                endDateTime = timeMgr.localDateTimeToISO(
//                    LocalDateTime.of(
//                        LocalDate.now(),
//                        LocalTime.of(3, 0)
//                    )
//                ),
//                unit = "test"
//            )
//        )
//
//        suspendCoroutine { cont ->
//            scope.launch {
//                repo.postData(
//                    data,
//                    1,
//                    { data -> api.postHeartRateData(TokenBearer(profileToken), data) },
//                    { chunk ->
//                        repo.saveLastSuccessfulQuery(
//                            HeartRateRecord::class,
//                            timeMgr.ISOToDate(chunk.last().endDateTime)
//                        )
//                        if (chunkMgr.postedChunkCount == 1) {
//                            cont.resume(Unit)
//                            return@postData
//                        }
//                    },
//                ) { error, successful ->
//                    if (successful)
//                        repo.saveLastSuccessfulQuery(HeartRateRecord::class, ZonedDateTime.now())
//                }
//            }
//        }
//
//        val lastQuery = repo.getLastSuccessfulQuery(HeartRateRecord::class)
//            ?: throw Exception("Last query was null")
//        data.removeIf { it.endDateTime <= timeMgr.instantToIsoTime(lastQuery.toInstant(), offset) }
//
//        Assert.assertEquals(3.0, data.first().value, 0.0)
//        Assert.assertEquals(4.0, data.last().value, 0.0)
//    }
//}