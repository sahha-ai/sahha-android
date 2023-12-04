package sdk.sahha.android.source

import androidx.annotation.Keep

@Keep
enum class SahhaSensorStatus {
    pending,
    unavailable,
    disabled,
    requested,
    partiallyRequested
}