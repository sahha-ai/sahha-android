package sdk.sahha.android.source

import androidx.annotation.Keep

@Keep
enum class SahhaActivityStatus {
    pending,
    unavailable,
    disabled,
    enabled
}