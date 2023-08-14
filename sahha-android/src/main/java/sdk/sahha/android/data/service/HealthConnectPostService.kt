package sdk.sahha.android.data.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.SleepStageRecord
import androidx.health.connect.client.records.StepsRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sdk.sahha.android.common.SahhaReconfigure
import sdk.sahha.android.data.Constants
import sdk.sahha.android.source.Sahha

class HealthConnectPostService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val ioScope = CoroutineScope(Dispatchers.IO)
        ioScope.launch {
            SahhaReconfigure(this@HealthConnectPostService)
            queryAndPostHealthConnectData()
        }

        return START_NOT_STICKY
    }

    private suspend fun queryAndPostHealthConnectData() {
        val repo = Sahha.di.healthConnectRepo
        val granted = repo.getGrantedPermissions()

        granted.forEach {
            when (it) {
                HealthPermission.getReadPermission(StepsRecord::class) -> {
//                    repo.postData(
//                        repo.getSteps(),
//                        Constants.DEFAULT_POST_LIMIT,
//
//                    )
                }
                HealthPermission.getReadPermission(SleepSessionRecord::class) -> {}
                HealthPermission.getReadPermission(SleepStageRecord::class) -> {}
                HealthPermission.getReadPermission(HeartRateRecord::class) -> {}
                HealthPermission.getReadPermission(RestingHeartRateRecord::class) -> {}
                HealthPermission.getReadPermission(BloodGlucoseRecord::class) -> {}
                HealthPermission.getReadPermission(BloodPressureRecord::class) -> {}
            }
        }
    }
}