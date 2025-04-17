package sdk.sahha.android.framework.activity.health_connect

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.common.Session
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensorStatus

internal class SahhaHealthConnectStatusActivity : AppCompatActivity() {
    private val permissionHandler by lazy { Sahha.di.permissionHandler }
    private val healthConnectClient by lazy { Sahha.di.healthConnectClient }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            SahhaReconfigure(this@SahhaHealthConnectStatusActivity)
            healthConnectClient?.also { client ->
                checkPermissions(this@SahhaHealthConnectStatusActivity, client)
            } ?: healthConnectUnavailable()
        }
    }

    private suspend fun checkPermissions(context: Context, healthConnectClient: HealthConnectClient) {
        Sahha.di.permissionManager.getTrimmedHcPermissions(
            Sahha.di.permissionManager.getManifestPermissions(context = context),
            Session.sensors ?: setOf()
        ) { error, status, permissions ->
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            if (granted.containsAll(permissions)) {
                permissionHandler.activityCallback.statusCallback
                    ?.invoke(null, SahhaSensorStatus.enabled)
                finish()
                return@getTrimmedHcPermissions
            }

            // Else
            permissionHandler.activityCallback.statusCallback
                ?.invoke(null, SahhaSensorStatus.disabled)
            finish()
        }

    }

    private suspend fun healthConnectUnavailable() = coroutineScope {
        permissionHandler.activityCallback.statusCallback
            ?.invoke(null, SahhaSensorStatus.unavailable)
        finish()
    }
}