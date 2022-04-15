package sdk.sahha.android.common

import sdk.sahha.android.domain.model.enums.SahhaSensor

object SahhaErrors {
    val typeAuthentication = "Authentication Error"
    val typeToken = "Token Error"
    val typeEncode = "Encode Error"
    val typeServer = "Server Error"
    val typeResponse = "Response Error"
    val typeDecode = "Decoding Error"
    val typeMissing = "Missing Data"

    fun sensorNotEnabled(sensor: Enum<SahhaSensor>): String {
        return "Error: The ${sensor.name.lowercase()} sensor is not enabled."
    }

    fun localDataIsEmpty(sensor: Enum<SahhaSensor>): String {
        return "Error: The local ${sensor.name.lowercase()} data is empty."
    }
}