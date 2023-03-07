package sdk.sahha.android.common

import android.util.Log
import sdk.sahha.android.common.enums.HealthConnectSensor
import sdk.sahha.android.source.SahhaSensor

object SahhaErrors {
    val typeAuthentication = "authentication"
    val typeRequest = "request"
    val typeEncode = "encoding"
    val typeDecode = "decoding"
    val typeResponse = "response"

    val attemptingTokenRefresh = "Error: Invalid token, attempting to refresh token..."
    val sensorInvalid = "Error: Sensor not valid"
    val datesInvalid = "Error: Both start and end dates must be entered or null"
    val emptyProfileToken = "Error: The profile token was null or empty"
    val emptyRefreshToken = "Error: The refresh token was null or empty"
    val somethingWentWrong = "Error: Something went wrong, please try again"
    val responseFailure = "Error: Failed to receive response"
    val noData = "Error: No data found"
    val noSettings = "Error: No Sahha settings found"

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

    object healthConnect {
        val unknownError = "Error: Please ensure Health Connect is installed and permissions are granted"
        val notInstalled = "Error: Health Connect must be installed"
        val noPermissions = "Error: There are no granted permissions"
        val permissionRequestDenied =
            "Error: Maximum requests reached. Please open HealthConnect manually to enable permissions"

        fun localDataIsEmpty(healthConnectSensor: HealthConnectSensor): String {
            return "Error: The local ${healthConnectSensor.name.lowercase()} data is empty."
        }
    }
}