package sdk.sahha.android.data.repository

import kotlinx.coroutines.CoroutineScope
import sdk.sahha.android.common.SahhaTimeManager
import sdk.sahha.android.common.security.Decryptor
import sdk.sahha.android.data.local.dao.SecurityDao
import sdk.sahha.android.data.local.dao.SleepDao
import sdk.sahha.android.data.remote.SahhaApi
import sdk.sahha.android.domain.repository.SleepWorkerRepo
import javax.inject.Inject
import javax.inject.Named

class SleepWorkerRepoImpl @Inject constructor(
    @Named("ioScope") private val ioScope: CoroutineScope,
    private val sleepDao: SleepDao,
    private val securityDao: SecurityDao,
    private val timeManager: SahhaTimeManager,
    private val decryptor: Decryptor,
    private val api: SahhaApi
) : SleepWorkerRepo {
    override suspend fun postSleepData(callback: (responseSuccessful: Boolean) -> Unit) {
        TODO("NYI")
    }
}