package sdk.sahha.android.suite.permission

import io.mockk.mockk
import org.junit.Assert
import org.junit.Test
import sdk.sahha.android.domain.interaction.PermissionInteractionManager
import sdk.sahha.android.domain.internal_enum.toSahhaSensorStatus
import sdk.sahha.android.source.SahhaSensorStatus

class SensorStatusBehaviourNativeOnlyBelowOrAndroid9 {
    private val manager = PermissionInteractionManager(
        mockk(),
        mockk(),
        mockk(),
        mockk(),
        mockk(),
        mockk(),
        mockk(),
    )

    @Test
    fun nativeDisabled_hcUnavailable_statusDisabled() {
        val status = manager.processStatuses(
            SahhaSensorStatus.disabled,
            SahhaSensorStatus.unavailable,
            isBelowOrEqualToAndroid9 = true
        )

        Assert.assertEquals(
            SahhaSensorStatus.disabled,
            status.toSahhaSensorStatus()
        )
    }

    // Impossible case - Cannot be enabled if Android 9 or below
    @Test
    fun nativeEnabled_hcUnavailable_statusUnavailable() {
        val status = manager.processStatuses(
            SahhaSensorStatus.enabled,
            SahhaSensorStatus.unavailable,
            isBelowOrEqualToAndroid9 = true
        )

        Assert.assertEquals(
            SahhaSensorStatus.unavailable,
            status.toSahhaSensorStatus()
        )
    }

    @Test
    fun nativePending_hcUnavailable_statusPending() {
        val status = manager.processStatuses(
            SahhaSensorStatus.pending,
            SahhaSensorStatus.unavailable,
            isBelowOrEqualToAndroid9 = true
        )

        Assert.assertEquals(
            SahhaSensorStatus.pending,
            status.toSahhaSensorStatus()
        )
    }

    @Test
    fun nativeUnavailable_hcUnavailable_statusUnavailable() {
        val status = manager.processStatuses(
            SahhaSensorStatus.unavailable,
            SahhaSensorStatus.unavailable,
            isBelowOrEqualToAndroid9 = true
        )

        Assert.assertEquals(
            SahhaSensorStatus.unavailable,
            status.toSahhaSensorStatus()
        )
    }
}