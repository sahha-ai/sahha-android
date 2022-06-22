package sdk.sahha.android.domain.model.analyze

data class AnalyzeRequest(
    val startDateTime: String?,
    val endDateTime: String?,
    val includeSourceData: Boolean
)