//package sdk.sahha.android.suite.permission
//
//import org.junit.Assert
//import org.junit.Test
//import sdk.sahha.android.di.AppModule
//import sdk.sahha.android.domain.model.dto.PermissionStateDto
//import sdk.sahha.android.source.SahhaSensor
//import sdk.sahha.android.source.SahhaSensorStatus
//
//class PlatformPermissionProcessorTest {
//    private val processor = AppModule.mockPlatformPermissionProcessor
//
//    @Test
//    fun iosOnlyPermissionPending_isUnavailable() {
//        val processed = processor.processAndroidUnavailablePermissions(
//            listOf(
//                PermissionStateDto(
//                    type = SahhaSensor.move_time,
//                    status = SahhaSensorStatus.pending
//                )
//            )
//        )
//
//        Assert.assertEquals(SahhaSensorStatus.unavailable, processed.first().status)
//    }
//
//    @Test
//    fun iosOnlyPermissionEnabled_isUnavailable() {
//        val processed = processor.processAndroidUnavailablePermissions(
//            listOf(
//                PermissionStateDto(
//                    type = SahhaSensor.move_time,
//                    status = SahhaSensorStatus.enabled
//                )
//            )
//        )
//
//        Assert.assertEquals(SahhaSensorStatus.unavailable, processed.first().status)
//    }
//
//    @Test
//    fun iosOnlyPermissionDisabled_isUnavailable() {
//        val processed = processor.processAndroidUnavailablePermissions(
//            listOf(
//                PermissionStateDto(
//                    type = SahhaSensor.move_time,
//                    status = SahhaSensorStatus.disabled
//                )
//            )
//        )
//
//        Assert.assertEquals(SahhaSensorStatus.unavailable, processed.first().status)
//    }
//
//    @Test
//    fun iosOnlyPermissionUnavailable_isUnavailable() {
//        val processed = processor.processAndroidUnavailablePermissions(
//            listOf(
//                PermissionStateDto(
//                    type = SahhaSensor.move_time,
//                    status = SahhaSensorStatus.unavailable
//                )
//            )
//        )
//
//        Assert.assertEquals(SahhaSensorStatus.unavailable, processed.first().status)
//    }
//
//    @Test
//    fun androidPermission_isExpectedStatus() {
//        val processed = processor.processAndroidUnavailablePermissions(
//            listOf(
//                PermissionStateDto(
//                    type = SahhaSensor.sleep,
//                    status = SahhaSensorStatus.enabled
//                )
//            )
//        )
//
//        Assert.assertEquals(SahhaSensorStatus.enabled, processed.first().status)
//    }
//
//    @Test
//    fun allPermissionsEnabled_hasExpectedUnavailableStatus() {
//        val states = mutableListOf<PermissionStateDto>()
//        SahhaSensor.values().forEach { sensor ->
//            states.add(
//                PermissionStateDto(
//                    sensor, SahhaSensorStatus.enabled
//                )
//            )
//        }
//        val processed = processor.processAndroidUnavailablePermissions(states)
//
//        processed.forEach { state ->
//            if (processor.iosOnlyPermissions.contains(state.type))
//                Assert.assertEquals(SahhaSensorStatus.unavailable, state.status)
//            else Assert.assertEquals(SahhaSensorStatus.enabled, state.status)
//        }
//    }
//
//    @Test
//    fun allPermissionsDisabled_hasExpectedUnavailableStatus() {
//        val states = mutableListOf<PermissionStateDto>()
//        SahhaSensor.values().forEach { sensor ->
//            states.add(
//                PermissionStateDto(
//                    sensor, SahhaSensorStatus.disabled
//                )
//            )
//        }
//        val processed = processor.processAndroidUnavailablePermissions(states)
//
//        processed.forEach { state ->
//            if (processor.iosOnlyPermissions.contains(state.type))
//                Assert.assertEquals(SahhaSensorStatus.unavailable, state.status)
//            else Assert.assertEquals(SahhaSensorStatus.disabled, state.status)
//        }
//    }
//}