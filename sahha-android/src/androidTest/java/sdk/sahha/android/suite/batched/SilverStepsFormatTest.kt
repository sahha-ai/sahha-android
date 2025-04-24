package sdk.sahha.android.suite.batched//package sdk.sahha.android.suite
//
//import androidx.activity.ComponentActivity
//import androidx.test.core.app.ApplicationProvider
//import androidx.work.ListenableWorker
//import androidx.work.WorkInfo
//import androidx.work.testing.TestListenableWorkerBuilder
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.test.runTest
//import org.junit.Assert
//import org.junit.BeforeClass
//import org.junit.Ignore
//import org.junit.Test
//import sdk.sahha.android.common.SahhaSetupUtil
//import sdk.sahha.android.common.appId
//import sdk.sahha.android.common.appSecret
//import sdk.sahha.android.common.externalId
//import sdk.sahha.android.common.Constants
//import sdk.sahha.android.data.worker.post.silver_format.SilverStepPostWorker
//import sdk.sahha.android.domain.model.steps.StepData
//import sdk.sahha.android.domain.model.steps.StepSession
//import sdk.sahha.android.source.Sahha
//import sdk.sahha.android.source.SahhaEnvironment
//import sdk.sahha.android.source.SahhaSettings
//import java.time.ZonedDateTime
//import java.time.temporal.ChronoUnit
//import kotlin.coroutines.resume
//import kotlin.coroutines.suspendCoroutine
//
//@OptIn(ExperimentalCoroutinesApi::class)
//class SilverStepsFormatTest {
//    companion object {
//        lateinit var application: Application
//
//        @JvmStatic
//        @BeforeClass
//        fun beforeClass() = runTest {
//            activity = ApplicationProvider.getApplicationContext()
//            val settings = SahhaSettings(environment = SahhaEnvironment.development)
//            suspendCoroutine<Unit> { cont ->
//                Sahha.configure(application, settings) { _, _ ->
//                    cont.resume(Unit)
//                }
//            }
//            SahhaSetupUtil.authenticateSahha(
//                appId, appSecret, externalId
//            )
//        }
//    }
//
//    @Test
//    fun stepData_segregatesHourly() = runTest {
//        val silverStepsWorker =
//            TestListenableWorkerBuilder<SilverStepPostWorker>(application).build()
//        store3HoursOfStepData()
//        val result = silverStepsWorker.doWork()
//
//        Assert.assertEquals(ListenableWorker.Result.success(), result)
//
//
//        val sessions = silverStepsWorker.useCase.hourlySteps
//        logHourlyStepData(sessions)
//
//        Assert.assertEquals(2, sessions.count())
//        // Expect 2 because the current hour should not be counted
//
//        val currentHour = Sahha.di.timeManager.zonedDateTimeToIso(
//            ZonedDateTime.now().truncatedTo(ChronoUnit.HOURS)
//        )
//        val lastHour = Sahha.di.timeManager.zonedDateTimeToIso(
//            ZonedDateTime.now().minusHours(1).truncatedTo(ChronoUnit.HOURS)
//        )
//        val last2Hours = Sahha.di.timeManager.zonedDateTimeToIso(
//            ZonedDateTime.now().minusHours(2).truncatedTo(ChronoUnit.HOURS)
//        )
//        Assert.assertEquals(
//            false,
//            sessions.contains(sessions.find { it.startDateTime == currentHour })
//        )
//        Assert.assertEquals(null, sessions.find { it.startDateTime == currentHour }?.count)
//        Assert.assertEquals(
//            true,
//            sessions.contains(sessions.find { it.startDateTime == lastHour })
//        )
//        Assert.assertEquals(20, sessions.find { it.startDateTime == lastHour }?.count)
//        Assert.assertEquals(
//            true,
//            sessions.contains(sessions.find { it.startDateTime == last2Hours })
//        )
//        Assert.assertEquals(30, sessions.find { it.startDateTime == last2Hours }?.count)
//    }
//
//    @Test
//    fun hourlySteps_chunksAreAsExpected() = runTest {
//        storeHourlyStepDataAmount(Constants.STEP_SESSION_POST_LIMIT * 3)
//
//        val silverStepsWorker =
//            TestListenableWorkerBuilder<SilverStepPostWorker>(application).build()
//        silverStepsWorker.doWork()
//
//        val sessions = silverStepsWorker.useCase.hourlySteps
//        logHourlyStepData(sessions)
//
//        val postedChunks = Sahha.di.postChunkManager.postedChunkCount
//
//        Assert.assertEquals(3, postedChunks)
//    }
//
//    @Test
//    fun incompleteHourBlock_isNotSent() = runTest {
//        Sahha.di.sensorRepo.clearAllStepData()
//        Sahha.di.sensorRepo.clearAllStepSessions()
//
//        var hourZdt = ZonedDateTime.now()
//        val steps = 10
//
//        storeSingleStepsAmount(steps, getIsoFromZdt(hourZdt))
//        storeSingleStepsAmount(steps, getIsoFromZdt(hourZdt))
//
//        hourZdt = hourZdt.minusHours(1)
//        storeSingleStepsAmount(steps, getIsoFromZdt(hourZdt))
//        storeSingleStepsAmount(steps, getIsoFromZdt(hourZdt))
//        storeSingleStepsAmount(steps, getIsoFromZdt(hourZdt))
//
//        val silverStepsWorker =
//            TestListenableWorkerBuilder<SilverStepPostWorker>(application).build()
//        silverStepsWorker.doWork()
//
//        Assert.assertEquals(1, silverStepsWorker.useCase.hourlySteps.count())
//        Assert.assertEquals(30, silverStepsWorker.useCase.hourlySteps.first().count)
//    }
//
//    @Ignore("Worker not found")
//    @Test
//    fun silverStepsPostWorker_isScheduled() = runTest {
//        val workInfo = suspendCoroutine<WorkInfo?> { cont ->
//            Sahha.sim.start { err, _ ->
//                Assert.assertEquals(null, err)
//                Sahha.di.defaultScope.launch {
//                    val info =
//                        Sahha.di.sensorRepo.getWorkerInfoByTag(Constants.HOURLY_STEP_POST_WORKER_TAG)
//                    cont.resume(info)
//                }
//            }
//        }
//
//        Assert.assertEquals(WorkInfo.State.ENQUEUED, workInfo)
//    }
//
//    private fun logHourlyStepData(sessions: List<StepSession>) {
//        println("*************************\n\n")
//        sessions.forEach {
//            println(it.count)
//            println(it.startDateTime)
//            println(it.endDateTime + "\n\n")
//        }
//        println("*************************")
//    }
//
//    private suspend fun storeHourlyStepDataAmount(amount: Int) {
//        Sahha.di.sensorRepo.clearAllStepData()
//        Sahha.di.sensorRepo.clearAllStepSessions()
//
//        var hourZdt = ZonedDateTime.now()
//        val steps = 10
//
//        for (i in 0 until amount) {
//            storeSingleStepsAmount(steps, getIsoFromZdt(hourZdt))
//            hourZdt = hourZdt.minusHours(1)
//        }
//    }
//
//    private fun getIsoFromZdt(zdt: ZonedDateTime): String {
//        return Sahha.di.timeManager.zonedDateTimeToIso(zdt)
//    }
//
//    private suspend fun store3HoursOfStepData() {
//        Sahha.di.sensorRepo.clearAllStepData()
//        Sahha.di.sensorRepo.clearAllStepSessions()
//
//        val currentHour = Sahha.di.timeManager.nowInISO()
//        val lastHour = Sahha.di.timeManager.zonedDateTimeToIso(
//            ZonedDateTime.now().minusHours(1)
//        )
//        val last2Hours = Sahha.di.timeManager.zonedDateTimeToIso(
//            ZonedDateTime.now().minusHours(2)
//        )
//
//        storeSingleStepsAmount(10, currentHour)
//        storeSingleStepsAmount(20, lastHour)
//        storeSingleStepsAmount(30, last2Hours)
//    }
//
//    private suspend fun storeSingleStepsAmount(amount: Int, isoTimeStamp: String) {
//        for (i in 0 until amount) {
//            Sahha.di.sensorRepo.saveStepData(
//                StepData(
//                    Constants.STEP_DETECTOR_DATA_SOURCE,
//                    1,
//                    isoTimeStamp
//                )
//            )
//        }
//    }
//}