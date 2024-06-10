package sdk.sahha.android.domain.interaction

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sdk.sahha.android.common.Constants
import sdk.sahha.android.common.security.Decryptor
import sdk.sahha.android.data.local.dao.SecurityDao
import sdk.sahha.android.data.local.dao.SleepDao
import sdk.sahha.android.di.IoScope
import sdk.sahha.android.domain.model.security.EncryptUtility
import sdk.sahha.android.domain.repository.AuthRepo
import sdk.sahha.android.domain.repository.BatchedDataRepo
import sdk.sahha.android.domain.repository.HealthConnectRepo
import sdk.sahha.android.domain.repository.SensorRepo
import sdk.sahha.android.domain.use_case.SaveTokensUseCase
import sdk.sahha.android.domain.use_case.background.KillMainService
import sdk.sahha.android.domain.use_case.background.RestartWorkers
import sdk.sahha.android.source.Sahha
import javax.inject.Inject

private const val TAG = "AuthInteractionManager"

internal class AuthInteractionManager @Inject constructor(
    @IoScope private val ioScope: CoroutineScope,
    private val authRepo: AuthRepo,
    private val securityDao: SecurityDao,
    private val decryptor: Decryptor,
    private val healthConnectRepo: HealthConnectRepo,
    private val batchedDataRepo: BatchedDataRepo,
    private val sensorRepo: SensorRepo,
    private val sleepDao: SleepDao,
    private val saveTokensUseCase: SaveTokensUseCase,
    private val restartWorkers: RestartWorkers,
    private val killMainService: KillMainService
) {
    fun checkIsAuthenticated(): Boolean {
        val tokenIsNotNullOrEmpty = !authRepo.getToken().isNullOrEmpty()
        val refreshTokenIsNotNullOrEmpty = !authRepo.getRefreshToken().isNullOrEmpty()
        return tokenIsNotNullOrEmpty && refreshTokenIsNotNullOrEmpty
    }

    fun authenticate(
        appId: String,
        appSecret: String,
        externalId: String,
        callback: ((error: String?, success: Boolean) -> Unit)
    ) {
        ioScope.launch {
            saveTokensUseCase(appId, appSecret, externalId) { error, success ->
                error?.also { e -> Log.d(TAG, e) }
                if (success) launch { restartWorkers() }
                callback(error, success)
            }
        }
    }

    fun authenticate(
        profileToken: String,
        refreshToken: String,
        callback: ((error: String?, success: Boolean) -> Unit)
    ) {
        ioScope.launch {
            saveTokensUseCase(profileToken, refreshToken) { error, success ->
                error?.also { e -> Log.d(TAG, e) }
                if (success) launch { restartWorkers() }
                callback(error, success)
            }
        }
    }

    suspend fun deauthenticate(
        callback: (suspend (error: String?, success: Boolean) -> Unit)
    ) {
        authRepo.clearTokenData(callback)
        healthConnectRepo.clearAllQueries()
        healthConnectRepo.clearAllStepsHc()
        healthConnectRepo.clearAllChangeTokens()
        batchedDataRepo.deleteAllBatchedData()
        sleepDao.clearAllSleepHistory()
        sleepDao.clearSleep()
        sleepDao.clearSleepDto()
        sensorRepo.clearAllStepSessions()
        sensorRepo.stopAllWorkers()
        killMainService()
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