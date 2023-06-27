package sdk.sahha.android.domain.repository

import androidx.health.connect.client.records.StepsRecord
import sdk.sahha.android.domain.internal_enum.CompatibleApps

interface HealthConnectRepo {
    val permissions: Set<String>

    fun getHealthConnectCompatibleApps(): Set<CompatibleApps>
    fun startPostWorker()
    fun getStepData(): List<StepsRecord>?
}