package sdk.sahha.android.common

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.health.connect.client.HealthConnectClient
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaSensor
import sdk.sahha.android.source.SahhaSensorStatus

internal const val PREFERENCE_KEY = "sdk.sahha.android.PREFERENCE_KEY"
internal const val RATIONALE_KEY = "shouldShowRationale"
internal const val PERMISSIONS_KEY = "permissions"
internal const val PERMISSION_ENABLED = "SahhaPermissionActivity.permission_enabled"
internal const val PERMISSION_PENDING = "SahhaPermissionActivity.permission_pending"
internal const val PERMISSION_DISABLED = "SahhaPermissionActivity.permission_disabled"

internal class SahhaSensorPermissionActivity : AppCompatActivity() {

    private val permissionRequestCode = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.getStringArrayExtra(PERMISSIONS_KEY)?.let {
            ActivityCompat.requestPermissions(
                this,
                it,
                permissionRequestCode
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        val sharedPrefs = getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE)

        if (requestCode == permissionRequestCode && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendBroadcast(Intent(PERMISSION_ENABLED))
            } else {
                val shouldShowRationale =
                    ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])

                if (shouldShowRationale) {
                    sendBroadcast(Intent(PERMISSION_PENDING))
                    sharedPrefs.edit().putBoolean(RATIONALE_KEY, shouldShowRationale).apply()
                } else {
                    if (!sharedPrefs.contains(RATIONALE_KEY)) {
                        sendBroadcast(Intent(PERMISSION_PENDING))
                        sharedPrefs.edit().putBoolean(RATIONALE_KEY, shouldShowRationale).apply()
                    } else {
                        sendBroadcast(Intent(PERMISSION_DISABLED))
                        sharedPrefs.edit().putBoolean(RATIONALE_KEY, shouldShowRationale).apply()
                    }
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
        finish()
    }
}

internal class SahhaSensorStatusActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPrefs = getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE)
        if (!sharedPrefs.contains(RATIONALE_KEY)) {
            sendBroadcast(Intent(PERMISSION_PENDING))
            finish()
            return
        }

        intent.getStringArrayExtra(PERMISSIONS_KEY)?.let {
            val shouldShowRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this, it[0])
            if (shouldShowRationale) {
                sendBroadcast(Intent(PERMISSION_PENDING))
            } else {
                sendBroadcast(Intent(PERMISSION_DISABLED))
            }
        }

        finish()
    }
}

internal object SahhaPermissions : BroadcastReceiver() {
    private val healthConnectClient by lazy { Sahha.di.healthConnectClient }
    private val mainScope by lazy { Sahha.di.mainScope }
    private val deviceOnly get() = runBlocking {
        val sensors = Sahha.di.sahhaConfigRepo.getConfig().sensorArray

        sensors.contains(SahhaSensor.device.ordinal)
                && sensors.count() == 1
    }

    var permissionCallback: ((Enum<SahhaSensorStatus>) -> Unit)? = null

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            PERMISSION_ENABLED -> {
                try {
                    context.unregisterReceiver(this)
                } catch (e: Exception) {
                    println(e.message)
                }
                onPermissionEnabled()
            }

            PERMISSION_PENDING -> {
                try {
                    context.unregisterReceiver(this)
                } catch (e: Exception) {
                    println(e.message)
                }
                onPermissionPending()
            }

            PERMISSION_DISABLED -> {
                try {
                    context.unregisterReceiver(this)
                } catch (e: Exception) {
                    println(e.message)
                }
                onPermissionDisabled()
            }

            else -> {
                try {
                    context.unregisterReceiver(this)
                } catch (e: Exception) {
                    println(e.message)
                }
                onPermissionUnavailable()
            }
        }
    }

    private fun onPermissionEnabled() {
        permissionCallback?.invoke(SahhaSensorStatus.enabled)
        permissionCallback = null
    }

    private fun onPermissionPending() {
        permissionCallback?.invoke(SahhaSensorStatus.pending)
        permissionCallback = null
    }

    private fun onPermissionDisabled() {
        permissionCallback?.invoke(SahhaSensorStatus.disabled)
        permissionCallback = null
    }

    private fun onPermissionUnavailable() {
        permissionCallback?.invoke(SahhaSensorStatus.unavailable)
        permissionCallback = null
    }

    private suspend fun checkPermissions(
        healthConnectClient: HealthConnectClient,
        callback: ((Enum<SahhaSensorStatus>) -> Unit)?
    ) {
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        val permissions = Sahha.di.permissionManager.getHcPermissions()

        if (granted.containsAll(permissions)) {
            callback?.invoke(SahhaSensorStatus.enabled)
            return
        }

        // Else
        callback?.invoke(SahhaSensorStatus.disabled)
    }

    private fun healthConnectUnavailable(callback: ((Enum<SahhaSensorStatus>) -> Unit)?) {
        callback?.invoke(SahhaSensorStatus.unavailable)
    }

    fun getSensorStatusHealthConnect(
        callback: ((Enum<SahhaSensorStatus>) -> Unit)?
    ) {
        healthConnectClient?.also { client ->
            mainScope.launch {
                checkPermissions(client, callback)
            }
        } ?: healthConnectUnavailable(callback)
    }

    fun getSensorStatus(
        context: Context,
        callback: ((Enum<SahhaSensorStatus>) -> Unit)?
    ) {
        if (deviceOnly) {
            callback?.invoke(SahhaSensorStatus.pending)
            return
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            callback?.invoke(SahhaSensorStatus.unavailable)
            return
        }

        when (context.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION)) {
            PackageManager.PERMISSION_GRANTED -> {
                callback?.invoke(SahhaSensorStatus.enabled)
            }

            PackageManager.PERMISSION_DENIED -> {
                permissionCallback = callback
                startPermissionActivity(context, SahhaSensorStatusActivity::class.java)
            }

            else -> {
                callback?.invoke(SahhaSensorStatus.unavailable)
            }
        }
    }

    fun enableSensor(
        context: Context,
        callback: ((Enum<SahhaSensorStatus>) -> Unit)?
    ) {
        if (deviceOnly) {
            callback?.invoke(SahhaSensorStatus.pending)
            return
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            callback?.invoke(SahhaSensorStatus.unavailable)
            return
        }

        permissionCallback = callback
        startPermissionActivity(context, SahhaSensorPermissionActivity::class.java)
    }

    fun activityRecognitionGranted(context: Context): Enum<SahhaSensorStatus> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return SahhaSensorStatus.unavailable
        }

        return when (context.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION)) {
            PackageManager.PERMISSION_GRANTED -> SahhaSensorStatus.enabled
            PackageManager.PERMISSION_DENIED -> SahhaSensorStatus.disabled
            else -> SahhaSensorStatus.unavailable
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun startPermissionActivity(context: Context, activityClass: Class<*>) {
        val intentFilter = getPermissionIntentFilter()
        val intent = getPermissionIntent(context, activityClass)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            context.registerReceiver(this, intentFilter, Context.RECEIVER_EXPORTED)
        else context.registerReceiver(this, intentFilter)

        context.startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getPermissionIntent(context: Context, activityClass: Class<*>): Intent {
        return Intent(context, activityClass).apply {
            putExtra(PERMISSIONS_KEY, arrayOf(Manifest.permission.ACTIVITY_RECOGNITION))
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    private fun getPermissionIntentFilter(): IntentFilter {
        return IntentFilter().apply {
            addAction(PERMISSION_ENABLED)
            addAction(PERMISSION_PENDING)
            addAction(PERMISSION_DISABLED)
        }
    }
}