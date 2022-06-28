package sdk.sahha.android.domain.model.error_log

data class SahhaResponseError(
    val title: String,
    val statusCode: Int,
    val location: String,
    val errors: List<SahhaResponseErrorItem>
)
