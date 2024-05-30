package sdk.sahha.android.common

import android.content.Context
import android.icu.text.DateFormat
import com.google.gson.GsonBuilder
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import sdk.sahha.android.BuildConfig
import sdk.sahha.android.source.SahhaEnvironment
import sdk.sahha.android.source.SahhaSensor
import sdk.sahha.android.source.SahhaSettings
import java.time.Instant
import java.time.ZoneOffset

internal object Session {
    var hcQueryInProgress = false
    var tokenRefreshAttempted = false

    internal var healthConnectPostCallback: ((error: String?, successful: Boolean) -> Unit)? = null
    internal var settings: SahhaSettings? = null
    internal var sensors: Set<SahhaSensor>? = null
    internal val onlyDeviceSensorProvided by lazy {
        sensors?.contains(SahhaSensor.device_lock) ?: false
                && sensors?.count() == 1
    }

    internal var healthConnectServiceLaunched = false


    internal fun shouldBeDevEnvironment(
        context: Context,
        environment: Enum<SahhaEnvironment>
    ): Boolean {
        return BuildConfig.DEBUG &&
                environment == SahhaEnvironment.sandbox &&
                packageNameContains(
                    context, "sahha"
                )
    }

    private fun packageNameContains(context: Context, text: String): Boolean {
        val packageName = context.packageName
        return packageName.contains(text)
    }

    internal fun <T> logJsonString(
        headerText: String,
        data: T?
    ): String {
        val json =
            GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(
                    Instant::class.java,
                    JsonSerializer<Instant> { src, _, _ ->
                        JsonPrimitive(src.toString())
                    }
                )
                .registerTypeAdapter(
                    ZoneOffset::class.java,
                    JsonSerializer<ZoneOffset> { src, _, _ ->
                        JsonPrimitive(src.toString())
                    }
                )
                .setDateFormat(DateFormat.TIMEZONE_ISO_FIELD)
                .create()
                .toJson(data)
        println("*******************************")
        println(headerText)
        println("*******************************")
        println(json)
        return json
    }
}
