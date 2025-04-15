package sdk.sahha.android.source

import androidx.annotation.Keep

@Keep
enum class SahhaEnvironment {
    sandbox,
    production,
    @Deprecated(
        message = "Internal use only"
    )
    development
}