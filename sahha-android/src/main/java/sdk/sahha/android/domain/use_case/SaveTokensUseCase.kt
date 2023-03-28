package sdk.sahha.android.domain.use_case

import android.util.Log
import retrofit2.Response
import sdk.sahha.android.common.ResponseCode
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.Session
import sdk.sahha.android.data.remote.dto.send.ExternalIdSendDto
import sdk.sahha.android.domain.model.auth.TokenData
import sdk.sahha.android.domain.repository.AuthRepo

private const val tag = "SaveTokensUseCase"

class SaveTokensUseCase(
    private val repository: AuthRepo
) {
    suspend operator fun invoke(
        appId: String,
        appSecret: String,
        externalId: String,
        callback: ((error: String?, success: Boolean) -> Unit)
    ) {
        Session.appId = appId
        Session.appSecret = appSecret
        Session.externalId = externalId

        val response = repository.getTokensByExternalId(appId, appSecret, ExternalIdSendDto(externalId))

        if (ResponseCode.isSuccessful(response.code())) {
            saveTokensIfAvailable(response, repository, callback)
            return
        }

        callback("${response.code()}: ${response.message()}", false)
    }

    private fun saveTokensIfAvailable(
        response: Response<TokenData>,
        repository: AuthRepo,
        callback: ((error: String?, success: Boolean) -> Unit)
    ) {
        val tokens = response.body()
        Log.d(tag, "Tokens: $tokens")
        when (tokens) {
            null -> callback(SahhaErrors.noToken, false)
            else -> repository.saveEncryptedTokens(
                tokens.profileToken,
                tokens.profileToken // TODO: Change back to refresh token once API is updated
            ) { error, success ->
                callback(error, success)
            }
        }
    }
}