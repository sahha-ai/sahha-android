//package sdk.sahha.android.suite.batched
//package sdk.sahha.android.suite
//
//import androidx.activity.ComponentActivity
//import androidx.test.core.app.ApplicationProvider
//import androidx.work.testing.TestListenableWorkerBuilder
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.test.runTest
//import org.junit.Assert
//import org.junit.BeforeClass
//import org.junit.Ignore
//import org.junit.Test
//import sdk.sahha.android.common.SahhaSetupUtil
//import sdk.sahha.android.common.appId
//import sdk.sahha.android.common.appSecret
//import sdk.sahha.android.common.externalId
//import sdk.sahha.android.data.worker.post.silver_format.SilverDevicePostWorker
//import sdk.sahha.android.domain.model.device.PhoneUsage
//import sdk.sahha.android.domain.model.device.PhoneUsageHourly
//import sdk.sahha.android.domain.model.device.PhoneUsageSilver
//import sdk.sahha.android.source.Sahha
//import sdk.sahha.android.source.SahhaEnvironment
//import sdk.sahha.android.source.SahhaSettings
//import kotlin.coroutines.resume
//import kotlin.coroutines.suspendCoroutine
//
//@OptIn(ExperimentalCoroutinesApi::class)
//class SilverPhoneUsageFormatTest {
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
//    fun phoneUsageData_segregatesHourly() = runTest {
//        clearAllPhoneUsageRelatedData()
//        storeHoursOfPhoneUsageData(3, 10, true, true)
//        Assert.assertEquals(2, getHourlyPhoneUsageData().count())
//    }
//
//    @Test
//    fun phoneUsageData_correctLockedCount() = runTest {
//        clearAllPhoneUsageRelatedData()
//        storeHoursOfPhoneUsageData(2, 5, true, true)
//        Assert.assertEquals(5, getHourlyPhoneUsageData().first().lockCount)
//    }
//
//    @Test
//    fun phoneUsageData_correctUnlockedCount() = runTest {
//        clearAllPhoneUsageRelatedData()
//        storeHoursOfPhoneUsageData(2, 10, false, true)
//        Assert.assertEquals(10, getHourlyPhoneUsageData().first().unlockCount)
//    }
//
//    @Test
//    fun phoneUsageData_correctScreenOn() = runTest {
//        clearAllPhoneUsageRelatedData()
//        storeHoursOfPhoneUsageData(2, 15, true, true)
//        Assert.assertEquals(15, getHourlyPhoneUsageData().first().screenOnCount)
//    }
//
//    @Test
//    fun phoneUsageData_correctScreenOff() = runTest {
//        clearAllPhoneUsageRelatedData()
//        storeHoursOfPhoneUsageData(2, 20, true, false)
//        Assert.assertEquals(20, getHourlyPhoneUsageData().first().screenOffCount)
//    }
//
//    private suspend fun getHourlyPhoneUsageData(): List<PhoneUsageHourly> {
//        val usagesHourly = Sahha.di.postSilverDeviceDataUseCase.getPreparedData()
//        logHourlyPhoneUsageData(usagesHourly)
//        return usagesHourly
//    }
//
//    private suspend fun storeHoursOfPhoneUsageData(
//        hours: Long,
//        usagesAmount: Int,
//        isLocked: Boolean,
//        isScreenOn: Boolean
//    ) {
//        var hour = Sahha.di.timeManager.nowInISO()
//
//        for (i in 0 until hours) {
//            for (j in 0 until usagesAmount) {
//                Sahha.di.sensorRepo.savePhoneUsage(
//                    PhoneUsage(isLocked, isScreenOn, hour)
//                )
//                Sahha.di.sensorRepo.savePhoneUsageSilver(
//                    PhoneUsageSilver(isLocked, isScreenOn, hour)
//                )
//            }
//            hour = decrementOneHourIso(hour)
//        }
//    }
//
//    private fun decrementOneHourIso(hour: String): String {
//        return Sahha.di.timeManager.isoTimePlusHours(hour, -1)
//    }
//
//    private suspend fun clearAllPhoneUsageRelatedData() {
//        Sahha.di.sensorRepo.clearAllPhoneUsages()
//        Sahha.di.sensorRepo.clearAllPhoneUsagesSilver()
//    }
//
//    @Ignore("Endpoint not yet implemented")
//    @Test
//    fun hourlyPhoneUsage_chunksAreAsExpected() = runTest {
//        clearAllPhoneUsageRelatedData()
//        storeHoursOfPhoneUsageData(60, 3, true, true)
//
//        val silverPhoneUsageWorker =
//            TestListenableWorkerBuilder<SilverDevicePostWorker>(application).build()
//        silverPhoneUsageWorker.doWork()
//
//        getHourlyPhoneUsageData() // Used to log
//
//        val postedChunks = Sahha.di.postChunkManager.postedChunkCount
//
//        Assert.assertEquals(2, postedChunks)
//    }
//
//    @Test
//    fun incompleteCurrentHourBlock_isNotSent() = runTest {
//        clearAllPhoneUsageRelatedData()
//        storeHoursOfPhoneUsageData(2, 10, true, true)
//
//        Assert.assertEquals(1, getHourlyPhoneUsageData().count())
//    }
//
//    private fun logHourlyPhoneUsageData(usages: List<PhoneUsageHourly>) {
//        println("*************************\n\n")
//        usages.forEach {
//            println("lockCount: " + it.lockCount)
//            println("unlockCount: " + it.unlockCount)
//            println("screenOnCount: " + it.screenOnCount)
//            println("screenOffCount: " + it.screenOffCount)
//            println("start: " + it.start)
//            println("end: " + it.end + "\n\n")
//        }
//        println("*************************")
//    }
//}