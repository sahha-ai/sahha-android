package sdk.sahha.android.data.repository

import android.content.Context
import kotlinx.coroutines.CoroutineScope
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
    private val encryptor: Encryptor
) : AuthRepo {
    override suspend fun saveTokens(
        token: String,
        refreshToken: String
    ) {
        encryptor.encryptText(UET, token)
        encryptor.encryptText(UERT, refreshToken)
    }
}