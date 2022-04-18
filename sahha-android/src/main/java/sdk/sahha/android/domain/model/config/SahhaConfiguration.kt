package sdk.sahha.android.domain.model.config

import androidx.room.Entity
import androidx.room.PrimaryKey
import sdk.sahha.android.domain.model.enums.SahhaEnvironment
import sdk.sahha.android.domain.model.enums.SahhaFramework
import sdk.sahha.android.domain.model.enums.SahhaSensor

@Entity
data class SahhaConfiguration(
    @PrimaryKey val id: Int,
    val environment: Int,
    val framework: String,
    val sensorArray: ArrayList<Int>,
    val manuallyPostData: Boolean
) {
    constructor(
        environment: Int,
        framework: String,
        sensorArray: ArrayList<Int>,
        manuallyPostData: Boolean
    ) : this(
        1,
        environment,
        framework,
        sensorArray,
        manuallyPostData
    )

    fun toSahhaSettings(): SahhaSettings {
        return SahhaSettings(
            SahhaEnvironment.values()[environment],
            SahhaFramework.valueOf(framework),
            sensorArray.mapTo(mutableSetOf()) {
                SahhaSensor.values()[it]
            },
            manuallyPostData
        )
    }
}
