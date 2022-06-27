package sdk.sahha.android.domain.model.error_log

data class SahhaResponseError(
    val title: String,
    val status: Int,
    val location: String,
    val errors: List<SahhaResponseErrorItem>
)
