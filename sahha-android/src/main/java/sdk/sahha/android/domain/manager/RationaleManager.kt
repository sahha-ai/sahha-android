package sdk.sahha.android.domain.manager

import sdk.sahha.android.domain.internal_enum.RationaleSensorType
import sdk.sahha.android.domain.model.permissions.Rationale

internal interface RationaleManager {
    fun shouldShowRationale(type: Enum<RationaleSensorType>): Boolean
    suspend fun saveRationale(rationale: Rationale)
    suspend fun getRationale(type: Enum<RationaleSensorType>): Rationale
    suspend fun removeRationales(rationales: List<Rationale>)
    suspend fun removeAllRationales()
}