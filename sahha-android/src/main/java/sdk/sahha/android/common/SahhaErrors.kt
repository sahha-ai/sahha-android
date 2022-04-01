package sdk.sahha.android.common

import sdk.sahha.android.domain.model.enums.SahhaSensor

object SahhaErrors {
    fun sensorNotEnabled(sensor: Enum<SahhaSensor>): String {
        return "Error: The ${sensor.name.lowercase()} sensor is not enabled."
    }

    fun localDataIsEmpty(sensor: Enum<SahhaSensor>): String {
        return "Error: The local ${sensor.name.lowercase()} data is empty."
    }
}