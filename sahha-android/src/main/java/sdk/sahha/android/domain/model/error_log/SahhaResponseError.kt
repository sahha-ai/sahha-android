package sdk.sahha.android.domain.model.error_log

internal data class SahhaResponseError(
    val title: String,
    val statusCode: Int,
    val location: String,
    val errors: List<SahhaResponseErrorItem>
)
