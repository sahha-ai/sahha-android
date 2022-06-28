package sdk.sahha.android.domain.model.error_log

data class SahhaResponseErrorItem(
    val origin: String,
    val errors: List<String>
)
