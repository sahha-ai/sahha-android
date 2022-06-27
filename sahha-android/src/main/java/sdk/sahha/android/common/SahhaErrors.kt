package sdk.sahha.android.common

import sdk.sahha.android.source.SahhaSensor

object SahhaErrors {
    val typeAuthentication = "authentication"
    val typeRequest = "request"
    val typeEncode = "encoding"
    val typeDecode = "decoding"
    val typeResponse = "response"

    val typeToken = "Token Error"
    val typeServer = "Server Error"
    val typeMissing = "Missing Data"

    val attemptingTokenRefresh = "Error: Invalid token, attempting to refresh token..."
    val sensorInvalid = "Error: Sensor not valid"
    val datesInvalid = "Error: Both start and end dates must be entered or null"
    val emptyProfileToken = "Error: The profile token was null or empty"
    val emptyRefreshToken = "Error: The refresh token was null or empty"
    val somethingWentWrong = "Error: Something went wrong, please try again"
    val responseFailure = "Error: Failed to receive response"
    val noData = "Error: No data found"

    fun sensorNotEnabled(sensor: Enum<SahhaSensor>): String {
        return "Error: The ${sensor.name.lowercase()} sensor is not enabled."
    }

    fun localDataIsEmpty(sensor: Enum<SahhaSensor>): String {
        return "Error: The local ${sensor.name.lowercase()} data is empty."
    }

    fun androidVersionTooLow(requiredVersion: Int): String {
        return "Error: Android $requiredVersion or above is required."
    }
}