package sdk.sahha.android.domain.model.dto

import androidx.annotation.Keep
import sdk.sahha.android.common.Constants

@Keep
data class BloodGlucoseDto(
    val recordingMethod: String,
    val value: Double,
    val source: String,
    val deviceType: String,
    val startDateTime: String,
    val endDateTime: String,
    val modifiedDateTime: String,
    val relationToMeal: String,
    val specimenSource: String,
    val mealType: String,
    val dataType: String = Constants.DataTypes.BLOOD_GLUCOSE,
    val unit: String = Constants.DataUnits.MMOL_PER_LITRE,
    val deviceManufacturer: String = Constants.UNKNOWN,
    val deviceModel: String = Constants.UNKNOWN
)
