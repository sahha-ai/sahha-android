package sdk.sahha.android.framework.activity

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import sdk.sahha.android.domain.model.categories.PermissionHandler
import sdk.sahha.android.source.SahhaSensorStatus
import sdk.sahha.android.ui.theme.SahhasdkemptyTheme
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.Q)
internal class SahhaPermissionActivity @Inject constructor(
    private val permissionHandler: PermissionHandler
) : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getPermissionLogic().launch(android.Manifest.permission.ACTIVITY_RECOGNITION)
    }

    private fun getPermissionLogic(): ActivityResultLauncher<String> {
        return registerForActivityResult(ActivityResultContracts.RequestPermission()) { enabled ->
            val status = convertToActivityStatus(enabled)
            permissionHandler.sensorStatus = status
            permissionHandler.activityCallback.statusCallback?.invoke(null, status)
            finish()
        }
    }

    private fun convertToActivityStatus(enabled: Boolean): Enum<SahhaSensorStatus> {
        if (enabled) return SahhaSensorStatus.enabled
        else return SahhaSensorStatus.disabled
    }
}