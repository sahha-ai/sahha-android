package sdk.sahha.android.suite.permission

import io.mockk.mockk
import org.junit.Assert
import org.junit.Test
import sdk.sahha.android.domain.interaction.PermissionInteractionManager
import sdk.sahha.android.domain.internal_enum.toSahhaSensorStatus
import sdk.sahha.android.source.SahhaSensorStatus

class SensorStatusBehaviourWithHealthConnectBelowOrAndroid9 {
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
    fun nativeDisabled_hcDisabled_statusDisabled() {
        val status = manager.processStatuses(
            SahhaSensorStatus.disabled,
            SahhaSensorStatus.disabled,
            isBelowOrEqualToAndroid9 = true
        )

        Assert.assertEquals(
            SahhaSensorStatus.disabled,
            status.toSahhaSensorStatus()
        )
    }

    // This should be an impossible state
    // HealthConnect permission can only be given if native permission
    // was enabled first
    @Test
    fun nativeDisabled_hcEnabled_statusDisabled() {
        val status = manager.processStatuses(
            SahhaSensorStatus.disabled,
            SahhaSensorStatus.enabled,
            isBelowOrEqualToAndroid9 = true
        )

        Assert.assertEquals(
            SahhaSensorStatus.disabled,
            status.toSahhaSensorStatus()
        )
    }

    // Impossible state
    @Test
    fun nativeDisabled_hcPending_statusDisabled() {
        val status = manager.processStatuses(
            SahhaSensorStatus.disabled,
            SahhaSensorStatus.pending,
            isBelowOrEqualToAndroid9 = true
        )

        Assert.assertEquals(
            SahhaSensorStatus.disabled,
            status.toSahhaSensorStatus()
        )
    }

    // Impossible state
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

    @Test
    fun nativeEnabled_hcDisabled_statusDisabled() {
        val status = manager.processStatuses(
            SahhaSensorStatus.enabled,
            SahhaSensorStatus.disabled,
            isBelowOrEqualToAndroid9 = true
        )

        Assert.assertEquals(
            SahhaSensorStatus.disabled,
            status.toSahhaSensorStatus()
        )
    }

    @Test
    fun nativeEnabled_hcEnabled_statusEnabled() {
        val status = manager.processStatuses(
            SahhaSensorStatus.enabled,
            SahhaSensorStatus.enabled,
            isBelowOrEqualToAndroid9 = true
        )

        Assert.assertEquals(
            SahhaSensorStatus.enabled,
            status.toSahhaSensorStatus()
        )
    }

    @Test
    fun nativeEnabled_hcPending_statusPending() {
        val status = manager.processStatuses(
            SahhaSensorStatus.enabled,
            SahhaSensorStatus.pending,
            isBelowOrEqualToAndroid9 = true
        )

        Assert.assertEquals(
            SahhaSensorStatus.pending,
            status.toSahhaSensorStatus()
        )
    }

    // Impossible case - Android 9 or below does not have native permissions
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

    // Impossible state
    @Test
    fun nativePending_hcDisabled_statusPending() {
        val status = manager.processStatuses(
            SahhaSensorStatus.pending,
            SahhaSensorStatus.disabled,
            isBelowOrEqualToAndroid9 = true
        )

        Assert.assertEquals(
            SahhaSensorStatus.pending,
            status.toSahhaSensorStatus()
        )
    }

    // Impossible state
    @Test
    fun nativePending_hcEnabled_statusPending() {
        val status = manager.processStatuses(
            SahhaSensorStatus.pending,
            SahhaSensorStatus.enabled,
            isBelowOrEqualToAndroid9 = true
        )

        Assert.assertEquals(
            SahhaSensorStatus.pending,
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
    fun nativePending_hcPending_statusPending() {
        val status = manager.processStatuses(
            SahhaSensorStatus.pending,
            SahhaSensorStatus.pending,
            isBelowOrEqualToAndroid9 = true
        )

        Assert.assertEquals(
            SahhaSensorStatus.pending,
            status.toSahhaSensorStatus()
        )
    }

    // Impossible state
    @Test
    fun nativeUnavailable_hcDisabled_statusUnavailable() {
        val status = manager.processStatuses(
            SahhaSensorStatus.unavailable,
            SahhaSensorStatus.disabled,
            isBelowOrEqualToAndroid9 = true
        )

        Assert.assertEquals(
            SahhaSensorStatus.unavailable,
            status.toSahhaSensorStatus()
        )
    }

    // Impossible state
    @Test
    fun nativeUnavailable_hcEnabled_statusUnavailable() {
        val status = manager.processStatuses(
            SahhaSensorStatus.unavailable,
            SahhaSensorStatus.enabled,
            isBelowOrEqualToAndroid9 = true
        )

        Assert.assertEquals(
            SahhaSensorStatus.unavailable,
            status.toSahhaSensorStatus()
        )
    }

    // Impossible state
    @Test
    fun nativeUnavailable_hcPending_statusUnavailable() {
        val status = manager.processStatuses(
            SahhaSensorStatus.unavailable,
            SahhaSensorStatus.pending,
            isBelowOrEqualToAndroid9 = true
        )

        Assert.assertEquals(
            SahhaSensorStatus.unavailable,
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