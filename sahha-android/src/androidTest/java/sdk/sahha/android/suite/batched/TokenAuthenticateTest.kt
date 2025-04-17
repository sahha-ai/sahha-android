package sdk.sahha.android.suite.batched

import androidx.activity.ComponentActivity
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.common.SahhaSetupUtil
import sdk.sahha.android.common.appId
import sdk.sahha.android.common.appSecret
import sdk.sahha.android.common.externalId
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaEnvironment
import sdk.sahha.android.source.SahhaScoreType
import sdk.sahha.android.source.SahhaSettings
import kotlin.coroutines.resume

class TokenAuthenticateTest {
    companion object {
        lateinit var activity: ComponentActivity

        @JvmStatic
        @BeforeClass
        fun beforeClass() = runTest {
            activity = ApplicationProvider.getApplicationContext()
            val settings = SahhaSettings(SahhaEnvironment.sandbox)
            SahhaSetupUtil.configureSahha(activity, settings)
//            SahhaSetupUtil.authenticateSahha(profileToken, refreshToken)
            SahhaSetupUtil.authenticateSahha(appId, appSecret, externalId)
        }
    }

    @Test
    fun auth_validTokens_analyzesSuccessfully() = runTest {
        SahhaReconfigure(activity)
        suspendCancellableCoroutine<Unit> { cont ->
            Sahha.getScores(setOf(SahhaScoreType.activity),) { error, analyzeSuccess ->
//                Assert.assertEquals("{}", analyzeSuccess)
                Assert.assertNotNull(analyzeSuccess)
                println(error)
                println(analyzeSuccess)
                if (cont.isActive) cont.resume(Unit)
            }
        }
    }
}