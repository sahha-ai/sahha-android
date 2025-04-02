package sdk.sahha.android.common

import android.content.Context
import kotlinx.coroutines.suspendCancellableCoroutine
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensor
import sdk.sahha.android.source.SahhaSettings
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import androidx.activity.ComponentActivity

private const val tag = "SahhaSetupUtil"

object SahhaSetupUtil {
    suspend fun configureSahha(activity: ComponentActivity, settings: SahhaSettings) =
        suspendCancellableCoroutine<Unit> { continuation ->
            try {
                Sahha.configure(activity, settings) { error, success ->
                    if (success) {
                        if (continuation.isActive) continuation.resume(Unit)
                    } else {
                        if (continuation.isActive) continuation.resumeWithException(Exception("Failed to configure: $error"))
                    }
                }
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }

    suspend fun authenticateSahha(
        appId: String,
        appSecret: String,
        externalId: String
    ): String? =
        suspendCancellableCoroutine<String?> { continuation ->
            try {
                Sahha.authenticate(appId, appSecret, externalId) { authError, authSuccess ->
                    continuation.resume(authError)
                }
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }

    suspend fun authenticateSahha(
        token: String,
        refresh: String,
    ) =
        suspendCancellableCoroutine<Unit> { continuation ->
            try {
                Sahha.authenticate(token, refresh) { authError, authSuccess ->
                    if (authSuccess) {
                        continuation.resume(Unit)
                    } else {
                        continuation.resumeWithException(Exception("Failed to auth: $authError"))
                    }
                }
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }

    suspend fun deauthenticateSahha() =
        suspendCancellableCoroutine<Unit> { continuation ->
            Sahha.deauthenticate { deauthError, deauthSuccess ->
                if (deauthSuccess) {
                    continuation.resume(Unit)
                } else {
                    continuation.resumeWithException(Exception("Failed to deauth: $deauthError"))
                }
            }
        }

    suspend fun enableSensors(context: Context, sensors: Set<SahhaSensor>) =
        suspendCoroutine { cont ->
            Sahha.enableSensors(context, sensors) { _, status ->
                cont.resume(status)
            }
        }
}