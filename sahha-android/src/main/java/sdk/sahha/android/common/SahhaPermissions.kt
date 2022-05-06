package sdk.sahha.android.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import sdk.sahha.android.source.SahhaSensorStatus

object SahhaPermissions {
    fun activityRecognitionGranted(context: Context): Enum<SahhaSensorStatus> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return SahhaSensorStatus.unavailable
        }

        return when (context.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION)) {
            PackageManager.PERMISSION_GRANTED -> {
                SahhaSensorStatus.enabled
            }
            PackageManager.PERMISSION_DENIED -> {
                SahhaSensorStatus.disabled
            }
            else -> {
                SahhaSensorStatus.unavailable
            }
        }
    }
}