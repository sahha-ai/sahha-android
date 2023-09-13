package sdk.sahha.android.common

import android.util.Log
import sdk.sahha.android.source.SahhaSensor

internal object SahhaErrors {
    const val typeAuthentication = "authentication"
    const val typeRequest = "request"
    const val typeEncode = "encoding"
    const val typeDecode = "decoding"
    const val typeResponse = "response"

    const val attemptingTokenRefresh = "Error: Invalid token, attempting to refresh token..."
    const val sensorInvalid = "Error: Sensor not valid"
    const val datesInvalid = "Error: Both start and end dates must be entered or null"
    const val emptyProfileToken = "Error: The profile token was null or empty"
    const val emptyRefreshToken = "Error: The refresh token was null or empty"
    const val somethingWentWrong = "Error: Something went wrong, please try again"
    const val responseFailure = "Error: Failed to receive response"
    const val noCodeBody = "Error: No code body found"
    const val noSettings = "Error: No Sahha settings found"
    const val noProfileOrExternalId = "Error: A profile or external ID must be provided"
    const val noProfileId = "Error: Could not retrieve profile ID"
    const val noToken = "Error: No token found"
    const val noDemographics = "Error: Demographic data was null"
    const val postingInProgress = "Error: There is already a post in progress, please try again shortly"
    const val failedToPostAllData = "Error: Failed to post all data, please try again"

    fun sensorNotEnabled(sensor: Enum<SahhaSensor>): String {
        return "Error: The ${sensor.name.lowercase()} sensor is not enabled."
    }

    fun localDataIsEmpty(sensor: Enum<SahhaSensor>): String {
        return "Error: The local ${sensor.name.lowercase()} data is empty."
    }

    fun androidVersionTooLow(requiredVersion: Int): String {
        return "Error: Android $requiredVersion or above is required."
    }

    fun wrapFunctionTryCatch(
        tag: String,
        defaultErrorMsg: String? = "Something went wrong",
        function: (() -> Unit)
    ) {
        try {
            function()
        } catch (e: Exception) {
            Log.w(tag, e.message ?: defaultErrorMsg, e)
        }
    }

    fun wrapMultipleFunctionTryCatch(
        tag: String,
        defaultErrorMsg: String? = "Something went wrong",
        functionList: List<(() -> Unit)>
    ) {
        functionList.forEach {
            try {
                it()
            } catch (e: Exception) {
                Log.w(tag, e.message ?: defaultErrorMsg, e)
            }
        }
    }
}