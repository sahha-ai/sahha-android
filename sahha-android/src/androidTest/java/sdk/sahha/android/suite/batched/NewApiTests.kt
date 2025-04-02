@file:OptIn(ExperimentalCoroutinesApi::class)

package sdk.sahha.android.suite.batched

import androidx.activity.ComponentActivity
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.TestListenableWorkerBuilder
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.*
import sdk.sahha.android.common.*
import sdk.sahha.android.common.SahhaSetupUtil.authenticateSahha
import sdk.sahha.android.common.SahhaSetupUtil.configureSahha
import sdk.sahha.android.domain.model.device.PhoneUsage
import sdk.sahha.android.domain.model.dto.SleepDto
import sdk.sahha.android.domain.model.steps.StepData
import sdk.sahha.android.domain.model.steps.StepSession
import sdk.sahha.android.framework.worker.post.DevicePostWorker
import sdk.sahha.android.framework.worker.post.SleepPostWorker
import sdk.sahha.android.framework.worker.post.StepPostWorker
import sdk.sahha.android.source.*
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

private const val tag = "NewApiTests"

class NewApiTests {
    private var workerJob: Job? = null
    private var postJob: Job? = null

    companion object {
        private lateinit var activity: ComponentActivity
        private val timeManager = SahhaTimeManager()

        @BeforeClass
        @JvmStatic
        fun beforeClass() = runTest {
            activity = ApplicationProvider.getApplicationContext()

            configureSahha(
                activity,
                SahhaSettings(
                    SahhaEnvironment.sandbox,
                )
            )
            authenticateSahha(appId, appSecret, externalId + UUID.randomUUID())
            SahhaSetupUtil.enableSensors(activity, SahhaSensor.values().toSet())
        }
    }

    @After
    fun tearDown() {
        workerJob?.cancel()
        postJob?.cancel()
    }

    @Ignore("Test in isolation")
    @Test
    fun migrateOldEncryptedData_isSuccessful() = runTest {
        val deferredResult = CompletableDeferred<Unit>()

        clearAndStoreTestEncryptedData()
        val oldToken = getOldDecryptedData(sdk.sahha.android.common.Constants.UET)
        val oldRefreshToken = getOldDecryptedData(sdk.sahha.android.common.Constants.UERT)
        Sahha.sim.auth.migrateDataIfNeeded { error, success ->
            if (success) {
                val newToken = Sahha.di.authRepo.getToken()
                val newRefreshToken = Sahha.di.authRepo.getRefreshToken()
                Assert.assertEquals(newToken, oldToken)
                Assert.assertEquals(newRefreshToken, oldRefreshToken)
                deferredResult.complete(Unit)
            } else {
                Assert.fail(error)
            }
        }

        deferredResult.await()
    }

    @Test
    fun noOldEncryptedDataToMigrate_skipsMigrating() = runTest {
        val latch = CountDownLatch(1)

        Sahha.di.securityDao.deleteAllEncryptedData()
        Sahha.sim.auth.migrateDataIfNeeded { error, success ->
            if (success) {
                Assert.assertEquals(true, success)
                latch.countDown()
            } else {
                Assert.fail(error)
            }
        }

        latch.await(5, TimeUnit.SECONDS)
    }

    private suspend fun getOldDecryptedData(alias: String): String {
        val d = Sahha.di.decryptor
        return d.decrypt(alias)
    }

    private suspend fun clearAndStoreTestEncryptedData() {
        val dao = Sahha.di.securityDao
        val e = Sahha.di.encryptor

        dao.deleteAllEncryptedData()
        e.encryptText(sdk.sahha.android.common.Constants.UET, "test-token")
        e.encryptText(sdk.sahha.android.common.Constants.UERT, "test-refresh-token")
    }

    @Test
    fun postingDemographic_isSuccessful() = runTest {
        val latch = CountDownLatch(1)

        Sahha.postDemographic(
            SahhaDemographic(
                age = 25,
                gender = "Male",
            )
        ) { error, success ->
            if (success) {
                Assert.assertEquals(true, success)
                latch.countDown()
            } else {
                Assert.fail(error)
            }
        }

        latch.await(5, TimeUnit.SECONDS)
    }

