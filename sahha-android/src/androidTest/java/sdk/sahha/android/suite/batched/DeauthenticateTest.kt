package sdk.sahha.android.suite.batched

import androidx.activity.ComponentActivity
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.SahhaSetupUtil
import sdk.sahha.android.common.appId
import sdk.sahha.android.common.appSecret
import sdk.sahha.android.common.externalId
import sdk.sahha.android.domain.model.device.PhoneUsage
import sdk.sahha.android.domain.model.dto.SleepDto
import sdk.sahha.android.domain.model.steps.StepSession
import sdk.sahha.android.framework.worker.post.DevicePostWorker
import sdk.sahha.android.framework.worker.post.SleepPostWorker
import sdk.sahha.android.framework.worker.post.StepPostWorker
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaEnvironment
import sdk.sahha.android.source.SahhaSettings
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class DeauthenticateTest {
    companion object {
        lateinit var activity: ComponentActivity

        @BeforeClass
        @JvmStatic
        fun before() = runTest {
            activity = ApplicationProvider.getApplicationContext()
            val settings = SahhaSettings(SahhaEnvironment.sandbox)
            SahhaSetupUtil.configureSahha(activity, settings)
        }
    }

    @After
    fun tearDown() = runTest {
        clearTestData()
        try {
            Sahha.di.mutex.unlock()
        } catch (e: Exception) {
            println(e.message)
        }
    }

    @Ignore("Hanging")
    @Test
    fun postingDataWhileAuth_isSuccessful() = runTest {
        SahhaSetupUtil.authenticateSahha(appId, appSecret, externalId)
        clearTestData()
        storeTestData()
        val successful = postData()
        Assert.assertEquals(true, successful)
    }

    @Test
    fun postingDataWhileDeauth_returnsError() = runTest {
        SahhaSetupUtil.authenticateSahha(appId, appSecret, externalId)
        clearTestData()
        storeTestData()
        Sahha.deauthenticate { _, _ ->
            postData { err, success ->
                Assert.assertEquals(false, success)
                Assert.assertEquals(SahhaErrors.noToken, err)
            }
        }
    }

    private suspend fun clearTestData() {
        Sahha.di.sensorRepo.clearAllStepSessions()
        Sahha.di.sleepDao.clearSleepDto()
        Sahha.di.deviceUsageRepo.clearAllUsages()
    }

    private suspend fun storeTestData() {
        Sahha.di.sensorRepo.saveStepSession(
            StepSession(
                100,
                Sahha.di.timeManager.nowInISO(),
                Sahha.di.timeManager.nowInISO(),
            )
        )

        Sahha.di.sleepDao.saveSleepDto(
            SleepDto(
                360,
                Sahha.di.timeManager.nowInISO(),
                Sahha.di.timeManager.nowInISO(),
            )
        )

        Sahha.di.deviceUsageRepo.saveUsages(
            listOf(
                PhoneUsage(
                    true, false, Sahha.di.timeManager.nowInISO()
                )
            )
        )

        println("*************************************")
        println(Sahha.di.sleepDao.getSleepDto())
        println(Sahha.di.deviceUsageRepo.getUsages())
        println(Sahha.di.sensorRepo.getAllStepSessions())
        println("*************************************")
    }

    private suspend fun postData(
        callback: ((error: String?, success: Boolean) -> Unit)? = null
    ): Boolean = suspendCancellableCoroutine { cont ->
        Sahha.sim.sensor.postSensorData(activity) { err, success ->
            callback?.invoke(err, success)
            if (cont.isActive)
                cont.resume(success)
        }
    }

    @Test
    fun deviceWorker_withDataNoAuth_returnsSuccessAndRetainsData() = runTest {
        Sahha.deauthenticate { _, _ ->
            suspendCancellableCoroutine { cont ->
                runBlocking {
                    clearTestData()
                    storeTestData()
                    val worker = TestListenableWorkerBuilder<DevicePostWorker>(activity).build()
                    val result = worker.doWork()

                    Assert.assertEquals(ListenableWorker.Result.success(), result)
                    Assert.assertEquals(1, Sahha.di.deviceUsageRepo.getUsages().count())
                    if (cont.isActive) cont.resume(Unit)
                }
            }
        }
    }

    @Test
    fun sleepWorker_withDataNoAuth_returnsSuccessAndRetainsData() = runTest {
        Sahha.deauthenticate { _, _ ->
            suspendCancellableCoroutine { cont ->
                runBlocking {
                    clearTestData()
                    storeTestData()
                    val worker = TestListenableWorkerBuilder<SleepPostWorker>(activity).build()
                    val result = worker.doWork()

                    Assert.assertEquals(ListenableWorker.Result.success(), result)
                    Assert.assertEquals(1, Sahha.di.sleepDao.getSleepDto().count())
                    if (cont.isActive) cont.resume(Unit)
                }
            }
        }
    }

    @Test
    fun stepWorker_withDataNoAuth_returnsSuccessAndRetainsData() = runTest {
        Sahha.deauthenticate { _, _ ->
            suspendCancellableCoroutine { cont ->
                runBlocking {
                    clearTestData()
                    storeTestData()
                    val worker = TestListenableWorkerBuilder<StepPostWorker>(activity).build()
                    val result = worker.doWork()

                    Assert.assertEquals(ListenableWorker.Result.success(), result)
                    Assert.assertEquals(1, Sahha.di.sensorRepo.getAllStepSessions().count())
                    if (cont.isActive) cont.resume(Unit)
                }
            }
        }
    }

    @Test
    fun deviceWorker_withDataAndAuth_isSuccessfulAndClearsData() = runTest {
        SahhaSetupUtil.authenticateSahha(appId, appSecret, externalId)
        clearTestData()
        storeTestData()
        val worker = TestListenableWorkerBuilder<DevicePostWorker>(activity).build()
        val result = worker.doWork()

        Assert.assertEquals(ListenableWorker.Result.success(), result)
        Assert.assertEquals(0, Sahha.di.deviceUsageRepo.getUsages().count())
    }

    @Test
    fun sleepWorker_withDataAndAuth_isSuccessfulAndClearsData() = runTest {
        SahhaSetupUtil.authenticateSahha(appId, appSecret, externalId)
        clearTestData()
        storeTestData()
        val worker = TestListenableWorkerBuilder<SleepPostWorker>(activity).build()
        val result = worker.doWork()

        Assert.assertEquals(ListenableWorker.Result.success(), result)
        Assert.assertEquals(0, Sahha.di.sleepDao.getSleepDto().count())
    }

    @Test
    fun stepWorker_withDataAndAuth_workerSuccess() = runTest {
        SahhaSetupUtil.authenticateSahha(appId, appSecret, externalId)
        clearTestData()
        storeTestData()
        val worker = TestListenableWorkerBuilder<StepPostWorker>(activity).build()
        val result = worker.doWork()

        Assert.assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun stepWorker_withDataAndAuth_hasNoStepSessions() = runTest {
        SahhaSetupUtil.authenticateSahha(appId, appSecret, externalId)
        clearTestData()
        storeTestData()
        val worker = TestListenableWorkerBuilder<StepPostWorker>(activity).build()
        val result = worker.doWork()

        Assert.assertEquals(0, Sahha.di.sensorRepo.getAllStepSessions().count())
    }

    @Test
    fun deauthenticate_whenNoTokensStored_hasNoRefreshToken() = runTest {
        SahhaSetupUtil.authenticateSahha(appId, appSecret, externalId)

        val success = suspendCoroutine<Boolean> { cont ->
            Sahha.deauthenticate { _, success ->
                cont.resume(success)
            }
        }

        Assert.assertEquals(true, Sahha.di.authRepo.getRefreshToken().isNullOrEmpty())

        val result = suspendCoroutine<Pair<String, Boolean>> { cont ->
            Sahha.deauthenticate { err, success ->
                cont.resume(Pair(err ?: "", success))
            }
        }

        Assert.assertEquals(Pair("", true), result)
    }

    @Test
    fun deauthenticate_whenNoTokensStored_returnsSuccessful() = runTest {
        SahhaSetupUtil.authenticateSahha(appId, appSecret, externalId)

        val success = suspendCoroutine<Boolean> { cont ->
            Sahha.deauthenticate { _, success ->
                cont.resume(success)
            }
        }

        Assert.assertEquals(true, success)

        val result = suspendCoroutine<Pair<String, Boolean>> { cont ->
            Sahha.deauthenticate { err, success ->
                cont.resume(Pair(err ?: "", success))
            }
        }

        Assert.assertEquals(Pair("", true), result)
    }

    @Test
    fun deauthenticate_whenNoTokensStored_hasNoToken() = runTest {
        SahhaSetupUtil.authenticateSahha(appId, appSecret, externalId)

        val success = suspendCoroutine<Boolean> { cont ->
            Sahha.deauthenticate { _, success ->
                cont.resume(success)
            }
        }

        Assert.assertEquals(true, Sahha.di.authRepo.getToken().isNullOrEmpty())

        val result = suspendCoroutine<Pair<String, Boolean>> { cont ->
            Sahha.deauthenticate { err, success ->
                cont.resume(Pair(err ?: "", success))
            }
        }

        Assert.assertEquals(Pair("", true), result)
    }

}