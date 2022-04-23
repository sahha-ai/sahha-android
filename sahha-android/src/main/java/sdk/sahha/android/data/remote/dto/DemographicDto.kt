package sdk.sahha.android.data.remote.dto

import androidx.annotation.Keep
import androidx.room.Entity
import com.google.android.gms.common.annotation.KeepName
import sdk.sahha.android.source.SahhaDemographic

@Keep
@Entity
data class DemographicDto(
    val occupation: String?,
    val age: Int?,
    val gender: String?,
    val country: String?,
    val birthCountry: String?,
    val ethnicity: String?,
    val industry: String?,
    val incomeRange: String?,
    val education: String?,
    val relationship: String?,
    val locale: String?,
    val livingArrangement: String?
)

fun DemographicDto.toSahhaDemographic(): SahhaDemographic {
    return SahhaDemographic(
        age,
        gender,
        country,
        birthCountry
    )
}
