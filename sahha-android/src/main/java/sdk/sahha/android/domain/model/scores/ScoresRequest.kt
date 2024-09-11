package sdk.sahha.android.domain.model.scores

import androidx.annotation.Keep

@Keep
internal data class ScoresRequest(
    val startDateTime: String?,
    val endDateTime: String?,
)