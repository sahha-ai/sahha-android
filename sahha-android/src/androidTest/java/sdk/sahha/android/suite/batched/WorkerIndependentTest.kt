package sdk.sahha.android.suite.batched

import androidx.activity.ComponentActivity
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.GrantPermissionRule
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.Assert.assertEquals
import sdk.sahha.android.common.*
import sdk.sahha.android.framework.worker.post.DevicePostWorker
import sdk.sahha.android.framework.worker.post.SleepPostWorker
import sdk.sahha.android.framework.worker.post.StepPostWorker
import sdk.sahha.android.domain.model.device.PhoneUsage
import sdk.sahha.android.domain.model.dto.SleepDto
import sdk.sahha.android.domain.model.steps.StepData
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaEnvironment
import sdk.sahha.android.source.SahhaSettings
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val tag = "WorkerIndependentTest"

@OptIn(ExperimentalCoroutinesApi::class)
class WorkerIndependentTest {
    companion object {
        private lateinit var activity: ComponentActivity
        private val timeManager = SahhaTimeManager()

        @get:Rule
        val permissionRule: GrantPermissionRule =
            GrantPermissionRule.grant(android.Manifest.permission.ACTIVITY_RECOGNITION)

        @BeforeClass
        @JvmStatic
        fun beforeClass() = runTest {
            activity = ApplicationProvider.getApplicationContext()
            SahhaSetupUtil.configureSahha(activity, SahhaSettings(SahhaEnvironment.sandbox))
            createMockData()
        }

        private suspend fun createMockData() {
            SahhaReconfigure(activity)
            val movementDao = Sahha.di.movementDao
            val sleepDao = Sahha.di.sleepDao
            val deviceDao = Sahha.di.deviceUsageRepo

            movementDao.saveStepData(
                StepData(
                    "Test", 10, timeManager.nowInISO()
                )
            )

            sleepDao.saveSleepDto(
                SleepDto(
                    360, timeManager.nowInISO(), timeManager.nowInISO()
                )
            )

            deviceDao.saveUsages(
                listOf(
                    PhoneUsage(
                        true, true, timeManager.nowInISO()
                    )
                )
            )
        }
    }

    // Configure once first in isolation
    @Ignore("Ignore after configuration")
    @Test
    fun configureAndAuthenticate() = runTest {
        val sahhaSettings = SahhaSettings(
            SahhaEnvironment.sandbox
        )

        suspendCoroutine<Unit> { cont ->
            Sahha.configure(
                activity, sahhaSettings
            ) { _, success ->
                Assert.assertEquals(true, success)
                Log.d(tag, "configure: $success")
                cont.resume(Unit)
            }
        }

        suspendCoroutine<Unit> { cont ->
            Sahha.authenticate(
                appId, appSecret, externalId + UUID.randomUUID()
            ) { _, success ->
                Assert.assertEquals(true, success)
                Log.d(tag, "authenticate: $success")
                cont.resume(Unit)
            }
        }

        Assert.assertEquals(true, Sahha.di.sleepDao.getSleepDto().isNotEmpty())
        Assert.assertEquals(true, Sahha.di.movementDao.getAllStepData().isNotEmpty())
        Assert.assertEquals(true, Sahha.di.deviceUsageRepo.getUsages().isNotEmpty())
    }

    @Test
    fun sleepWorker_postsSuccessfully() = runTest {
        if (!Sahha.isAuthenticated)
            SahhaSetupUtil.authenticateSahha(appId, appSecret, externalId)

        val sleepWorker = TestListenableWorkerBuilder<SleepPostWorker>(activity).build()
        val result = sleepWorker.doWork()
        sleepWorker.stop()
        Assert.assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun deviceWorker_postsSuccessfully() = runTest {
        if (!Sahha.isAuthenticated)
            SahhaSetupUtil.authenticateSahha(appId, appSecret, externalId)

        val deviceWorker = TestListenableWorkerBuilder<DevicePostWorker>(activity).build()
        val result = deviceWorker.doWork()
        deviceWorker.stop()
        Assert.assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun stepWorker_postsSuccessfully() = runTest {
        if (!Sahha.isAuthenticated)
            SahhaSetupUtil.authenticateSahha(appId, appSecret, externalId)

        val stepWorker = TestListenableWorkerBuilder<StepPostWorker>(activity).build()
        val result = stepWorker.doWork()
        stepWorker.stop()
        Assert.assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun sleepWorker_noAuth_returnsSuccess() = runTest {
        if (Sahha.isAuthenticated)
            SahhaSetupUtil.deauthenticateSahha()

        val sleepWorker = TestListenableWorkerBuilder<SleepPostWorker>(activity).build()
        val result = sleepWorker.doWork()
        sleepWorker.stop()
        Assert.assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun deviceWorker_noAuth_returnsSuccess() = runTest {
        if (Sahha.isAuthenticated)
            SahhaSetupUtil.deauthenticateSahha()

        val deviceWorker = TestListenableWorkerBuilder<DevicePostWorker>(activity).build()
        val result = deviceWorker.doWork()
        deviceWorker.stop()
        Assert.assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun stepWorker_noAuth_returnsSuccess() = runTest {
        if (Sahha.isAuthenticated)
            SahhaSetupUtil.deauthenticateSahha()

        val stepWorker = TestListenableWorkerBuilder<StepPostWorker>(activity).build()
        val result = stepWorker.doWork()
        stepWorker.stop()
        Assert.assertEquals(ListenableWorker.Result.success(), result)
    }

    @Ignore("Not ready")
    @Test
    fun stepPost_isChunkedAndSent() = runTest {
        SahhaReconfigure(activity)
        Sahha.di.movementDao.clearAllStepData()
        for (i in 0 until 100) {
            Sahha.di.movementDao.saveStepData(
                StepData(
                    "Test", 10, timeManager.nowInISO()
                )
            )
        }

        val stepPostWorker = TestListenableWorkerBuilder<StepPostWorker>(activity).build()
        val result = stepPostWorker.doWork()
        stepPostWorker.stop()
        assertEquals(result, ListenableWorker.Result.success())
        assertEquals(0, Sahha.di.movementDao.getAllStepData().size)
    }
}