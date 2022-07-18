package sdk.sahha.android.source

import androidx.annotation.Keep

@Keep
data class SahhaDemographic(
    val age: Int? = null,
    val gender: String? = null,
    val country: String? = null,
    val birthCountry: String? = null,
    val ethnicity: String? = null,
    val occupation: String? = null,
    val industry: String? = null,
    val incomeRange: String? = null,
    val education: String? = null,
    val relationship: String? = null,
    val locale: String? = null,
    val livingArrangement: String? = null,
    val birthDate: String? = null
)