    @Test
    fun gettingDemographic_isSuccessful() = runTest {
        val latch = CountDownLatch(1)

        Sahha.getDemographic { error, demographic ->
            error?.also {
                Assert.fail(it)
                latch.countDown()
            }

            demographic?.also {
                Assert.assertEquals(25, it.age)
                Assert.assertEquals("Male", it.gender)
                latch.countDown()
            } ?: Assert.fail("Demographic was null")
        }

        latch.await(5, TimeUnit.SECONDS)
    }

//    @Test
//    fun postingSensorData_isSuccessful() = runTest {
//        val deferredResult = CompletableDeferred<Unit>()
//
//        try {
//            val now = timeManager.nowInISO()
//
//            Sahha.di.sensorRepo.savePhoneUsage(
//                PhoneUsage(
//                    true, true, now
//                )
//            )
//
//            Sahha.di.sensorRepo.saveStepData(
//                StepData(
//                    Constants.STEP_COUNTER_DATA_SOURCE,
//                    10,
//                    now
//                )
//            )
//
//            Sahha.di.sensorRepo.saveStepSession(
//                StepSession(
//                    10,
//                    now,
//                    now
//                )
//            )
//
//            Sahha.di.sleepDao.saveSleepDto(
//                SleepDto(
//                    100,
//                    now,
//                    now,
//                )
//            )
//            Log.d(tag, "Created mock data")
//
//            Sahha.sim.sensor.postSensorData { error, success ->
//                Log.d(tag, "Posted sensor data")
//                error?.also { Log.e(tag, it) }
//                Log.d(tag, "Posted sensor data: $success")
//                Assert.assertEquals(true, error.isNullOrEmpty())
//
//                if (success) {
//                    launch(Dispatchers.IO) {
//                        Assert.assertEquals(true, success)
//                        Assert.assertEquals(true, Sahha.di.sleepDao.getSleepDto().isEmpty())
//                        Assert.assertEquals(true, Sahha.di.sensorRepo.getAllPhoneUsages().isEmpty())
////                        Assert.assertEquals(true, Sahha.di.movementDao.getAllStepData().isEmpty())
//                        Assert.assertEquals(
//                            true,
//                            Sahha.di.sensorRepo.getAllStepSessions().isEmpty()
//                        )
//                        Log.d(tag, "Cleared mock data")
//                        deferredResult.complete(Unit)
//                    }
//                }
//            }
//
//            deferredResult.await()
//        } catch (e: Exception) {
//            Assert.fail(e.message)
//        }
//    }

    //    @Ignore("Test in isolation, buggy when run with other tests")
    @Test
    fun analyze_isSuccessful() = runTest {
        val deferredResult = CompletableDeferred<Unit>()

        Sahha.getScores(setOf(SahhaScoreType.activity),) { error, success ->
            error?.also {
                Log.e(tag, "Analyze Error: $it")
                Assert.fail(it)
            }

            success?.also {
                Log.d(tag, "Analyze Success: $it")
                Assert.assertEquals(true, it.isNotEmpty())
            }
            deferredResult.complete(Unit)
        }

        deferredResult.await()
    }

    @Test
    fun getNewProfileToken_isSuccessful() = runTest {
        val latch = CountDownLatch(1)

        Sahha.di.authRepo.postRefreshToken({}) { error, success ->
            error?.also {
                Assert.fail(it)
            }

            Assert.assertEquals(true, success)
            latch.countDown()
        }

        latch.await(5, TimeUnit.SECONDS)
    }

