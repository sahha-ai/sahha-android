package sdk.sahha.android.domain.repository

import android.content.Context
import androidx.activity.ComponentActivity
import kotlinx.coroutines.Job
import sdk.sahha.android.source.SahhaSensor
import sdk.sahha.android.source.SahhaSensorStatus

interface PermissionsRepo {
    fun setPermissionLogic(activity: ComponentActivity)
    fun openAppSettings(context: Context)
    fun activate(
        context: Context,
        callback: ((error: String?, status: Enum<SahhaSensorStatus>) -> Unit)
    )

    fun enableSensors(
        context: Context,
        sensors: Set<SahhaSensor>,
        callback: (error: String?, statuses: Map<Enum<SahhaSensor>, Enum<SahhaSensorStatus>>) -> Unit
    ): Job

    fun getSensorStatuses(
        context: Context,
        sensors: Set<SahhaSensor> = SahhaSensor.values().toSet(),
        callback: ((error: String?, statuses: Map<Enum<SahhaSensor>, Enum<SahhaSensorStatus>>) -> Unit)
    ): Job
}