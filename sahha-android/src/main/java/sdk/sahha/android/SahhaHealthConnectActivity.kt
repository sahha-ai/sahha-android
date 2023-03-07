package sdk.sahha.android

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import sdk.sahha.android.common.SahhaErrors
import sdk.sahha.android.di.AppModule
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensor
import sdk.sahha.android.source.SahhaSensorStatus
import sdk.sahha.android.ui.theme.SahhasdkemptyTheme

@RequiresApi(Build.VERSION_CODES.Q)
class SahhaHealthConnectActivity : ComponentActivity() {
    private var isInitialResume = true
    private var healthConnectClient: HealthConnectClient? = null
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

        if (isInitialResume) {
            isInitialResume = false
            return
        }

        finish()
    }

    private fun checkAndSetPermissionRequest() {
        healthConnectClient = AppModule.provideHealthConnectClient(this)

        healthConnectClient?.also {
            val resultContract =
                it.permissionController.createRequestPermissionActivityContract()
            healthConnectPermissionRequest = registerForActivityResult(resultContract) {}

            lifecycleScope.launch {
                checkAndRequestHealthConnectPermissions()
            }
        } ?: promptToInstallHealthConnect()
    }

    private suspend fun checkAndRequestHealthConnectPermissions() {
        Sahha.di.permissionRepo.getHealthConnectStatus(this) { error, status ->
            if (status == SahhaSensorStatus.enabled) {
                updateSahhaConfig()
                Sahha.di.permissionRepo.healthConnectCallback?.invoke(
                    null,
                    SahhaSensorStatus.enabled
                )
                Sahha.start()
                finish()
            } else {
                error?.also {
                    Sahha.di.permissionRepo.healthConnectCallback?.invoke(
                        error,
                        SahhaSensorStatus.disabled
                    )
                }
                Sahha.di.permissionRepo.healthConnectCallback?.invoke(
                    SahhaErrors.healthConnect.noPermissions,
                    SahhaSensorStatus.disabled
                )
                healthConnectPermissionRequest.launch(Sahha.di.permissionRepo.healthConnectPermissions)
            }
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
        Sahha.di.permissionRepo.healthConnectCallback?.invoke(
            SahhaErrors.healthConnect.notInstalled,
            SahhaSensorStatus.disabled
        )
        finish()
    }

    private suspend fun updateSahhaConfig() {
        val config = Sahha.di.configurationDao.getConfig()
        config.sensorArray.add(SahhaSensor.health_connect.ordinal)
        Sahha.di.configurationDao.saveConfig(config)
    }
}