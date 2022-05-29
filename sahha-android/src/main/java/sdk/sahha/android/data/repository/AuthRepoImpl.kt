package sdk.sahha.android.data.repository

import sdk.sahha.android.common.SahhaErrorLogger
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.security.Encryptor
import sdk.sahha.android.data.Constants.UERT
import sdk.sahha.android.data.Constants.UET
import sdk.sahha.android.domain.repository.AuthRepo
import javax.inject.Inject

class AuthRepoImpl @Inject constructor(
    private val encryptor: Encryptor,
    private val sahhaErrorLogger: SahhaErrorLogger
) : AuthRepo {
    override suspend fun saveTokens(
        profileToken: String,
        refreshToken: String,
        callback: ((error: String?, success: Boolean) -> Unit)?
    ) {
        if (profileToken.isEmpty()) {
            callback?.also { it(SahhaErrors.emptyProfileToken, false) }
            return
        }
        if (refreshToken.isEmpty()) {
            callback?.also { it(SahhaErrors.emptyRefreshToken, false) }
            return
        }

        try {
            encryptor.encryptText(UET, profileToken)
            encryptor.encryptText(UERT, refreshToken)
            callback?.also { it(null, true) }
        } catch (e: Exception) {
            val nullErrorMsg = "Something went wrong storing tokens"

            callback?.also { it(e.message ?: nullErrorMsg, false) }
            sahhaErrorLogger.application(e.message ?: nullErrorMsg, "saveTokens", null)
        }
    }
}