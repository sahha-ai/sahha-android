package sdk.sahha.android.common

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.PackageManagerCompat
import androidx.work.impl.utils.PackageManagerHelper
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