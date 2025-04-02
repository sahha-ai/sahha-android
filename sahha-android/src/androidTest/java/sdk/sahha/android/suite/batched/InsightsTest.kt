//package sdk.sahha.android.suite.batched
//
//import androidx.activity.ComponentActivity
//import androidx.health.connect.client.records.SleepSessionRecord
//import androidx.health.connect.client.records.StepsRecord
//import androidx.test.core.app.ApplicationProvider
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.suspendCancellableCoroutine
//import kotlinx.coroutines.test.runTest
//import org.junit.Assert
//import org.junit.BeforeClass
//import org.junit.Test
//import sdk.sahha.android.common.SahhaSetupUtil
//import sdk.sahha.android.common.appId
//import sdk.sahha.android.common.appSecret
//import sdk.sahha.android.common.externalId
//import sdk.sahha.android.data.repository.InsightsRepoImpl
//import sdk.sahha.android.domain.model.insight.InsightData
//import sdk.sahha.android.source.Sahha
//import sdk.sahha.android.source.SahhaEnvironment
//import sdk.sahha.android.source.SahhaSensor
//import sdk.sahha.android.source.SahhaSettings
//import java.time.ZonedDateTime
//import kotlin.coroutines.resume
//
//class InsightsTest {
//    companion object {
//        private lateinit var application: Application
//        private val insightsRepo by lazy {
//            InsightsRepoImpl(
//                Sahha.di.timeManager,
//                Sahha.di.api,
//                Sahha.di.sahhaErrorLogger,
//                Sahha.di.healthConnectClient,
//                Sahha.di.ioScope
//            )
//        }
//        private val authRepo by lazy { Sahha.di.authRepo }
//        private val sleepRecords by lazy { getMockSleep() }
//        private val sleepRecordsMultiEntry by lazy { getMockSleepMultipleEntries() }
//        private val stepRecords by lazy { getMockSteps() }
//        private val summary by lazy { insightsRepo.getSleepStageSummary(sleepRecords) }
//        private val summaryMultiEntry by lazy {
//            insightsRepo.getSleepStageSummary(
//                sleepRecordsMultiEntry
//            )
//        }
//
////        @get:Rule
////        val serviceRule: ServiceTestRule = withTimeout(15, TimeUnit.SECONDS)
//
//        @BeforeClass
//        @JvmStatic
//        fun beforeClass() = runTest {
//            activity = ApplicationProvider.getApplicationContext()
//
//            SahhaSetupUtil.configureSahha(
//                application,
//                SahhaSettings(
//                    SahhaEnvironment.sandbox,
//                )
//            )
//            SahhaSetupUtil.authenticateSahha(appId, appSecret, externalId)
//            SahhaSetupUtil.enableSensors(application, SahhaSensor.values().toSet())
//        }
//
//        private fun getMockSleep(): List<SleepSessionRecord> {
//            val start = ZonedDateTime.now().minusHours(2)
//            val end = start.plusMinutes(160)
//            return listOf(
//                SleepSessionRecord(
//                    startTime = start.toInstant(),
//                    startZoneOffset = start.offset,
//                    endTime = end.toInstant(),
//                    endZoneOffset = end.offset,
//                    stages = listOf(
//                        SleepSessionRecord.Stage(
//                            startTime = start.toInstant(),
//                            endTime = start.plusMinutes(10).toInstant(),
//                            stage = SleepSessionRecord.STAGE_TYPE_UNKNOWN
//                        ),
//                        SleepSessionRecord.Stage(
//                            startTime = start.plusMinutes(10).toInstant(),
//                            endTime = start.plusMinutes(20).toInstant(),
//                            stage = SleepSessionRecord.STAGE_TYPE_SLEEPING
//                        ),
//                        SleepSessionRecord.Stage(
//                            startTime = start.plusMinutes(20).toInstant(),
//                            endTime = start.plusMinutes(30).toInstant(),
//                            stage = SleepSessionRecord.STAGE_TYPE_LIGHT
//                        ),
//                        SleepSessionRecord.Stage(
//                            startTime = start.plusMinutes(30).toInstant(),
//                            endTime = start.plusMinutes(40).toInstant(),
//                            stage = SleepSessionRecord.STAGE_TYPE_REM
//                        ),
//                        SleepSessionRecord.Stage(
//                            startTime = start.plusMinutes(40).toInstant(),
//                            endTime = start.plusMinutes(50).toInstant(),
//                            stage = SleepSessionRecord.STAGE_TYPE_DEEP
//                        ),
//                        SleepSessionRecord.Stage(
//                            startTime = start.plusMinutes(50).toInstant(),
//                            endTime = start.plusMinutes(60).toInstant(),
//                            stage = SleepSessionRecord.STAGE_TYPE_AWAKE
//                        ),
//                        SleepSessionRecord.Stage(
//                            startTime = start.plusMinutes(60).toInstant(),
//                            endTime = start.plusMinutes(70).toInstant(),
//                            stage = SleepSessionRecord.STAGE_TYPE_AWAKE_IN_BED
//                        ),
//                        SleepSessionRecord.Stage(
//                            startTime = start.plusMinutes(70).toInstant(),
//                            endTime = start.plusMinutes(80).toInstant(),
//                            stage = SleepSessionRecord.STAGE_TYPE_OUT_OF_BED
//                        ),
//                    )
//                ),
//            )
//        }
//
//        private fun getMockSleepMultipleEntries(): List<SleepSessionRecord> {
//            val start = ZonedDateTime.now().minusHours(2)
//            val end = start.plusMinutes(160)
//            return listOf(
//                SleepSessionRecord(
//                    startTime = start.toInstant(),
//                    startZoneOffset = start.offset,
//                    endTime = end.toInstant(),
//                    endZoneOffset = end.offset,
//                    stages = listOf(
//                        SleepSessionRecord.Stage(
//                            startTime = start.toInstant(),
//                            endTime = start.plusMinutes(10).toInstant(),
//                            stage = SleepSessionRecord.STAGE_TYPE_UNKNOWN
//                        ),
//                        SleepSessionRecord.Stage(
//                            startTime = start.plusMinutes(10).toInstant(),
//                            endTime = start.plusMinutes(20).toInstant(),
//                            stage = SleepSessionRecord.STAGE_TYPE_SLEEPING
//                        ),
//                        SleepSessionRecord.Stage(
//                            startTime = start.plusMinutes(20).toInstant(),
//                            endTime = start.plusMinutes(30).toInstant(),
//                            stage = SleepSessionRecord.STAGE_TYPE_LIGHT
//                        ),
//                        SleepSessionRecord.Stage(
//                            startTime = start.plusMinutes(30).toInstant(),
//                            endTime = start.plusMinutes(40).toInstant(),
//                            stage = SleepSessionRecord.STAGE_TYPE_REM
//                        ),
//                        SleepSessionRecord.Stage(
//                            startTime = start.plusMinutes(40).toInstant(),
//                            endTime = start.plusMinutes(50).toInstant(),
//                            stage = SleepSessionRecord.STAGE_TYPE_DEEP
//                        ),
//                        SleepSessionRecord.Stage(
//                            startTime = start.plusMinutes(50).toInstant(),
//                            endTime = start.plusMinutes(60).toInstant(),
//                            stage = SleepSessionRecord.STAGE_TYPE_AWAKE
//                        ),
//                        SleepSessionRecord.Stage(
//                            startTime = start.plusMinutes(60).toInstant(),
//                            endTime = start.plusMinutes(70).toInstant(),
//                            stage = SleepSessionRecord.STAGE_TYPE_AWAKE_IN_BED
//                        ),
//                        SleepSessionRecord.Stage(
//                            startTime = start.plusMinutes(70).toInstant(),
//                            endTime = start.plusMinutes(80).toInstant(),
//                            stage = SleepSessionRecord.STAGE_TYPE_OUT_OF_BED
//                        ),
//                    )
//                ),
//                SleepSessionRecord(
//                    startTime = start.toInstant(),
//                    startZoneOffset = start.offset,
//                    endTime = end.toInstant(),
//                    endZoneOffset = end.offset,
//                    stages = listOf(
//                        SleepSessionRecord.Stage(
//                            startTime = start.toInstant(),
//                            endTime = start.plusMinutes(5).toInstant(),
//                            stage = SleepSessionRecord.STAGE_TYPE_UNKNOWN
//                        ),
//                        SleepSessionRecord.Stage(
//                            startTime = start.plusMinutes(5).toInstant(),
//                            endTime = start.plusMinutes(10).toInstant(),
//                            stage = SleepSessionRecord.STAGE_TYPE_SLEEPING
//                        ),
//                        SleepSessionRecord.Stage(
//                            startTime = start.plusMinutes(10).toInstant(),
//                            endTime = start.plusMinutes(15).toInstant(),
//                            stage = SleepSessionRecord.STAGE_TYPE_LIGHT
//                        ),
//                        SleepSessionRecord.Stage(
//                            startTime = start.plusMinutes(15).toInstant(),
//                            endTime = start.plusMinutes(20).toInstant(),
//                            stage = SleepSessionRecord.STAGE_TYPE_REM
//                        ),
//                        SleepSessionRecord.Stage(
//                            startTime = start.plusMinutes(20).toInstant(),
//                            endTime = start.plusMinutes(25).toInstant(),
//                            stage = SleepSessionRecord.STAGE_TYPE_DEEP
//                        ),
//                        SleepSessionRecord.Stage(
//                            startTime = start.plusMinutes(25).toInstant(),
//                            endTime = start.plusMinutes(30).toInstant(),
//                            stage = SleepSessionRecord.STAGE_TYPE_AWAKE
//                        ),
//                        SleepSessionRecord.Stage(
//                            startTime = start.plusMinutes(35).toInstant(),
//                            endTime = start.plusMinutes(40).toInstant(),
//                            stage = SleepSessionRecord.STAGE_TYPE_AWAKE_IN_BED
//                        ),
//                        SleepSessionRecord.Stage(
//                            startTime = start.plusMinutes(45).toInstant(),
//                            endTime = start.plusMinutes(50).toInstant(),
//                            stage = SleepSessionRecord.STAGE_TYPE_OUT_OF_BED
//                        ),
//                    )
//                ),
//            )
//        }
//
//        private fun getMockSteps(): List<StepsRecord> {
//            val steps = mutableListOf<StepsRecord>()
//
//            for (i in 0 until 5) {
//                var start = ZonedDateTime.now().minusHours(2)
//                var count = 100L
//                steps.add(
//                    StepsRecord(
//                        startTime = start.toInstant(),
//                        startZoneOffset = start.offset,
//                        endTime = start.plusMinutes(10).toInstant(),
//                        endZoneOffset = start.offset,
//                        count = count
//                    )
//                )
//
//                start = start.plusMinutes(10)
//                count += 100
//            }
//            return steps
//        }
//    }
//
//    @Test
//    fun minutesSleptStagesTotal_is50Minutes() = runTest {
//        val minutesSlept = insightsRepo.getMinutesSlept(sleepRecords)
//        Assert.assertEquals(50.0, minutesSlept, 0.0)
//    }
//
//    @Test
//    fun minutesInBedStagesTotal_is70Minutes() = runTest {
//        val minutesInBed = insightsRepo.getMinutesInBed(sleepRecords)
//        Assert.assertEquals(70.0, minutesInBed, 0.0)
//    }
//
//    @Test
//    fun minutesInRem_is10Minutes() = runTest {
//        val minutes =
//            insightsRepo.getMinutesInSleepStage(summary, SleepSessionRecord.STAGE_TYPE_REM)
//        Assert.assertEquals(10.0, minutes, 0.0)
//    }
//
//    @Test
//    fun minutesInLight_is10Minutes() = runTest {
//        val minutes =
//            insightsRepo.getMinutesInSleepStage(summary, SleepSessionRecord.STAGE_TYPE_LIGHT)
//        Assert.assertEquals(10.0, minutes, 0.0)
//    }
//
//    @Test
//    fun minutesInDeep_is10Minutes() = runTest {
//        val minutes =
//            insightsRepo.getMinutesInSleepStage(summary, SleepSessionRecord.STAGE_TYPE_DEEP)
//        Assert.assertEquals(10.0, minutes, 0.0)
//    }
//
//    @Test
//    fun minutesInRem_multipleEntries_is15Minutes() = runTest {
//        val minutes = insightsRepo.getMinutesInSleepStage(
//            summaryMultiEntry,
//            SleepSessionRecord.STAGE_TYPE_REM
//        )
//        Assert.assertEquals(15.0, minutes, 0.0)
//    }
//
//    @Test
//    fun minutesInLight_multipleEntries_is15Minutes() = runTest {
//        val minutes = insightsRepo.getMinutesInSleepStage(
//            summaryMultiEntry,
//            SleepSessionRecord.STAGE_TYPE_LIGHT
//        )
//        Assert.assertEquals(15.0, minutes, 0.0)
//    }
//
//    @Test
//    fun minutesInDeep_multipleEntries_is15Minutes() = runTest {
//        val minutes = insightsRepo.getMinutesInSleepStage(
//            summaryMultiEntry,
//            SleepSessionRecord.STAGE_TYPE_DEEP
//        )
//        Assert.assertEquals(15.0, minutes, 0.0)
//    }
//
//    @Test
//    fun minutesSleptStagesTotal_is75Minutes() = runTest {
//        val minutesSlept = insightsRepo.getMinutesSlept(sleepRecordsMultiEntry)
//        Assert.assertEquals(75.0, minutesSlept, 0.0)
//    }
//
//    @Test
//    fun minutesInBedStagesTotal_is105Minutes() = runTest {
//        val minutesInBed = insightsRepo.getMinutesInBed(sleepRecordsMultiEntry)
//        Assert.assertEquals(105.0, minutesInBed, 0.0)
//    }
//
//    @Test
//    fun totalStepsTotal_is500() = runTest {
//        val totalSteps = insightsRepo.getStepCount(stepRecords)
//        Assert.assertEquals(500.0, totalSteps, 0.0)
//    }
//
//    @Test
//    fun postInsights_withInvalidToken_refreshesToken_isSuccessful() = runTest {
//        val now = Sahha.di.timeManager.nowInISO()
//        val insights = listOf(
//            InsightData(
//                "test", 100.0, "steps", now, now
//            )
//        )
//
//        suspendCancellableCoroutine { cont ->
//            Sahha.di.defaultScope.launch {
//                insightsRepo.postInsights("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJodHRwczovL2FwaS5zYWhoYS5haS9jbGFpbXMvcHJvZmlsZUlkIjoiZDUxNTQ1NjctY2JkMS00NGI5LWIxZDUtOTNmNWVkMGNmZTcyIiwiaHR0cHM6Ly9hcGkuc2FoaGEuYWkvY2xhaW1zL2V4dGVybmFsSWQiOiJzZGtfYW5kcm9pZF90ZXN0ZXIiLCJodHRwczovL2FwaS5zYWhoYS5haS9jbGFpbXMvYWNjb3VudElkIjoiYTA3ZmU5MTItNDZhMi00OGRhLWI4M2UtMTE0NmVlNWNkMjU4IiwiaHR0cHM6Ly9hcGkuc2FoaGEuYWkvY2xhaW1zL3NhaGhhQXBpU2NvcGUiOiJEZXZlbG9wbWVudCIsImV4cCI6MTY5NjYzNjc3NywiaXNzIjoiaHR0cHM6Ly9kZXZlbG9wbWVudC1hcGkuc2FoaGEuYWkiLCJhdWQiOiJodHRwczovL2RldmVsb3BtZW50LWFwaS5zYWhoYS5haSJ9.Zp_uOz7wLD4LuBe9zNu4wVo2QmslVWQrLMQSoedHgVM", insights) { err, success ->
//                    println()
//                    println(err)
//                    println(success)
//                    println()
//                    Assert.assertEquals(true, success)
//                    if (cont.isActive) cont.resume(Unit)
//                }
//            }
//        }
//    }
//
//    @Test
//    fun postInsights_withValidToken_isSuccessful() = runTest {
//        val now = Sahha.di.timeManager.nowInISO()
//        val insights = listOf(
//            InsightData(
//                "test", 100.0, "steps", now, now
//            )
//        )
//
//        suspendCancellableCoroutine { cont ->
//            Sahha.di.defaultScope.launch {
//                insightsRepo.postInsights(authRepo.getToken() ?: "", insights) { err, success ->
//                    println()
//                    println(err)
//                    println(success)
//                    println()
//                    Assert.assertEquals(true, success)
//                    if (cont.isActive) cont.resume(Unit)
//                }
//            }
//        }
//    }
//
//    @Test
//    fun testSummary_calculations() = runTest {
//        val summaries = insightsRepo.getSleepStageSummary(sleepRecords)
//
//        summaries.forEach {
//            val duration = it.value
//            val durationMins = duration / 1000 / 60
//            Assert.assertEquals(10.0, durationMins, 0.0)
//        }
//    }
//
////    @Ignore("Not working")
////    @Test
////    fun service() = runTest {
////        val intent = Intent(application, InsightsPostService::class.java)
////        withContext(Dispatchers.Main) {
////            serviceRule.startService(intent)
////        }
////
////        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
////        uiDevice.openNotification()
////        val notification =
////            uiDevice.findObject(UiSelector().textContains("Synchronizing insights..."))
////        Assert.assertTrue(notification.exists())
////
////        delay(5500)
////
////        val notificationAfterDelay =
////            uiDevice.findObject(UiSelector().textContains("Synchronizing insights..."))
////        Assert.assertFalse(notificationAfterDelay.exists())
////    }
//}