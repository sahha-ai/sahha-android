package sdk.sahha.android.domain.model.config

import androidx.annotation.Keep
import sdk.sahha.android.domain.model.enums.SahhaEnvironment
import sdk.sahha.android.domain.model.enums.SahhaSensor

@Keep
class SahhaSettings(
    val environment: Enum<SahhaEnvironment>,
    val sensors: Set<Enum<SahhaSensor>> = mutableSetOf<Enum<SahhaSensor>>().let {
        for (sensor in SahhaSensor.values()) {
            it.add(sensor)
        }
        return@let it
    },
    val manuallyPostData: Boolean = false
)