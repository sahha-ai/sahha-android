//package sdk.sahha.android.source
//
//import androidx.test.core.app.ApplicationProvider
//import junit.framework.TestCase
//import kotlinx.coroutines.test.runTest
//import org.junit.Before
//import org.junit.Test
//import sdk.sahha.android.common.SahhaReconfigure
//
//class SahhaTest : TestCase() {
//    @Before
//    override fun setUp() {
//        super.setUp()
//        Sahha.configure(
//            ApplicationProvider.getApplicationContext(),
//            SahhaSettings(
//                SahhaEnvironment.development
//            )
//        )
////        Sahha.authenticate(
////            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJodHRwczovL2FwaS5zYWhoYS5haS9jbGFpbXMvcHJvZmlsZUlkIjoiMmYwYjEyMDctODQ2ZC00YmU5LWE3MzktYTc1NjEzYTdjOGE0IiwiaHR0cHM6Ly9hcGkuc2FoaGEuYWkvY2xhaW1zL2FjY291bnRJZCI6Ijk5NDA4ZmFlLWVkZTMtNDcwZS1hMWZhLWZlZTlhZmNlMmEwZSIsImV4cCI6MTY1NTQzMTEwOSwiaXNzIjoiaHR0cHM6Ly9zYW5kYm94LWFwaS5zYWhoYS5haSIsImF1ZCI6Imh0dHBzOi8vc2FuZGJveC1hcGkuc2FoaGEuYWkifQ.NRm2N_stccaj_GIt9rwkPuSDVMBIj1eWhkgI5ojCyps",
////            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJodHRwczovL2FwaS5zYWhoYS5haS9jbGFpbXMvcHJvZmlsZUlkIjoiMmYwYjEyMDctODQ2ZC00YmU5LWE3MzktYTc1NjEzYTdjOGE0IiwiaHR0cHM6Ly9hcGkuc2FoaGEuYWkvY2xhaW1zL2FjY291bnRJZCI6Ijk5NDA4ZmFlLWVkZTMtNDcwZS1hMWZhLWZlZTlhZmNlMmEwZSIsImV4cCI6MTY1NTQzMTEwOSwiaXNzIjoiaHR0cHM6Ly9zYW5kYm94LWFwaS5zYWhoYS5haSIsImF1ZCI6Imh0dHBzOi8vc2FuZGJveC1hcGkuc2FoaGEuYWkifQ.NRm2N_stccaj_GIt9rwkPuSDVMBIj1eWhkgI5ojCyps"
////        )
//    }
//
//    @Test
//    fun test_defaultSensors() {
//        val sensors = Sahha.getDefaultSensors()
//        assertEquals(false, sensors.contains(SahhaSensor.health_connect))
//    }
//
//    @Test
//    fun test_healthConnect() {
//        Sahha.enableHealthConnect(ApplicationProvider.getApplicationContext()) { error, status ->
//            error?.also { assertEquals("", error) }
//            assertEquals(SahhaSensorStatus.enabled, status.name)
//        }
//    }
//
//    var summary = ""
//
//    @Test
//    fun test_deviceInfo() = runTest {
//        Sahha.configure(
//            ApplicationProvider.getApplicationContext(), SahhaSettings(
//                SahhaEnvironment.development
//            )
//        )
//
//        Sahha.authenticate(
//            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJodHRwczovL2FwaS5zYWhoYS5haS9jbGFpbXMvcHJvZmlsZUlkIjoiY2ZhMmNiMjMtMWEyMi00OTViLTllZDYtMzgzZTFiNDMxYTI5IiwiaHR0cHM6Ly9hcGkuc2FoaGEuYWkvY2xhaW1zL2FjY291bnRJZCI6IjViNGY0MjUxLWRkZDEtNDIwZi1hMDVlLTBiMjJjNzc2OGViNSIsImV4cCI6MTY2NDA3MTcyOCwiaXNzIjoiaHR0cHM6Ly9zYW5kYm94LWFwaS5zYWhoYS5haSIsImF1ZCI6Imh0dHBzOi8vc2FuZGJveC1hcGkuc2FoaGEuYWkifQ.YXJdsfBmFIO6_DS6mZEvoPAcolGxBDtKOZbQsxQoSDw",
//            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJodHRwczovL2FwaS5zYWhoYS5haS9jbGFpbXMvcHJvZmlsZUlkIjoiY2ZhMmNiMjMtMWEyMi00OTViLTllZDYtMzgzZTFiNDMxYTI5IiwiaHR0cHM6Ly9hcGkuc2FoaGEuYWkvY2xhaW1zL2FjY291bnRJZCI6IjViNGY0MjUxLWRkZDEtNDIwZi1hMDVlLTBiMjJjNzc2OGViNSIsImV4cCI6MTY2NDA3MTcyOCwiaXNzIjoiaHR0cHM6Ly9zYW5kYm94LWFwaS5zYWhoYS5haSIsImF1ZCI6Imh0dHBzOi8vc2FuZGJveC1hcGkuc2FoaGEuYWkifQ.YXJdsfBmFIO6_DS6mZEvoPAcolGxBDtKOZbQsxQoSDw"
//        )
//
//        SahhaReconfigure(ApplicationProvider.getApplicationContext())
//
//        assertEquals("", Sahha.di.configurationDao.getDeviceInformation())
//    }
//
////    @Test
////    fun test() = runTest {
////        val jobs = listOf(
////            async { logFast() },
////            async { logMedium() },
////            async { logSlow() }
////        )
////        jobs.joinAll()
////        println(summary)
//////        assertEquals(true, true)
////    }
////
////    private suspend fun logFast() {
////        summary += ("logFast start\n")
////        delay(1000)
////        summary += ("logFast complete\n")
////    }
////
////    private suspend fun logMedium() {
////        summary += ("logMedium start\n")
////        delay(2000)
////        summary += ("logMedium complete\n")
////    }
////
////    private suspend fun logSlow() {
////        summary += ("logSlow start\n")
////        delay(3000)
////        summary += ("logSlow complete\n")
////    }
//
////    @Test
////    fun test_postStepData() = runTest {
////        Sahha.di.setDependencies(ApplicationProvider.getApplicationContext())
////        delay(1500)
////
////        Sahha.di.remotePostRepo.postStepData(
////            listOf(
////                StepData(
////                    Constants.STEP_COUNTER_DATA_SOURCE,
////                    1000,
////                    SahhaTimeManager().nowInISO()
////                )
////            )
////        ) { error, successful ->
////            runTest {
////                println(error)
////                delay(1500)
////                assertEquals(true, successful)
////            }
////        }
////    }
////
////    @Test
////    fun test_1000Steps() = runTest {
////        Sahha.di.setDependencies(ApplicationProvider.getApplicationContext())
////        Sahha.di.movementDao.clearAllStepData()
////
////        delay(1500)
////        saveSteps(1001)
//////        assertEquals(1001, Sahha.di.movementDao.getAllStepData().count())
////        Sahha.di.remotePostRepo.postStepData(
////            Sahha.di.movementDao.getAllStepData()
////        ) { error, successful ->
////            runTest {
////                println(error)
////                println(successful)
////                delay(1500)
//////                assertEquals(true, successful)
////                assertEquals(1, Sahha.di.movementDao.getAllStepData().count())
////            }
////        }
////    }
////
////    private suspend fun saveSteps(amount: Int) {
////        val stm = SahhaTimeManager()
////        for (i in 0 until amount) {
////            Sahha.di.movementDao.saveStepData(
////                StepData(
////                    source = Constants.STEP_DETECTOR_DATA_SOURCE,
////                    count = 1,
////                    detectedAt = stm.nowInISO()
////                )
////            )
////        }
////    }
//}