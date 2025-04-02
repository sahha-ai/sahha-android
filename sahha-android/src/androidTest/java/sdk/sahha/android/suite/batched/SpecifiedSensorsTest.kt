//package sdk.sahha.android.suite.batched
//
//import androidx.activity.ComponentActivity
//import androidx.test.core.app.ApplicationProvider
//import kotlinx.coroutines.suspendCancellableCoroutine
//import kotlinx.coroutines.test.runTest
//import org.junit.Assert
//import org.junit.BeforeClass
//import org.junit.FixMethodOrder
//import org.junit.Ignore
//import org.junit.Test
//import org.junit.runners.MethodSorters
//import sdk.sahha.android.common.SahhaSetupUtil
//import sdk.sahha.android.source.Sahha
//import sdk.sahha.android.source.SahhaEnvironment
//import sdk.sahha.android.source.SahhaSensor
//import sdk.sahha.android.source.SahhaSensorStatus
//import sdk.sahha.android.source.SahhaSettings
//import kotlin.coroutines.resume
//
//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
//class SpecifiedSensorsTest {
//    companion object {
//        lateinit var application: Application
//
//        @BeforeClass
//        @JvmStatic
//        fun beforeClass() = runTest {
//            activity = ApplicationProvider.getApplicationContext()
//            val settings = SahhaSettings(
//                environment = SahhaEnvironment.sandbox
//            )
//            Sahha.configure(application, settings)
//            SahhaSetupUtil.enableSensors(
//                application, setOf(
//                    SahhaSensor.total_energy_burned, SahhaSensor.heart_rate
//                )
//            )
//        }
//    }
//
//    @Ignore("Manually tested ok, this test hangs for some reason")
//    @Test
//    fun test01_initial_status_isPending() = runTest {
//        suspendCancellableCoroutine { cont ->
//            if (cont.isActive) Sahha.getSensorStatus(
//                application,
//                null
//            ) { error, status ->
//                Assert.assertEquals(SahhaSensorStatus.pending, status)
//                error?.also { println(error) }
//                cont.resume(Unit)
//            }
//        }
//    }
//
//    @Test
//    fun test02_emptySet_isPending() = runTest {
//        suspendCancellableCoroutine { cont ->
//            if (cont.isActive) Sahha.getSensorStatus(
//                application,
//                setOf()
//            ) { error, status ->
//                Assert.assertEquals(SahhaSensorStatus.pending, status)
//                error?.also { println(error) }
//                cont.resume(Unit)
//            }
//        }
//    }
//
//    @Test
//    fun test03_emptySet_returnsError() = runTest {
//        suspendCancellableCoroutine { cont ->
//            if (cont.isActive) Sahha.getSensorStatus(
//                application,
//                setOf()
//            ) { error, status ->
//                Assert.assertNotNull(error)
//                cont.resume(Unit)
//            }
//        }
//    }
//
//    @Test
//    fun test04_nativeAndHealthConnectPermissions() = runTest {
//        suspendCancellableCoroutine { cont ->
//            if (cont.isActive) Sahha.getSensorStatus(
//                application,
//                setOf(SahhaSensor.step_count, SahhaSensor.sleep)
//            ) { error, status ->
//                Assert.assertEquals(SahhaSensorStatus.enabled, status)
//                cont.resume(Unit)
//            }
//        }
//    }
//
//    @Test
//    fun test05_healthConnectPermissionsOnly() = runTest {
//        suspendCancellableCoroutine { cont ->
//            if (cont.isActive) Sahha.getSensorStatus(
//                application,
//                setOf(SahhaSensor.heart_rate, SahhaSensor.blood_pressure_systolic)
//            ) { error, status ->
//                Assert.assertEquals(SahhaSensorStatus.enabled, status)
//                cont.resume(Unit)
//            }
//        }
//    }
//
//    @Test
//    fun test06_enableSensors_null_enablesAll() = runTest {
//        suspendCancellableCoroutine { cont ->
//            if (cont.isActive) Sahha.enableSensors(
//                application,
//                null
//            ) { error, status ->
//                Assert.assertEquals(SahhaSensorStatus.enabled, status)
//                cont.resume(Unit)
//            }
//        }
//    }
//}