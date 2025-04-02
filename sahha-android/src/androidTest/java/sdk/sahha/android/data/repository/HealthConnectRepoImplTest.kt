//package sdk.sahha.android.data.repository
//
//import androidx.activity.ComponentActivity
//import android.content.Context
//import androidx.test.core.app.ApplicationProvider
//import kotlinx.coroutines.test.runTest
//import org.junit.Before
//import org.junit.Test
//import sdk.sahha.android.domain.repository.HealthConnectRepo
//import sdk.sahha.android.source.Sahha
//import sdk.sahha.android.source.SahhaEnvironment
//import sdk.sahha.android.source.SahhaSettings
//
//class HealthConnectRepoImplTest {
//    private lateinit var application: Application
//    private lateinit var context: Context
//    private var repo: HealthConnectRepo? = null
//
//    @Before
//    fun before() {
//        activity = ApplicationProvider.getApplicationContext()
//        context = ApplicationProvider.getApplicationContext()
//        runTest {
//            Sahha.configure(
//                application = application,
//                SahhaSettings(SahhaEnvironment.development)
//            )
//        }
//        repo = Sahha.di.healthConnectRepo
//    }
//
//    @Test
//    fun test() {
//
//    }
//}