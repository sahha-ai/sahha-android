//package sdk.sahha.android.suite
//
//import io.mockk.mockk
//import org.junit.Assert
//import org.junit.BeforeClass
//import org.junit.Test
//import sdk.sahha.android.domain.interaction.PermissionInteractionManager
//import sdk.sahha.android.source.SahhaSensorStatus
//
//class AppUsageStatuses {
//    companion object {
//        private lateinit var pim: PermissionInteractionManager
//
//        @JvmStatic
//        @BeforeClass
//        fun before(): Unit {
//            pim = PermissionInteractionManager(
//                mockk(),
//                mockk(),
//                mockk(),
//                mockk(),
//                mockk(),
//                mockk(),
//                mockk(),
//            )
//        }
//    }
//
//    @Test
//    fun statusPending_andUsagePending_isPending() {
//        val status = pim.processFinalStatus(SahhaSensorStatus.pending, SahhaSensorStatus.pending)
//        Assert.assertEquals(SahhaSensorStatus.pending, status)
//    }
//
//    @Test
//    fun statusPending_andUsageDisabled_isPending() {
//        val status = pim.processFinalStatus(SahhaSensorStatus.pending, SahhaSensorStatus.disabled)
//        Assert.assertEquals(SahhaSensorStatus.pending, status)
//    }
//
//    @Test
//    fun statusPending_andUsageEnabled_isPending() {
//        val status = pim.processFinalStatus(SahhaSensorStatus.pending, SahhaSensorStatus.enabled)
//        Assert.assertEquals(SahhaSensorStatus.pending, status)
//    }
//
//    @Test
//    fun statusPending_andUsageUnavailable_isPending() {
//        val status = pim.processFinalStatus(SahhaSensorStatus.pending, SahhaSensorStatus.unavailable)
//        Assert.assertEquals(SahhaSensorStatus.pending, status)
//    }
//
//    @Test
//    fun statusEnabled_andUsagePending_isPending() {
//        val status = pim.processFinalStatus(SahhaSensorStatus.enabled, SahhaSensorStatus.pending)
//        Assert.assertEquals(SahhaSensorStatus.pending, status)
//    }
//
//    @Test
//    fun statusEnabled_andUsageEnabled_isEnabled() {
//        val status = pim.processFinalStatus(SahhaSensorStatus.enabled, SahhaSensorStatus.enabled)
//        Assert.assertEquals(SahhaSensorStatus.enabled, status)
//    }
//
//    @Test
//    fun statusEnabled_andUsageDisabled_isDisabled() {
//        val status = pim.processFinalStatus(SahhaSensorStatus.enabled, SahhaSensorStatus.disabled)
//        Assert.assertEquals(SahhaSensorStatus.disabled, status)
//    }
//
//    @Test
//    fun statusEnabled_andUsageUnavailable_isDisabled() {
//        val status = pim.processFinalStatus(SahhaSensorStatus.enabled, SahhaSensorStatus.unavailable)
//        Assert.assertEquals(SahhaSensorStatus.disabled, status)
//    }
//
//    @Test
//    fun statusDisabled_andUsagePending_isPending() {
//        val status = pim.processFinalStatus(SahhaSensorStatus.disabled, SahhaSensorStatus.pending)
//        Assert.assertEquals(SahhaSensorStatus.pending, status)
//    }
//
//    @Test
//    fun statusDisabled_andUsageEnabled_isDisabled() {
//        val status = pim.processFinalStatus(SahhaSensorStatus.disabled, SahhaSensorStatus.enabled)
//        Assert.assertEquals(SahhaSensorStatus.disabled, status)
//    }
//
//    @Test
//    fun statusDisabled_andUsageDisabled_isDisabled() {
//        val status = pim.processFinalStatus(SahhaSensorStatus.disabled, SahhaSensorStatus.disabled)
//        Assert.assertEquals(SahhaSensorStatus.disabled, status)
//    }
//
//    @Test
//    fun statusDisabled_andUsageUnavailable_isDisabled() {
//        val status = pim.processFinalStatus(SahhaSensorStatus.disabled, SahhaSensorStatus.unavailable)
//        Assert.assertEquals(SahhaSensorStatus.disabled, status)
//    }
//
//    @Test
//    fun statusUnavailable_andUsagePending_isPending() {
//        val status = pim.processFinalStatus(SahhaSensorStatus.unavailable, SahhaSensorStatus.pending)
//        Assert.assertEquals(SahhaSensorStatus.pending, status)
//    }
//
//    @Test
//    fun statusUnavailable_andUsageEnabled_isDisabled() {
//        val status = pim.processFinalStatus(SahhaSensorStatus.unavailable, SahhaSensorStatus.enabled)
//        Assert.assertEquals(SahhaSensorStatus.disabled, status)
//    }
//
//    @Test
//    fun statusUnavailable_andUsageDisabled_isDisabled() {
//        val status = pim.processFinalStatus(SahhaSensorStatus.unavailable, SahhaSensorStatus.disabled)
//        Assert.assertEquals(SahhaSensorStatus.disabled, status)
//    }
//
//    @Test
//    fun statusUnavailable_andUsageUnavailable_isUnavailable() {
//        val status = pim.processFinalStatus(SahhaSensorStatus.unavailable, SahhaSensorStatus.unavailable)
//        Assert.assertEquals(SahhaSensorStatus.unavailable, status)
//    }
//}