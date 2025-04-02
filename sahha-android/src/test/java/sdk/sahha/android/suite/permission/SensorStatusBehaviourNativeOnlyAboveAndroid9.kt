package sdk.sahha.android.suite.permission

import io.mockk.mockk
import org.junit.Assert
import org.junit.Test
import sdk.sahha.android.domain.interaction.PermissionInteractionManager
import sdk.sahha.android.domain.internal_enum.toSahhaSensorStatus
import sdk.sahha.android.source.SahhaSensorStatus

class SensorStatusBehaviourNativeOnlyAboveAndroid9 {
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
            isBelowOrEqualToAndroid9 = false
        )

        Assert.assertEquals(
            SahhaSensorStatus.disabled,
            status.toSahhaSensorStatus()
        )
    }

    @Test
    fun nativeEnabled_hcUnavailable_statusEnabled() {
        val status = manager.processStatuses(
            SahhaSensorStatus.enabled,
            SahhaSensorStatus.unavailable,
            isBelowOrEqualToAndroid9 = false
        )

        Assert.assertEquals(
            SahhaSensorStatus.enabled,
            status.toSahhaSensorStatus()
        )
    }

    @Test
    fun nativePending_hcUnavailable_statusPending() {
        val status = manager.processStatuses(
            SahhaSensorStatus.pending,
            SahhaSensorStatus.unavailable,
            isBelowOrEqualToAndroid9 = false
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
            isBelowOrEqualToAndroid9 = false
        )

        Assert.assertEquals(
            SahhaSensorStatus.unavailable,
            status.toSahhaSensorStatus()
        )
    }
}