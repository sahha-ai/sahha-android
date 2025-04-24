//package sdk.sahha.android.domain.use_case.post
//
//import androidx.activity.ComponentActivity
//import android.util.Log
//import androidx.test.core.app.ApplicationProvider
//import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
//import androidx.test.uiautomator.UiDevice
//import kotlinx.coroutines.test.runTest
//import org.junit.Assert.assertEquals
//import org.junit.Before
//import org.junit.Test
//import sdk.sahha.android.source.*
//import java.util.concurrent.CountDownLatch
//import java.util.concurrent.TimeUnit
//
//private const val tag = "PostHealthConnectDataUseCaseTest"
//private const val profile =
//    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJodHRwczovL2FwaS5zYWhoYS5haS9jbGFpbXMvcHJvZmlsZUlkIjoiY2ZhMmNiMjMtMWEyMi00OTViLTllZDYtMzgzZTFiNDMxYTI5IiwiaHR0cHM6Ly9hcGkuc2FoaGEuYWkvY2xhaW1zL2FjY291bnRJZCI6IjViNGY0MjUxLWRkZDEtNDIwZi1hMDVlLTBiMjJjNzc2OGViNSIsImV4cCI6MTY2NDA3MTcyOCwiaXNzIjoiaHR0cHM6Ly9zYW5kYm94LWFwaS5zYWhoYS5haSIsImF1ZCI6Imh0dHBzOi8vc2FuZGJveC1hcGkuc2FoaGEuYWkifQ.YXJdsfBmFIO6_DS6mZEvoPAcolGxBDtKOZbQsxQoSDw"
//
//class PostHealthConnectDataUseCaseTest {
//    private lateinit var application: Application
//    private lateinit var uiDevice: UiDevice
//
//    @Before
//    fun before() {
//        activity = ApplicationProvider.getApplicationContext()
//        uiDevice = UiDevice.getInstance(getInstrumentation())
//
//        val sahhaSettings = SahhaSettings(
//            environment = SahhaEnvironment.development,
//            sensors = setOf(SahhaSensor.health_connect)
//        )
//
//        var latch = CountDownLatch(1)
//
//        Sahha.configure(
//            application,
//            sahhaSettings
//        ) { error, success ->
//            latch.countDown()
//        }
//        latch.await()
//        Log.d(tag, "configure complete")
//
//        latch = CountDownLatch(1)
//        Sahha.authenticate(profile, profile) { error, success ->
//            latch.countDown()
//        }
//        Log.d(tag, "auth complete")
//        latch.await()
//    }
//
//    @Test
//    fun heartRatePost_returnsUnavailableError() {
//        val latch = CountDownLatch(1)
//        Sahha.postHealthConnectData(
//            setOf(
//                HealthConnectSensor.heart_rate
//            )
//        ) { _, successful ->
//            assertEquals(false, successful)
//            latch.countDown()
//        }
//        latch.await(30, TimeUnit.SECONDS)
//    }
//
//    @Test
//    fun noSpecifiedSensors_postsAllSensors() {
//        val latch = CountDownLatch(1)
//        runTest {
//            Sahha.di.postHealthConnectDataUseCase { error, successful ->
//                val successCount = Sahha.di.postHealthConnectDataUseCase.successes.count()
//                val sensorCount = HealthConnectSensor.values().count()
//
//                Log.d(tag, "sensor count: $sensorCount")
//                Log.d(tag, "success count: $successCount")
//
//                assertEquals(sensorCount, successCount)
//                latch.countDown()
//            }
//        }
//        latch.await()
//    }
//}