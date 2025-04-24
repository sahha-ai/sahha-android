package sdk.sahha.android.data.repository

import sdk.sahha.android.domain.model.device.PhoneUsage
import sdk.sahha.android.domain.repository.DeviceUsageRepo

internal class MockDeviceUsageRepoImpl: DeviceUsageRepo {
    private val usages = mutableListOf<PhoneUsage>()

    override suspend fun getUsages(): List<PhoneUsage> {
        return this.usages
    }

    override suspend fun saveUsages(usages: List<PhoneUsage>) {
        this.usages += usages
    }

    override suspend fun clearUsages(usages: List<PhoneUsage>) {
        this.usages.removeAll(usages)
    }

    override suspend fun clearAllUsages() {
        this.usages.clear()
    }
}