//package sdk.sahha.android.suite.batched
//
//import androidx.activity.ComponentActivity
//import androidx.test.core.app.ApplicationProvider
//import kotlinx.coroutines.test.runTest
//import org.junit.Assert
//import org.junit.BeforeClass
//import org.junit.Test
//import sdk.sahha.android.common.SahhaSetupUtil
//import sdk.sahha.android.source.Sahha
//import sdk.sahha.android.source.SahhaEnvironment
//import sdk.sahha.android.source.SahhaSensor
//import sdk.sahha.android.source.SahhaSettings
//
//class HCManifestPermissionsTest {
//    companion object {
//        private lateinit var application: Application
//
//        @JvmStatic
//        @BeforeClass
//        fun beforeClass() = runTest {
//            activity = ApplicationProvider.getApplicationContext()
//            val settings = SahhaSettings(
//                environment = SahhaEnvironment.sandbox,
//            )
//            SahhaSetupUtil.configureSahha(activity, settings)
//            SahhaSetupUtil.enableSensors(
//                application,
//                sensors = setOf(SahhaSensor.step_count, SahhaSensor.floor_count)
//            )
//        }
//    }
//
//    private val stepsDeclaration = "android.permission.health.READ_STEPS"
//    private val floorsClimbedDeclaration = "android.permission.health.READ_FLOORS_CLIMBED"
//
//    @Test
//    fun missingDeclaration_isIgnored() = runTest {
//        val permissionManager = Sahha.di.permissionManager
//
//        val permissions = permissionManager.getTrimmedHcPermissions(
//            setOf(stepsDeclaration), SahhaSensor.values().toSet()
//        )
//
//        println(permissions)
//        Assert.assertEquals(false, permissions.contains(floorsClimbedDeclaration))
//    }
//
//    @Test
//    fun noDeclarations_usesExpectedPermissions() = runTest {
//        val permissionManager = Sahha.di.permissionManager
//        val permissions = permissionManager.getTrimmedHcPermissions(null, SahhaSensor.values().toSet())
//
//        println(permissions)
//        Assert.assertEquals(true, permissions.contains(stepsDeclaration))
//        Assert.assertEquals(true, permissions.contains(floorsClimbedDeclaration))
//    }
//
//    @Test
//    fun emptySet_usesExpectedPermissions() = runTest {
//        val permissionManager = Sahha.di.permissionManager
//        val permissions = permissionManager.getTrimmedHcPermissions(setOf(), SahhaSensor.values().toSet())
//
//        println(permissions)
//        Assert.assertEquals(true, permissions.contains(stepsDeclaration))
//        Assert.assertEquals(true, permissions.contains(floorsClimbedDeclaration))
//    }
//}