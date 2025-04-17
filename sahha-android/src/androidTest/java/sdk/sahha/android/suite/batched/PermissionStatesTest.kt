//package sdk.sahha.android.suite.batched
//
//import androidx.activity.ComponentActivity
//import androidx.compose.ui.test.assertTextContains
//import androidx.compose.ui.test.junit4.createAndroidComposeRule
//import androidx.compose.ui.test.onNodeWithText
//import androidx.compose.ui.test.performClick
//import androidx.test.core.app.ActivityScenario
//import androidx.test.core.app.ApplicationProvider
//import androidx.test.core.app.launchActivity
//import androidx.test.platform.app.InstrumentationRegistry
//import androidx.test.rule.ActivityTestRule
//import androidx.test.uiautomator.UiDevice
//import androidx.test.uiautomator.UiSelector
//import kotlinx.coroutines.test.runTest
//import org.junit.Assert
//import org.junit.BeforeClass
//import org.junit.Rule
//import org.junit.Test
//import sdk.sahha.android.common.SahhaErrors
//import sdk.sahha.android.common.SahhaSensorPermissionActivity
//import sdk.sahha.android.source.Sahha
//import sdk.sahha.android.source.SahhaEnvironment
//import sdk.sahha.android.source.SahhaSensorStatus
//import sdk.sahha.android.source.SahhaSettings
//import kotlin.coroutines.resume
//import kotlin.coroutines.suspendCoroutine
//
//private const val physical_activity = "physical activity?"
//private const val allow = "Allow"
//private const val dont_allow = "Don't allow"
//
//class PermissionStatesTest {
//    companion object {
//        private lateinit var application: Application
//
//        @BeforeClass
//        @JvmStatic
//        fun beforeClass() = runTest {
//            activity = ApplicationProvider.getApplicationContext()
//        }
//    }
//
//    suspend fun configureSahha(settings: SahhaSettings): Pair<String?, Boolean> =
//        suspendCoroutine { cont ->
//            Sahha.configure(application, settings) { error, success ->
//                cont.resume(Pair(error, success))
//            }
//        }
//
//    suspend fun enableSensors(): Pair<String?, Enum<SahhaSensorStatus>> =
//        suspendCoroutine { cont ->
//            Sahha.enableSensors(application) { error, status ->
//                cont.resume(Pair(error, status))
//            }
//        }
//
//    suspend fun getSensorStatus(): Pair<String?, Enum<SahhaSensorStatus>> =
//        suspendCoroutine { cont ->
//            Sahha.getSensorStatus(application) { error, status ->
//                cont.resume(Pair(error, status))
//            }
//        }
//
//    @Test
//    fun emptySet_returnsPendingAndError() = runTest {
//        val settings = SahhaSettings(environment = SahhaEnvironment.sandbox, sensors = setOf())
//        configureSahha(settings)
//
//        val enableResult = enableSensors()
//        Assert.assertEquals(SahhaSensorStatus.pending, enableResult.second)
//        Assert.assertEquals(SahhaErrors.dataTypesUnspecified, enableResult.first)
//
//        val getSensorResult = getSensorStatus()
//        Assert.assertEquals(SahhaSensorStatus.pending, getSensorResult.second)
//        Assert.assertEquals(SahhaErrors.dataTypesUnspecified, getSensorResult.first)
//    }
//
//    @Test
//    fun deviceOnly_firstLaunch_isPending() = runTest {
//
//    }
//
//    @Test
//    fun deviceOnly_enablePermission_isAlwaysEnabled() = runTest {
//
//    }
//
//    @Test
//    fun deviceOnly_permissionEnabled_relaunch_remainsEnabled() = runTest {
//
//    }
//
//    @Test
//    fun allSensors_firstLaunch_isPending() = runTest {
//
//    }
//
//    @Test
//    fun allSensors_enableNativePermission_isDisabledOrEnabled() = runTest {
//        val result = enableSensors()
//        val selector = UiSelector()
//        ActivityScenario.launch(SahhaSensorPermissionActivity::class.java)
//        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
//        val popup = device.findObject(selector.text(physical_activity))
//        Assert.assertEquals(true, popup.exists())
//        val allowButton = device.findObject(selector.text(allow))
//        allowButton.click()
//        Assert.assertEquals(SahhaSensorStatus.enabled, result.second)
//    }
//
//    @Test
//    fun allSensors_disableNativePermission_isDisabled() = runTest {
//
//    }
//
//    @Test
//    fun allSensors_cancelNativePermission_isPending() = runTest {
//
//    }
//
//    @Test
//    fun allSensors_cancelNativePermissionTWICE_isDisabled() = runTest {
//
//    }
//}