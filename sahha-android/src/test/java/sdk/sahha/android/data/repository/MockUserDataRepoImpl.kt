package sdk.sahha.android.data.repository

import sdk.sahha.android.domain.repository.UserDataRepo
import sdk.sahha.android.source.SahhaDemographic

internal class MockUserDataRepoImpl: UserDataRepo {
    private var demographic = SahhaDemographic()

    override suspend fun getDemographic(callback: ((error: String?, demographic: SahhaDemographic?) -> Unit)?) {
        callback?.invoke(null, demographic)
    }

    override suspend fun postDemographic(
        sahhaDemographic: SahhaDemographic,
        callback: ((error: String?, successful: Boolean) -> Unit)?
    ) {
        demographic = sahhaDemographic
        callback?.invoke(null, true)
    }

    override suspend fun getScores(
        scoresString: List<String>,
        dates: Pair<String, String>?,
        callback: ((error: String?, successful: String?) -> Unit)?
    ) {
        callback?.invoke(null, "scores_response")
    }

    override suspend fun getBiomarkers(
        categoriesString: List<String>,
        typesString: List<String>,
        dates: Pair<String, String>?,
        callback: (error: String?, value: String?) -> Unit
    ) {
        TODO("Not yet implemented")
    }
}