package sdk.sahha.android.data.manager

import kotlinx.coroutines.runBlocking
import sdk.sahha.android.data.local.dao.RationaleDao
import sdk.sahha.android.domain.internal_enum.RationaleSensorType
import sdk.sahha.android.domain.manager.RationaleManager
import sdk.sahha.android.domain.model.permissions.Rationale
import javax.inject.Inject

internal class RationaleManagerImpl @Inject constructor(
    private val rationaleDao: RationaleDao
): RationaleManager {
    override suspend fun saveRationale(rationale: Rationale) {
        rationaleDao.createRationale(rationale = rationale)
    }

    override suspend fun getRationale(type: Enum<RationaleSensorType>): Rationale {
        return rationaleDao.getRationale(type.ordinal) ?: Rationale(type.ordinal, 0)
    }

    override suspend fun removeRationales(rationales: List<Rationale>) {
        rationaleDao.removeRationale(rationales)
    }

    override suspend fun removeAllRationales() {
        rationaleDao.removeAllRationale()
    }

    override fun shouldShowRationale(type: Enum<RationaleSensorType>): Boolean {
        val rationale = runBlocking { getRationale(type) }
        return rationale.denialCount < 2
    }
}