package sdk.sahha.android.domain.repository

import sdk.sahha.android.domain.model.device.PhoneUsage

internal interface DeviceUsageRepo {
    suspend fun getUsages(): List<PhoneUsage>
    suspend fun saveUsages(usages: List<PhoneUsage>)
    suspend fun clearUsages(usages: List<PhoneUsage>)
    suspend fun clearAllUsages()
}