package sdk.sahha.android.common

import sdk.sahha.android.source.SahhaSensor

object SahhaErrors {
    val typeAuthentication = "Authentication Error"
    val typeToken = "Token Error"
    val typeEncode = "Encode Error"
    val typeServer = "Server Error"
    val typeResponse = "Response Error"
    val typeDecode = "Decoding Error"
    val typeMissing = "Missing Data"
    val activityNotPrepared = "Error: Activity must be prepared before calling activate"
    val attemptingTokenRefresh = "Error: Invalid token, attempting to refresh token..."

    fun sensorNotEnabled(sensor: Enum<SahhaSensor>): String {
        return "Error: The ${sensor.name.lowercase()} sensor is not enabled."
    }

    fun sensorEnablingNotRequired(sensor: Enum<SahhaSensor>): String {
        return "The ${sensor.name.lowercase()} sensor does not need to be enabled."
    }

    fun localDataIsEmpty(sensor: Enum<SahhaSensor>): String {
        return "Error: The local ${sensor.name.lowercase()} data is empty."
    }

    fun androidVersionTooLow(requiredVersion: Int): String {
        return "Error: Android $requiredVersion or above is required."
    }
}