    @Test
    fun sleepManualPost_isBlockedByAutomaticPost() = runTest {
        if (Sahha.di.permissionManager.shouldUseHealthConnect()) {
            println("Skipping: Test not valid on Health Connect")
            return@runTest
        }

        Sahha.di.sleepDao.clearSleepDto()
        Sahha.di.sleepDao.saveSleepDto(
            SleepDto(
                100,
                timeManager.nowInISO(),
                timeManager.nowInISO(),
            )
        )

        val sleepWorker = TestListenableWorkerBuilder<SleepPostWorker>(activity).build()
        val deferredResult = CompletableDeferred<Unit>()

        sleepWorker.postSleepData {
            Sahha.sim.sensor.postSensorData(activity) { error, _ ->
                error?.also {
                    Log.e(tag, it)
                    Assert.assertEquals(true, it.contains(SahhaErrors.postingInProgress))
                }
                deferredResult.complete(Unit)
            }
        }

        deferredResult.await()
    }

    @Ignore("Difficult to test")
    @Test
    fun sleepAutomaticPost_isBlockedByManualPost() = runTest {
        Sahha.di.sleepDao.clearSleepDto()
        Sahha.di.sleepDao.saveSleepDto(
            SleepDto(
                100,
                timeManager.nowInISO(),
                timeManager.nowInISO(),
            )
        )
        val sleepWorker = TestListenableWorkerBuilder<SleepPostWorker>(activity).build()

        postJob = launch {
            Sahha.sim.sensor.postSensorData(activity) { _, _ -> }
        }
        workerJob = launch {
            val result = sleepWorker.startWork()
            Assert.assertEquals(true, Sahha.di.sleepDao.getSleepDto().isNotEmpty())
        }

        postJob?.join()
        workerJob?.join()
    }

    @Test
    fun stepManualPost_isBlockedByAutomaticPost() = runTest {
        if (Sahha.di.permissionManager.shouldUseHealthConnect()) {
            println("Skipping: Test not valid on Health Connect")
            return@runTest
        }

        Sahha.di.sensorRepo.clearAllStepSessions()
        Sahha.di.sensorRepo.saveStepSession(
            StepSession(
                10,
                timeManager.nowInISO(),
                timeManager.nowInISO()
            )
        )

        val stepWorker = TestListenableWorkerBuilder<StepPostWorker>(activity).build()
        val deferredResult = CompletableDeferred<Unit>()

        stepWorker.postStepSessions {
            Sahha.sim.sensor.postSensorData(activity) { error, _ ->
                error?.also {
                    Log.e(tag, it)
                    Assert.assertEquals(true, it.contains(SahhaErrors.postingInProgress))
                }
                deferredResult.complete(Unit)
            }
        }
        deferredResult.await()
    }


    @Ignore("Difficult to test")
    @Test
    fun stepAutomaticPost_isBlockedByManualPost() = runTest {
        Sahha.di.movementDao.clearAllStepData()
        val stepWorker = TestListenableWorkerBuilder<StepPostWorker>(activity).build()
        Sahha.di.movementDao.saveStepData(
            StepData(
                sdk.sahha.android.common.Constants.STEP_COUNTER_DATA_SOURCE,
                10,
                timeManager.nowInISO()
            )
        )
        postJob = launch {
            Sahha.sim.sensor.postSensorData(activity) { _, _ -> }
        }
        workerJob = launch {
            val result = stepWorker.startWork()
            Assert.assertEquals(true, Sahha.di.movementDao.getAllStepData().isNotEmpty())
        }

        postJob?.join()
        workerJob?.join()
    }

    @Test
    fun deviceManualPost_isBlockedByAutomaticPost() = runTest {
        if (Sahha.di.permissionManager.shouldUseHealthConnect()) {
            println("Skipping: Test not valid on Health Connect")
            return@runTest
        }

        Sahha.di.deviceUsageRepo.clearAllUsages()
        Sahha.di.deviceUsageRepo.saveUsages(
            listOf(
                PhoneUsage(
                    true, true, timeManager.nowInISO()
                )
            )
        )

        val deviceWorker = TestListenableWorkerBuilder<DevicePostWorker>(activity).build()
        val deferredResult = CompletableDeferred<Unit>()

        deviceWorker.postDeviceData {
            Sahha.sim.sensor.postSensorData(activity) { error, _ ->
                error?.also {
                    Log.e(tag, it)
                    Assert.assertEquals(true, it.contains(SahhaErrors.postingInProgress) ?: false)
                }
                deferredResult.complete(Unit)
            }
        }
        deferredResult.await()
    }

