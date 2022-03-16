package sdk.sahha.android.data.repository

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import sdk.sahha.android.domain.repository.PermissionsRepo
import javax.inject.Inject

class PermissionsRepoImpl @Inject constructor(
    private val activity: ComponentActivity
) : PermissionsRepo {
    private lateinit var permission: ActivityResultLauncher<String>

    override fun setPermissionLogic() {
        permission =
            activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
    }

    override fun setPermissionLogic(logic: ((enabled: Boolean) -> Unit)) {
        permission =
            activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { _enabled ->
                logic(_enabled)
            }
    }

    override fun openSettings() {
        val openSettingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val packageNameUri = Uri.fromParts("package", activity.packageName, null)
        openSettingsIntent.data = packageNameUri

        activity.startActivity(openSettingsIntent)
    }

    override fun grantActivityRecognition() {
        permission.launch(android.Manifest.permission.ACTIVITY_RECOGNITION)
    }
}