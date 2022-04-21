package sdk.sahha.android.common

import android.Manifest
import android.content.pm.PackageManager
import sdk.sahha.android.source.Sahha

object SahhaPermissions {
    fun activityRecognitionGranted(): Boolean {
        return Sahha.di.context.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
    }
}