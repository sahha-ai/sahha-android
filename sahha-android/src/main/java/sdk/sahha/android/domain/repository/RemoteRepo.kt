package sdk.sahha.android.domain.repository

import sdk.sahha.android.domain.model.steps.StepData
import sdk.sahha.android.source.SahhaDemographic
import sdk.sahha.android.source.SahhaSensor

interface RemoteRepo {
    suspend fun postRefreshToken(retryLogic: (suspend () -> Unit))
    suspend fun postSleepData(callback: ((error: String?, successful: Boolean) -> Unit)?)
    suspend fun postPhoneScreenLockData(callback: ((error: String?, successful: Boolean) -> Unit)?)
    suspend fun postStepData(
        stepData: List<StepData>,
        callback: ((error: String?, successful: Boolean) -> Unit)?
    )
    suspend fun postAllSensorData(
        sensors: Set<Enum<SahhaSensor>>?,
        callback: ((error: String?, successful: Boolean) -> Unit)
    )

    suspend fun getAnalysis(
        dates: Pair<String, String>? = null,
        includeSourceData: Boolean,
        callback: ((error: String?, successful: String?) -> Unit)?,
    )

    suspend fun getDemographic(callback: ((error: String?, demographic: SahhaDemographic?) -> Unit)?)
    suspend fun postDemographic(
        sahhaDemographic: SahhaDemographic,
        callback: ((error: String?, successful: Boolean) -> Unit)?
    )
}