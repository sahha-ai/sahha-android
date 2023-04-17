package sdk.sahha.android.interaction

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sdk.sahha.android.common.security.Decryptor
import sdk.sahha.android.data.Constants
import sdk.sahha.android.data.local.dao.SecurityDao
import sdk.sahha.android.di.IoScope
import sdk.sahha.android.domain.model.security.EncryptUtility
import sdk.sahha.android.domain.repository.AuthRepo
import sdk.sahha.android.domain.use_case.SaveTokensUseCase
import javax.inject.Inject

class AuthInteractionManager @Inject constructor(
    @IoScope private val ioScope: CoroutineScope,
    private val authRepo: AuthRepo,
    private val securityDao: SecurityDao,
    private val decryptor: Decryptor,
    private val saveTokensUseCase: SaveTokensUseCase
) {
    fun authenticate(
        appId: String,
        appSecret: String,
        externalId: String,
        callback: ((error: String?, success: Boolean) -> Unit)
    ) {
        ioScope.launch {
            saveTokensUseCase(appId, appSecret, externalId, callback)
        }
    }

    internal suspend fun migrateDataIfNeeded(callback: (error: String?, success: Boolean) -> Unit) {
        val oldData = getOldDataFromEncryptUtilityTable()

        if (oldData.isEmpty()) {
            callback(null, true)
            return
        }

        val oldToken: String? = decryptOldData(Constants.UET)
        val oldRefreshToken: String? = decryptOldData(Constants.UERT)

        val bothTokensAreNull = setOf(oldToken, oldRefreshToken).all { it == null }
        if (bothTokensAreNull) {
            callback(null, true)
            return
        }

        saveDataToEncryptedSharedPreferences(
            setOf(
                oldToken!!,
                oldRefreshToken!!
            )
        ) { error, success ->
            if (success) {
                ioScope.launch {
                    deleteOldDataFromEncryptUtilityTable()
                    callback(null, true)
                }
            } else {
                callback(error, false)
            }
        }
    }

    private suspend fun getOldDataFromEncryptUtilityTable(): Set<EncryptUtility> {
        return setOf(
            securityDao.getEncryptUtility(Constants.UET),
            securityDao.getEncryptUtility(Constants.UERT),
        )
    }

    private suspend fun decryptOldData(alias: String): String? {
        val data = securityDao.getEncryptUtility(alias)
        return when (data) {
            null -> null
            else -> decryptor.decrypt(alias)
        }
    }

    private fun saveDataToEncryptedSharedPreferences(
        decryptedData: Set<String>,
        callback: (error: String?, success: Boolean) -> Unit
    ) {
        authRepo.saveEncryptedTokens(
            decryptedData.elementAt(0), decryptedData.elementAt(1)
        ) { error, successful ->
            callback(error, successful)
        }
    }

    private suspend fun deleteOldDataFromEncryptUtilityTable() {
        securityDao.deleteAllEncryptedData()
    }
}