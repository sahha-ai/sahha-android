package sdk.sahha.android

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
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensorStatus
import sdk.sahha.android.ui.theme.SahhasdkemptyTheme

@RequiresApi(Build.VERSION_CODES.Q)
class SahhaPermissionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getPermissionLogic().launch(android.Manifest.permission.ACTIVITY_RECOGNITION)

        setContent {
            SahhasdkemptyTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background.copy(alpha = 0f),
                ) {}
            }
        }
    }

    private fun getPermissionLogic(): ActivityResultLauncher<String> {
        return registerForActivityResult(ActivityResultContracts.RequestPermission()) { enabled ->
            val status = convertToActivityStatus(enabled)
            Sahha.motion.sensorStatus = status
            Sahha.motion.activityCallback.requestPermission?.let { it(null, status) }
            finish()
        }
    }

    private fun convertToActivityStatus(enabled: Boolean): Enum<SahhaSensorStatus> {
        if (enabled) return SahhaSensorStatus.enabled
        else return SahhaSensorStatus.disabled
    }
}