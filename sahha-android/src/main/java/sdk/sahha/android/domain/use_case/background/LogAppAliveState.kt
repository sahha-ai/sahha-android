package sdk.sahha.android.domain.use_case.background

import sdk.sahha.android.domain.model.health_connect.HealthConnectQuery
import sdk.sahha.android.domain.repository.BatchedDataRepo
import sdk.sahha.android.domain.repository.HealthConnectRepo
import javax.inject.Inject

internal class LogAppAliveState @Inject constructor(
    private val logAppEvent: LogAppEvent,
    private val queryTime: HealthConnectRepo
) {
    suspend operator fun invoke() {
        
    }
}