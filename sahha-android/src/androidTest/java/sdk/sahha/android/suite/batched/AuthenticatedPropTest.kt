package sdk.sahha.android.suite.batched

import androidx.activity.ComponentActivity
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.AfterClass
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import sdk.sahha.android.common.SahhaSetupUtil
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaEnvironment
import sdk.sahha.android.source.SahhaSettings
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AuthenticatedPropTest {
    companion object {
        private lateinit var activity: ComponentActivity

        @JvmStatic
        @BeforeClass
        fun beforeClass() = runTest {
            activity = ApplicationProvider.getApplicationContext()
            val settings = SahhaSettings(environment = SahhaEnvironment.sandbox)
            SahhaSetupUtil.configureSahha(activity, settings)
        }
    }

    @Test
    fun noAuthData_authIsFalse() = runTest {
        suspendCoroutine { cont ->
            Sahha.deauthenticate { err, success ->
                Assert.assertEquals(false, Sahha.isAuthenticated)
                cont.resume(Unit)
            }
        }
    }

    @Test
    fun noAuthData_reversed_authIsTrue() = runTest {
        suspendCoroutine { cont ->
            Sahha.deauthenticate { err, success ->
                Assert.assertEquals(true, !Sahha.isAuthenticated)
                cont.resume(Unit)
            }
        }
    }

    @Test
    fun onlyToken_authIsFalse() = runTest {
        suspendCoroutine { cont ->
            Sahha.deauthenticate { err, success ->
                Sahha.di.authRepo.saveEncryptedTokens(
                    "example_token",
                    ""
                ) { saveErr, saveSuccess ->
                    Assert.assertEquals(false, Sahha.isAuthenticated)
                    cont.resume(Unit)
                }
            }
        }
    }

    @Test
    fun onlyToken_reversed_authIsTrue() = runTest {
        suspendCoroutine { cont ->
            Sahha.deauthenticate { err, success ->
                Sahha.di.authRepo.saveEncryptedTokens(
                    "example_token",
                    ""
                ) { saveErr, saveSuccess ->
                    Assert.assertEquals(true, !Sahha.isAuthenticated)
                    cont.resume(Unit)
                }
            }
        }
    }

    @Test
    fun onlyRefreshToken_authIsFalse() = runTest {
        suspendCoroutine { cont ->
            Sahha.deauthenticate { err, success ->
                Sahha.di.authRepo.saveEncryptedTokens(
                    "",
                    "example_refresh"
                ) { saveErr, saveSuccess ->
                    Assert.assertEquals(false, Sahha.isAuthenticated)
                    cont.resume(Unit)
                }
            }
        }
    }

    @Test
    fun onlyRefreshToken_reversed_authIsTrue() = runTest {
        suspendCoroutine { cont ->
            Sahha.deauthenticate { err, success ->
                Sahha.di.authRepo.saveEncryptedTokens(
                    "",
                    "example_refresh"
                ) { saveErr, saveSuccess ->
                    Assert.assertEquals(true, !Sahha.isAuthenticated)
                    cont.resume(Unit)
                }
            }
        }
    }

    @Test
    fun tokenAndRefreshToken_authIsTrue() = runTest {
        suspendCoroutine { cont ->
            Sahha.deauthenticate { err, success ->
                Sahha.di.authRepo.saveEncryptedTokens(
                    "example_token",
                    "example_refresh"
                ) { saveErr, saveSuccess ->
                    Assert.assertEquals(true, Sahha.isAuthenticated)
                    cont.resume(Unit)
                }
            }
        }
    }

    @Test
    fun tokenAndRefreshToken_reversed_authIsFalse() = runTest {
        suspendCoroutine { cont ->
            Sahha.deauthenticate { err, success ->
                Sahha.di.authRepo.saveEncryptedTokens(
                    "example_token",
                    "example_refresh"
                ) { saveErr, saveSuccess ->
                    Assert.assertEquals(false, !Sahha.isAuthenticated)
                    cont.resume(Unit)
                }
            }
        }
    }
}