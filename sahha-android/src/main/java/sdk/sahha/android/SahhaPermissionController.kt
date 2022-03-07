package sdk.sahha.android

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

object SahhaPermissionController {
    private lateinit var permission: ActivityResultLauncher<String>

    fun init(activity: AppCompatActivity) {
        permission =
            activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { enabled ->
                // Successful logic
                if (enabled) {
                    Toast.makeText(
                        activity,
                        "Enabled",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }
    }

    fun init(activity: ComponentActivity) {
        permission =
            activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { enabled ->
                // Successful logic
                if (enabled) {
                    Toast.makeText(
                        activity,
                        "Enabled",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        activity,
                        "Disabled",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    fun grantActivityRecognition() {
        permission.launch(android.Manifest.permission.ACTIVITY_RECOGNITION)
    }
}