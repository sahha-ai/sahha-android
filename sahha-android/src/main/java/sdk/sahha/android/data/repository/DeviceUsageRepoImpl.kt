package sdk.sahha.android.data.repository

import sdk.sahha.android.data.local.dao.DeviceUsageDao
import sdk.sahha.android.domain.model.device.PhoneUsage
import sdk.sahha.android.domain.repository.DeviceUsageRepo
import javax.inject.Inject

internal class DeviceUsageRepoImpl @Inject constructor(
    private val dao: DeviceUsageDao
) : DeviceUsageRepo {
    override suspend fun getUsages(): List<PhoneUsage> {
        return dao.getUsages()
    }

    override suspend fun saveUsages(usages: List<PhoneUsage>) {
        dao.saveUsages(usages)
    }

    override suspend fun clearUsages(usages: List<PhoneUsage>) {
        dao.clearUsages(usages)
    }

    override suspend fun clearAllUsages() {
        dao.clearAllUsages()
    }
}