package sdk.sahha.android.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi

object SahhaPermissions {
    @RequiresApi(Build.VERSION_CODES.Q)
    fun activityRecognitionGranted(context: Context): Boolean {
        return context.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
    }
}