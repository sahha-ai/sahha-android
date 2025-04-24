package sdk.sahha.android.suite.batched

import androidx.activity.ComponentActivity
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test
import sdk.sahha.android.common.SahhaSetupUtil
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.common.appId
import sdk.sahha.android.common.appSecret
import sdk.sahha.android.data.local.SahhaDatabase
import sdk.sahha.android.data.local.SahhaDbUtility
import sdk.sahha.android.domain.model.device_info.DeviceInformation
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaEnvironment
import sdk.sahha.android.source.SahhaSettings
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class DeviceInfoTest {
    companion object {
        private lateinit var activity: ComponentActivity
        private val ioScope = CoroutineScope(Dispatchers.IO)
        private val settings = SahhaSettings(
            environment = SahhaEnvironment.sandbox
        )
        private lateinit var db: SahhaDatabase
        private lateinit var packageName: String
        private lateinit var deviceInfo: DeviceInformation
        private lateinit var externalId: String

        @BeforeClass
        @JvmStatic
        fun beforeClass() = runTest {
            activity = ApplicationProvider.getApplicationContext()
            packageName = activity.packageManager.getPackageInfo(
                activity.packageName,
                0
            ).packageName

            db = SahhaDbUtility.getDb(activity)
            deviceInfo = DeviceInformation(
                sdkId = settings.framework.name,
                appId = packageName,
                timeZone = SahhaTimeManager().getTimezone()
            )
            externalId = UUID.randomUUID().toString()
        }
    }

    @Before
    fun before() = runTest {
//        db.configurationDao().clearDeviceInformation()
    }

    @Test
    fun configureNoData_savesNothing() = runTest {
        db.configurationDao().clearDeviceInformation()
        SahhaSetupUtil.configureSahha(activity, settings)
        val data = Sahha.di.configurationDao.getDeviceInformation()
        Assert.assertEquals(null, data)
    }

    @Test
    fun configureSameData_savesNothing() = runTest {
        db.configurationDao().clearDeviceInformation()
        db.configurationDao().saveDeviceInformation(deviceInfo)
        SahhaSetupUtil.configureSahha(activity, settings)
        val data = Sahha.di.configurationDao.getDeviceInformation()
        Assert.assertEquals(deviceInfo, data)
    }

    @Test
    fun configureDiffData_savesCurrentInfo() = runTest {
        db.configurationDao().clearDeviceInformation()
        val diffInfo = deviceInfo.copy(appId = "different.app.id")
        db.configurationDao().saveDeviceInformation(diffInfo)
        SahhaSetupUtil.configureSahha(activity, settings)
        val data = Sahha.di.configurationDao.getDeviceInformation()
        Assert.assertEquals(deviceInfo.appId, data?.appId)
    }

    @Ignore("Async")
    @Test
    fun authNoData_savesCurrentInfo() = runTest {
        db.configurationDao().clearDeviceInformation()
        SahhaSetupUtil.configureSahha(activity, settings)
        SahhaSetupUtil.authenticateSahha(appId, appSecret, externalId)
        val data = db.configurationDao().getDeviceInformation()
        Assert.assertEquals(deviceInfo, data)
    }

    @Test
    fun authSameData_savesNothing() = runTest {
        db.configurationDao().clearDeviceInformation()
        db.configurationDao().saveDeviceInformation(deviceInfo)
        SahhaSetupUtil.configureSahha(activity, settings)
        SahhaSetupUtil.authenticateSahha(appId, appSecret, externalId)
        val data = db.configurationDao().getDeviceInformation()
        Assert.assertEquals(deviceInfo, data)
    }

    @Test
    fun authDiffData_savesCurrentInfo() = runTest {
        db.configurationDao().clearDeviceInformation()
        val diffInfo = deviceInfo.copy(appId = "different.app.id")
        db.configurationDao().saveDeviceInformation(diffInfo)
        SahhaSetupUtil.configureSahha(activity, settings)
        SahhaSetupUtil.authenticateSahha(appId, appSecret, externalId)
        val data = db.configurationDao().getDeviceInformation()
        Assert.assertEquals(deviceInfo, data)
    }
}