package sdk.sahha.android.suite.batched

import androidx.activity.ComponentActivity
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test
import sdk.sahha.android.common.Constants
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.common.SahhaSetupUtil
import sdk.sahha.android.common.appId
import sdk.sahha.android.common.appSecret
import sdk.sahha.android.common.externalId
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaEnvironment
import sdk.sahha.android.source.SahhaNotificationConfiguration
import sdk.sahha.android.source.SahhaSettings
import java.util.UUID

class SahhaReconfigureTest {
    companion object {
        private lateinit var activity: ComponentActivity

        @BeforeClass
        @JvmStatic
        fun beforeClass() = runTest {
            activity = ApplicationProvider.getApplicationContext()

            SahhaSetupUtil.configureSahha(
                activity,
                SahhaSettings(
                    SahhaEnvironment.sandbox,
                )
            )
            SahhaSetupUtil.authenticateSahha(appId, appSecret, externalId + UUID.randomUUID())
        }
    }

    @Ignore("No longer required")
    @Test
    fun reconfigure_changesEnvironment() = runTest {
        val context: Context = ApplicationProvider.getApplicationContext()

        SahhaReconfigure(context = context)
        val prefs =
            context.getSharedPreferences(Constants.CONFIGURATION_PREFS, Context.MODE_PRIVATE)
        var envInt = prefs.getInt(Constants.ENVIRONMENT_KEY, -1)
        var env = SahhaEnvironment.values()[envInt]

        println(env)
        Assert.assertEquals(SahhaEnvironment.sandbox, env)
        var err = SahhaSetupUtil.authenticateSahha(appId, appSecret, externalId + UUID.randomUUID())
        Assert.assertEquals(null, err)

        SahhaReconfigure(
            context = ApplicationProvider.getApplicationContext(),
            environment = SahhaEnvironment.production
        )

        envInt = prefs.getInt(Constants.ENVIRONMENT_KEY, -1)
        env = SahhaEnvironment.values()[envInt]

        println(env)
        Assert.assertEquals(SahhaEnvironment.production, env)
//        err = SahhaSetupUtil.authenticateSahha(appId, appSecret, externalId + UUID.randomUUID())
//        Assert.assertEquals(true, err?.isNotEmpty())
    }

    @Test
    fun reconfigure_withNotificationInfo() = runTest {
        val activity: ComponentActivity = ApplicationProvider.getApplicationContext()
        val notification = SahhaNotificationConfiguration(
            icon = androidx.appcompat.R.drawable.abc_btn_check_to_on_mtrl_015,
            title = "Test notification",
            shortDescription = "Testing the notification"
        )
        val settings = SahhaSettings(
            environment = SahhaEnvironment.sandbox,
            notificationSettings = notification
        )
        SahhaSetupUtil.configureSahha(activity, settings)
        SahhaReconfigure(activity)
    }

    @Test
    fun reconfigure_setsDependencies() = runTest {
        val context: Context = ApplicationProvider.getApplicationContext()

        try {
            Sahha.di.sahhaErrorLogger
            Sahha.di.defaultScope
            Sahha.di.sensorRepo
            Sahha.di.permissionManager
            Sahha.di.permissionHandler
            Sahha.di.sahhaConfigRepo
        } catch (e: Exception) {
            println(e.message)
            Assert.assertNotNull(e.message)
        }

        try {
            Sahha.sim.sensor
            Sahha.sim.auth
            Sahha.sim.userData
            Sahha.sim.insights
            Sahha.sim.permission
        } catch (e: Exception) {
            println(e.message)
            Assert.assertNotNull(e.message)
        }

        SahhaReconfigure(context = context)

        try {
            Sahha.di.sahhaErrorLogger
            Sahha.di.defaultScope
            Sahha.di.sensorRepo
            Sahha.di.permissionManager
            Sahha.di.permissionHandler
            Sahha.di.sahhaConfigRepo
        } catch (e: Exception) {
            println(e.message)
            Assert.assertNull(e.message)
        }

        try {
            Sahha.sim.sensor
            Sahha.sim.auth
            Sahha.sim.userData
            Sahha.sim.insights
            Sahha.sim.permission
        } catch (e: Exception) {
            println(e.message)
            Assert.assertNull(e.message)
        }
    }
}