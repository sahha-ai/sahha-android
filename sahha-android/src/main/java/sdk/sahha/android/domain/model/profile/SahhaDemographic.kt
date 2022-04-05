package sdk.sahha.android.domain.model.profile

import androidx.annotation.Keep

@Keep
data class SahhaDemographic(
    val age: Int?,
    val gender: String?,
    val country: String?,
    val birthCountry: String?
)