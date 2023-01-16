package sdk.sahha.android

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.Permission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.di.AppModule
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.ui.theme.SahhasdkemptyTheme

private val tag = "SahhaHealthConnectActivity"

@RequiresApi(Build.VERSION_CODES.Q)
class SahhaHealthConnectActivity : ComponentActivity() {
    private var isInitialResume = true

    private val healthConnectPermissions =
        setOf(
            Permission.createReadPermission(HeartRateRecord::class),
            Permission.createWritePermission(HeartRateRecord::class),
            Permission.createReadPermission(StepsRecord::class),
            Permission.createWritePermission(StepsRecord::class),
            Permission.createReadPermission(SleepSessionRecord::class),
            Permission.createWritePermission(SleepSessionRecord::class)
        )
    private lateinit var healthConnectClient: HealthConnectClient

    private lateinit var healthConnectPermissionRequest: ActivityResultLauncher<Set<Permission>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkAndSetPermissionRequest()

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

    override fun onResume() {
        super.onResume()

        if(isInitialResume) {
            isInitialResume = false
            return
        }

        finish()
    }

    private fun checkAndSetPermissionRequest() {
        val healthConnectIsInstalled = HealthConnectClient.isAvailable(this)

        if (healthConnectIsInstalled) {
            healthConnectClient = AppModule.provideHealthConnectClient(this)
            val resultContract =
                healthConnectClient.permissionController.createRequestPermissionActivityContract()
            healthConnectPermissionRequest = registerForActivityResult(resultContract) {
                Log.w(tag, "$tag:: Activity result:\n$it")
            }
            Log.d(tag, "$tag:: Permission request set")

            lifecycleScope.launch {
                checkAndRequestHealthConnectPermissions()
            }
        } else {
            Log.d(tag, "$tag:: Health Connect is not installed, prompt to install")
            promptToInstallHealthConnect()
            Sahha.healthConnectCallback?.invoke(SahhaErrors.healthConnect.notInstalled, false)
            finish()
        }
    }

    private suspend fun checkAndRequestHealthConnectPermissions() {
            Log.d(tag, "$tag:: Health Connect is installed")
            val granted =
                healthConnectClient.permissionController.getGrantedPermissions(
                    healthConnectPermissions
                )
            if (containsAtleastOneGrantedPermission(granted)) {
                Log.d(tag, "$tag:: There are granted permissions")
                Sahha.healthConnectCallback?.invoke(null, true)
                finish()
            } else {
                Log.d(tag, "$tag:: No granted permissions, launch permission request")
                Sahha.healthConnectCallback?.invoke(SahhaErrors.healthConnect.noPermissions, false)
                healthConnectPermissionRequest.launch(healthConnectPermissions)
            }
    }

    private fun promptToInstallHealthConnect() {
        val packageName = "com.google.android.apps.healthdata"
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        Toast.makeText(this, "Health Connect must be installed", Toast.LENGTH_LONG).show()
    }

    private fun containsAtleastOneGrantedPermission(granted: Set<Permission>): Boolean {
        if (granted.isNotEmpty()) return true
        return false
    }
}