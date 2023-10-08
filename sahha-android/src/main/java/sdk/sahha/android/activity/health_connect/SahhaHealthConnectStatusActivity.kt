package sdk.sahha.android.activity.health_connect

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensorStatus

internal class SahhaHealthConnectStatusActivity : AppCompatActivity() {
    private val permissionHandler by lazy { Sahha.di.permissionHandler }
    private val permissionManager by lazy { Sahha.di.permissionManager }
    private val healthConnectClient by lazy { Sahha.di.healthConnectClient }
    private val healthConnectRepo by lazy { Sahha.di.healthConnectRepo }
    private val permissions by lazy { permissionManager.permissions }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            SahhaReconfigure(this@SahhaHealthConnectStatusActivity)
            healthConnectClient?.also { client ->
                checkPermissions(client)
            } ?: healthConnectUnavailable()
        }

//        setContent {
//            SahhasdkemptyTheme {
//                // A surface container using the 'background' color from the theme
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colors.background.copy(alpha = 0f),
//                ) {}
//            }
//        }
    }

    //    private fun atleastOnePermissionGranted(granted: Set<String>): Boolean {
//        return granted.any { it in healthConnectRepo.permissions }
//    }
    private suspend fun checkPermissions(healthConnectClient: HealthConnectClient) {
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        if (granted.containsAll(permissions)) {
            println("SahhaHealthConnectStatusActivity0001")
            permissionHandler.activityCallback.statusCallback
                ?.invoke(null, SahhaSensorStatus.enabled)
            finish()
            return
        }

        // Else
        println("SahhaHealthConnectStatusActivity0002")
        permissionHandler.activityCallback.statusCallback
            ?.invoke(null, SahhaSensorStatus.disabled)
        finish()
    }

    private fun healthConnectUnavailable() {
        println("SahhaHealthConnectStatusActivity0003")
        permissionHandler.activityCallback.statusCallback
            ?.invoke(null, SahhaSensorStatus.unavailable)
        finish()
    }
}