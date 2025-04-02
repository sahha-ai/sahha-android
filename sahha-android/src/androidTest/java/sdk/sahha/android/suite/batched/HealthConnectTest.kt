package sdk.sahha.android.suite.batched

import androidx.activity.ComponentActivity
import android.os.Build
import androidx.health.connect.client.HealthConnectClient
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import sdk.sahha.android.common.SahhaSetupUtil
import sdk.sahha.android.domain.manager.PermissionManager
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaEnvironment
import sdk.sahha.android.source.SahhaSettings

class HealthConnectTest {
    companion object {
        lateinit var activity: ComponentActivity
        internal lateinit var pm: PermissionManager
        private val healthConnectClient by lazy { Sahha.di.healthConnectClient }

        @BeforeClass
        @JvmStatic
        fun beforeClass() = runTest {
            activity = ApplicationProvider.getApplicationContext()
            val settings = SahhaSettings(environment = SahhaEnvironment.sandbox)
            SahhaSetupUtil.configureSahha(activity, settings)

            pm = Sahha.di.permissionManager
        }
    }

    @Test
    fun sdkVersion27_unableToUseHealthConnect() = runTest {
        val result = pm.shouldUseHealthConnect(Build.VERSION_CODES.O_MR1)

        Assert.assertEquals(false, result)
    }

    @Test
    fun sdkVersion28_ableToUseHealthConnect() = runTest {
        
        val result = pm.shouldUseHealthConnect(Build.VERSION_CODES.P)

        healthConnectClient?.also {
            Assert.assertEquals(true, result)
        } ?: Assert.assertEquals(false, result)
    }

    @Test
    fun sdkVersion29_ableToUseHealthConnect() = runTest {
        
        val result = pm.shouldUseHealthConnect(Build.VERSION_CODES.Q)

        healthConnectClient?.also {
            Assert.assertEquals(true, result)
        } ?: Assert.assertEquals(false, result)
    }

    @Test
    fun sdkVersion30_ableToUseHealthConnect() = runTest {
        
        val result = pm.shouldUseHealthConnect( Build.VERSION_CODES.R)

        healthConnectClient?.also {
            Assert.assertEquals(true, result)
        } ?: Assert.assertEquals(false, result)
    }

    @Test
    fun sdkVersion31_ableToUseHealthConnect() = runTest {
        
        val result = pm.shouldUseHealthConnect(Build.VERSION_CODES.S)

        healthConnectClient?.also {
            Assert.assertEquals(true, result)
        } ?: Assert.assertEquals(false, result)
    }

    @Test
    fun sdkVersion32_ableToUseHealthConnect() = runTest {
        
        val result = pm.shouldUseHealthConnect( Build.VERSION_CODES.S_V2)

        healthConnectClient?.also {
            Assert.assertEquals(true, result)
        } ?: Assert.assertEquals(false, result)
    }

    @Test
    fun sdkVersion33_ableToUseHealthConnect() = runTest {
        
        val result = pm.shouldUseHealthConnect(Build.VERSION_CODES.TIRAMISU)

        healthConnectClient?.also {
            Assert.assertEquals(true, result)
        } ?: Assert.assertEquals(false, result)
    }

    @Test
    fun sdkVersion34_ableToUseHealthConnect() = runTest {
        
        val result = pm.shouldUseHealthConnect( Build.VERSION_CODES.UPSIDE_DOWN_CAKE)

        healthConnectClient?.also {
            Assert.assertEquals(true, result)
        } ?: Assert.assertEquals(false, result)
    }
}