package sdk.sahha.android.domain.repository

import sdk.sahha.android.source.SahhaDemographic

internal interface UserDataRepo {
    suspend fun getDemographic(callback: ((error: String?, demographic: SahhaDemographic?) -> Unit)?)
    suspend fun postDemographic(
        sahhaDemographic: SahhaDemographic,
        callback: ((error: String?, successful: Boolean) -> Unit)?
    )

    suspend fun getScores(
        dates: Pair<String, String>? = null,
        callback: ((error: String?, successful: String?) -> Unit)?
    )
}