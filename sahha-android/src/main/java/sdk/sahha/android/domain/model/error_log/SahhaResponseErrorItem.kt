package sdk.sahha.android.domain.model.error_log

data class SahhaResponseErrorItem(
    val property: String,
    val errorMessages: List<String>
)
