package sdk.sahha.android.common

import android.util.Log
import sdk.sahha.android.common.enums.HealthConnectSensor
import sdk.sahha.android.source.SahhaSensor

object SahhaErrors {
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
    const val noData = "Error: No data found"
    const val noSettings = "Error: No Sahha settings found"

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
        const val unknownError = "Error: Please ensure Health Connect is installed and permissions are granted"
        const val notInstalled = "Error: Health Connect must be installed"
        const val noPermissions = "Error: There are no granted permissions"
        const val permissionRequestDenied =
            "Error: Maximum requests reached. Please open HealthConnect manually to enable permissions"
        const val minimumPostInterval = "Error: A minimum interval of 6 hours is required"
        const val unavailable = "Error: HealthConnect is unavailable on this device"

        fun minimumPostInterval(sensor: String): String {
            return "${sensor.uppercase()} $minimumPostInterval"
        }

        fun noPermissions(sensor: String): String {
            return "${sensor.uppercase()} $noPermissions"
        }

        fun localDataIsEmpty(healthConnectSensor: Enum<HealthConnectSensor>): String {
            return "Error: The local ${healthConnectSensor.name.lowercase()} data is empty."
        }
    }
}