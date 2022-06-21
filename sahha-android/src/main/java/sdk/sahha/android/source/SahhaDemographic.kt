package sdk.sahha.android.source

import androidx.annotation.Keep

@Keep
data class SahhaDemographic(
    val age: Int?,
    val gender: String?,
    val country: String?,
    val birthCountry: String?,
    var ethnicity: String?,
    var occupation: String?,
    var industry: String?,
    var incomeRange: String?,
    var education: String?,
    var relationship: String?,
    var locale: String?,
    var livingArrangement: String?,
    var birthDate: String?
)