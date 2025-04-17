package sdk.sahha.android.suite.batched

import androidx.activity.ComponentActivity
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.TestListenableWorkerBuilder
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test
import sdk.sahha.android.common.SahhaSetupUtil
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.common.appId
import sdk.sahha.android.common.appSecret
import sdk.sahha.android.framework.worker.post.DevicePostWorker
import sdk.sahha.android.framework.worker.post.SleepPostWorker
import sdk.sahha.android.framework.worker.post.StepPostWorker
import sdk.sahha.android.domain.model.device.PhoneUsage
import sdk.sahha.android.domain.model.dto.SleepDto
import sdk.sahha.android.domain.model.steps.StepData
import sdk.sahha.android.domain.model.steps.StepSession
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaEnvironment
import sdk.sahha.android.source.SahhaSettings
import java.util.*

class ChunkingTest {
    companion object {
        private lateinit var stepPostWorker: StepPostWorker
        private lateinit var sleepPostWorker: SleepPostWorker
        private lateinit var devicePostWorker: DevicePostWorker
        private lateinit var activity: ComponentActivity
        private val timeManager = SahhaTimeManager()

        private var stepData = listOf<StepData>()
        private var stepSessions = listOf<StepSession>()
        private var sleepData = listOf<SleepDto>()
        private var phoneLockData = listOf<PhoneUsage>()

        private var id = 1

        @BeforeClass
        @JvmStatic
        fun beforeClass() = runTest {
            println(id++)
            activity = ApplicationProvider.getApplicationContext()

            println(id++)
            SahhaSetupUtil.configureSahha(
                activity,
                SahhaSettings(environment = SahhaEnvironment.sandbox)
            )
            println(id++)
            SahhaSetupUtil.authenticateSahha(appId, appSecret, UUID.randomUUID().toString())

            println(id++)
            stepPostWorker = TestListenableWorkerBuilder<StepPostWorker>(activity).build()
            println(id++)
            sleepPostWorker = TestListenableWorkerBuilder<SleepPostWorker>(activity).build()
            println(id++)
            devicePostWorker = TestListenableWorkerBuilder<DevicePostWorker>(activity).build()

            println(id++)
            withContext(Dispatchers.IO) {
                println(id++)
                stepData = createTestStepData(100)
                println(id++)
                stepSessions = createTestStepSessions(100)
                println(id++)
                sleepData = createTestSleepData(100)
                println(id++)
                phoneLockData = createTestPhoneLockData(300)
            }
        }

        private suspend fun createTestPhoneLockData(size: Int): List<PhoneUsage> {
            for (i in 1..size) {
                Sahha.di.deviceUsageRepo.saveUsages(
                    listOf(
                        PhoneUsage(
                            true, true, timeManager.nowInISO()
                        )
                    )
                )
            }
            return Sahha.di.deviceUsageRepo.getUsages()
        }

        private suspend fun createTestStepData(size: Int): List<StepData> {
            for (i in 1..size) {
                Sahha.di.movementDao.saveStepData(
                    StepData(
                        "Test", 10, timeManager.nowInISO()
                    )
                )
            }
            return Sahha.di.movementDao.getAllStepData()
        }

        private suspend fun createTestStepSessions(size: Int): List<StepSession> {
            for (i in 1..size) {
                Sahha.di.sensorRepo.saveStepSession(
                    StepSession(
                        10, timeManager.nowInISO(), timeManager.nowInISO()
                    )
                )
            }
            return Sahha.di.sensorRepo.getAllStepSessions()
        }

        private suspend fun createTestSleepData(size: Int): List<SleepDto> {
            for (i in 1..size) {
                Sahha.di.sleepDao.saveSleepDto(
                    SleepDto(
                        360, timeManager.nowInISO(), timeManager.nowInISO()
                    )
                )
            }
            return Sahha.di.sleepDao.getSleepDto()
        }
    }

    @Ignore("Obsolete with introduction to step sessions")
    @Test
    fun chunkManager_sendsStepsSuccessfully() = runTest {
        // Given
        Sahha.di.movementDao.clearAllStepData()
        Assert.assertEquals(0, Sahha.di.movementDao.getAllStepData().count())
        val deferredResult = CompletableDeferred<Boolean>()

        // When
        Sahha.sim.sensor.postStepDataUseCase(stepData) { _, success ->
            deferredResult.complete(success)
        }
        val result = deferredResult.await()

        // Then
        Assert.assertEquals(3, Sahha.di.postChunkManager.postedChunkCount)
        Assert.assertEquals(true, result)
    }

    @Test
    fun chunkManager_sendsStepSessionsSuccessfully() = runTest {
        // Given
        Sahha.di.sensorRepo.clearAllStepSessions()
        Assert.assertEquals(0, Sahha.di.sensorRepo.getAllStepSessions().count())
        val deferredResult = CompletableDeferred<Boolean>()

        // When
        Sahha.di.sensorRepo.postStepSessions(stepSessions) { _, success ->
            deferredResult.complete(success)
        }
        val result = deferredResult.await()

        // Then
        Assert.assertEquals(3, Sahha.di.postChunkManager.postedChunkCount)
        Assert.assertEquals(true, result)
    }

    @Test
    fun chunkManager_sendsSleepSuccessfully() = runTest {
        // Given
        Sahha.di.sleepDao.clearSleepDto()
        val deferredResult = CompletableDeferred<Boolean>()

        // When
        Sahha.sim.sensor.postSleepDataUseCase(sleepData) { _, success ->
            deferredResult.complete(success)
        }
        val result = deferredResult.await()

        // Then
        Assert.assertEquals(3, Sahha.di.postChunkManager.postedChunkCount)
        Assert.assertEquals(true, result)
    }

    @Test
    fun chunkManager_sendsPhoneLocksSuccessfully() = runTest {
        // Given
        Sahha.di.deviceUsageRepo.clearAllUsages()
        Assert.assertEquals(0, Sahha.di.deviceUsageRepo.getUsages().count())
        val deferredResult = CompletableDeferred<Boolean>()

        // When
        Sahha.sim.sensor.postDeviceDataUseCase(phoneLockData) { error, success ->
            Log.d("SensorRepoImpl", error.toString())
            deferredResult.complete(success)
        }
        val result = deferredResult.await()

        // Then
        Assert.assertEquals(0, Sahha.di.deviceUsageRepo.getUsages().count())
        Assert.assertEquals(4, Sahha.di.postChunkManager.postedChunkCount)
        Assert.assertEquals(true, result)
    }
}