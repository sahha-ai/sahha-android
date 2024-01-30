package sdk.sahha.android.domain.model.analyze

import androidx.annotation.Keep

@Keep
internal data class AnalyzeRequest(
    val startDateTime: String?,
    val endDateTime: String?,
)