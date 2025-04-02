//package sdk.sahha.android.sandbox
//
//import androidx.activity.ComponentActivity
//import androidx.test.core.app.ApplicationProvider
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.test.runTest
//import org.junit.Assert
//import org.junit.Before
//import org.junit.Test
//import sdk.sahha.android.source.Sahha
//import sdk.sahha.android.source.SahhaEnvironment
//import sdk.sahha.android.source.SahhaSettings
//import kotlin.coroutines.resume
//import kotlin.coroutines.suspendCoroutine
//
//@OptIn(ExperimentalCoroutinesApi::class)
//class AuthCrashTest {
//    private lateinit var application: Application
//
//    @Before
//    fun before() = runTest {
//        activity = ApplicationProvider.getApplicationContext()
//        val settings = SahhaSettings(
//            SahhaEnvironment.sandbox
//        )
//
//        suspendCoroutine<Unit> { cont ->
//            Sahha.configure(application, settings) { _, _ ->
//                cont.resume(Unit)
//            }
//        }
//    }
//
//    @Test
//    fun configureWithoutAuth_doesNotCrash() = runTest {
//        suspendCoroutine { cont ->
//            Sahha.deauthenticate { error, success ->
//                Assert.assertEquals(false, Sahha.isAuthenticated)
//                cont.resume(Unit)
//            }
//        }
//    }
//}