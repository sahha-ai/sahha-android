package sdk.sahha.android.domain.internal_enum

import sdk.sahha.android.source.SahhaSensorStatus

internal enum class InternalSensorStatus {
    pending,
    unavailable,
    disabled,
    enabled,
    partial
}

internal fun Enum<InternalSensorStatus>.toSahhaSensorStatus(): Enum<SahhaSensorStatus> {
    return when (this) {
        InternalSensorStatus.pending -> SahhaSensorStatus.pending
        InternalSensorStatus.unavailable -> SahhaSensorStatus.unavailable
        InternalSensorStatus.disabled -> SahhaSensorStatus.disabled
        InternalSensorStatus.enabled -> SahhaSensorStatus.enabled
        InternalSensorStatus.partial -> SahhaSensorStatus.disabled
        else -> SahhaSensorStatus.unavailable
    }
}

