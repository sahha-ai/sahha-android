package sdk.sahha.android.data.repository

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import sdk.sahha.android.common.AppCenterLog
import sdk.sahha.android.common.security.Encryptor
import sdk.sahha.android.data.Constants.UERT
import sdk.sahha.android.data.Constants.UET
import sdk.sahha.android.data.remote.SahhaApi
import sdk.sahha.android.domain.repository.AuthRepo
import javax.inject.Inject
import javax.inject.Named

class AuthRepoImpl @Inject constructor(
    private val context: Context,
    private val api: SahhaApi,
    @Named("ioScope") private val ioScope: CoroutineScope,
    @Named("mainScope") private val mainScope: CoroutineScope,
    private val encryptor: Encryptor,
    private val appCenterLog: AppCenterLog
) : AuthRepo {
    override suspend fun saveTokens(
        profileToken: String,
        refreshToken: String,
        callback: ((error: String?, success: Boolean) -> Unit)?
    ) {
        try {
            encryptor.encryptText(UET, profileToken)
            encryptor.encryptText(UERT, refreshToken)
            callback?.also { it(null, true) }
        } catch (e: Exception) {
            val nullErrorMsg = "Something went wrong storing tokens"

            callback?.also { it(e.message ?: nullErrorMsg, false) }
            appCenterLog.application(e.message ?: nullErrorMsg)
        }
    }
}