    @Ignore("Difficult to test")
    @Test
    fun deviceAutomaticPost_isBlockedByManualPost() = runTest {
        Sahha.di.deviceUsageRepo.clearAllUsages()
        val deviceWorker = TestListenableWorkerBuilder<DevicePostWorker>(activity).build()
        Sahha.di.deviceUsageRepo.saveUsages(
            listOf(
                PhoneUsage(
                    true, true, timeManager.nowInISO()
                )
            )
        )

        postJob = launch {
            Sahha.sim.sensor.postSensorData(activity) { _, _ -> }
        }
        workerJob = launch {
            val result = deviceWorker.startWork()
            Assert.assertEquals(true, Sahha.di.deviceUsageRepo.getUsages().isNotEmpty())
        }

        postJob?.join()
        workerJob?.join()
    }

//    @Test
//    fun multipleWorkers_causesRetryOnClash() = runTest {
//        clearAndCreateMockSteps(1)
//        clearAndCreateMockSleep()
//
//        val sleepWorker = TestListenableWorkerBuilder<SleepPostWorker>(activity).build()
//        val stepWorker = TestListenableWorkerBuilder<StepPostWorker>(activity).build()
//
//        var sleepResult = sleepWorker.doWork()
//        Assert.assertEquals(androidx.work.ListenableWorker.Result.success(), sleepResult)
//        var stepResult = stepWorker.doWork()
//        Assert.assertEquals(androidx.work.ListenableWorker.Result.success(), stepResult)
//
//        clearAndCreateMockSteps(1)
//        clearAndCreateMockSleep()
//
//        val deferredResult = CompletableDeferred<androidx.work.ListenableWorker.Result>()
//        sleepResult = sleepWorker.postSleepData {
//            runTest { deferredResult.complete(stepWorker.postStepSessions()) }
//        }
//        stepResult = deferredResult.await()
//
//        Assert.assertEquals(androidx.work.ListenableWorker.Result.retry(), stepResult)
//        Assert.assertEquals(androidx.work.ListenableWorker.Result.success(), sleepResult)
//    }

//    @Ignore("Irrelevant to new step sessions")
//    @Test
//    fun postingStepData_isDuplicateFree() = runTest {
//        clearAndCreateMockSteps(Constants.STEP_POST_LIMIT * 5)
//        val stepWorker = TestListenableWorkerBuilder<StepPostWorker>(activity).build()
//
//        val result = suspendCoroutine<androidx.work.ListenableWorker.Result> { cont ->
//            Sahha.getSensorStatus(activity) { _, status ->
//                println("Status: ${status.name}")
//                try {
//                    runTest {
//                        val r = stepWorker.postStepSessions()
//                        cont.resume(r)
//                    }
//                } catch (e: Exception) {
//                    cont.resume(androidx.work.ListenableWorker.Result.failure())
//                    println(e.message)
//                }
//            }
//        }
//
//        Assert.assertEquals(androidx.work.ListenableWorker.Result.success(), result)
//        Assert.assertEquals(0, Sahha.di.movementDao.getAllStepData().count())
//    }

//    private suspend fun clearAndCreateMockSteps(size: Int) {
//        Sahha.di.sensorRepo.clearAllStepData()
//        Sahha.di.sensorRepo.clearAllStepSessions()
//        for (i in 0 until size) {
//            Sahha.di.sensorRepo.saveStepData(
//                StepData(
//                    Constants.STEP_COUNTER_DATA_SOURCE,
//                    10 * i,
//                    timeManager.nowInISO()
//                )
//            )
//            Sahha.di.sensorRepo.saveStepSession(
//                StepSession(
//                    10 * i,
//                    timeManager.nowInISO(),
//                    timeManager.nowInISO()
//                )
//            )
//        }
//    }

    private suspend fun clearAndCreateMockSleep() {
        Sahha.di.sleepDao.clearSleepDto()
        Sahha.di.sleepDao.saveSleepDto(
            SleepDto(
                100,
                timeManager.nowInISO(),
                timeManager.nowInISO(),
            )
        )
    }
}