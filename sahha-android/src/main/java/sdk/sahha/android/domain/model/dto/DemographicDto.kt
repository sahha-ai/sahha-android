package sdk.sahha.android.domain.model.dto

import androidx.annotation.Keep
import androidx.room.Entity
import sdk.sahha.android.source.SahhaDemographic

@Keep
@Entity
data class DemographicDto(
    val age: Int?,
    val gender: String?,
    val country: String?,
    val birthCountry: String?,
    val ethnicity: String?,
    val occupation: String?,
    val industry: String?,
    val incomeRange: String?,
    val education: String?,
    val relationship: String?,
    val locale: String?,
    val livingArrangement: String?,
    val birthDate: String?
)

fun DemographicDto.toSahhaDemographic(): SahhaDemographic {
    return SahhaDemographic(
        age,
        gender,
        country,
        birthCountry,
        ethnicity,
        occupation,
        industry,
        incomeRange,
        education,
        relationship,
        locale,
        livingArrangement,
        birthDate,
    )
}
