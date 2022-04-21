package sdk.sahha.android.source

import androidx.annotation.Keep

@Keep
data class SahhaDemographic(
    val age: Int?,
    val gender: String?,
    val country: String?,
    val birthCountry: String?
)