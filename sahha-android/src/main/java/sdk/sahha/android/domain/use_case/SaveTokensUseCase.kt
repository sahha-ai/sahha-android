package sdk.sahha.android.domain.use_case

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import retrofit2.Response
import sdk.sahha.android.common.ResponseCode
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.common.Session
import sdk.sahha.android.di.IoScope
import sdk.sahha.android.domain.interaction.UserDataInteractionManager
import sdk.sahha.android.domain.model.auth.TokenData
import sdk.sahha.android.domain.model.dto.send.ExternalIdSendDto
import sdk.sahha.android.domain.repository.AuthRepo
import javax.inject.Inject

private const val tag = "SaveTokensUseCase"

internal class SaveTokensUseCase @Inject constructor(
    private val context: Context,
    @IoScope private val ioScope: CoroutineScope,
    private val repository: AuthRepo,
    private val userData: UserDataInteractionManager
) {
    suspend operator fun invoke(
        appId: String,
        appSecret: String,
        externalId: String,
        callback: ((error: String?, success: Boolean) -> Unit)
    ) {
        val lockSuccessful = Session.authMutex.tryLock()
        if (lockSuccessful) {
            try {
                val response =
                    repository.getTokensByExternalId(
                        appId,
                        appSecret,
                        ExternalIdSendDto(externalId)
                    )

                if (ResponseCode.isSuccessful(response.code())) {
                    saveTokensIfAvailable(response, callback)
                    return
                }

                callback("${response.code()}: ${response.message()}", false)
            } catch (e: Exception) {
                callback(e.message, false)
            } finally {
                Session.authMutex.unlock()
            }
        } else {
            callback("Error: Authentication already in progress", false)
        }
    }

    operator fun invoke(
        profileToken: String,
        refreshToken: String,
        callback: ((error: String?, success: Boolean) -> Unit)
    ) {
        ioScope.launch {
            Session.authMutex.withLock {
                repository.saveEncryptedTokens(
                    profileToken,
                    refreshToken,
                ) { error, success ->
                    if (success) {
                        callback(null, true)
                        ioScope.launch {
                            userData.processAndPutDeviceInfo(
                                context = context,
                                isAuthenticating = true,
                            )
                        }
                        return@saveEncryptedTokens
                    }
                    callback(error, false)
                }
            }
        }
    }

    private fun saveTokensIfAvailable(
        response: Response<TokenData>,
        callback: ((error: String?, success: Boolean) -> Unit)
    ) {
        val tokens = response.body()
        when (tokens) {
            null -> callback(SahhaErrors.noToken, false)
            else -> repository.saveEncryptedTokens(
                tokens.profileToken,
                tokens.refreshToken,
            ) { error, success ->
                if (success) {
                    callback(null, true)
                    ioScope.launch {
                        userData.processAndPutDeviceInfo(
                            context = context,
                            isAuthenticating = true,
                        )
                    }
                    return@saveEncryptedTokens
                }
                callback(error, false)
            }
        }
    }
}