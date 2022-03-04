package sdk.sahha.android

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

object PermissionController {
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
                    )
                        .show()
                } else {
                    Toast.makeText(
                        activity,
                        "Disabled, please enable",
                        Toast.LENGTH_LONG
                    )
                        .show()

                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    val packageNameUri = Uri.fromParts("package", activity.packageName, null)
                    intent.data = packageNameUri

                    activity.startActivity(intent)
                }
            }
    }

    fun grantActivityRecognition() {
        permission.launch(android.Manifest.permission.ACTIVITY_RECOGNITION)
    }
}