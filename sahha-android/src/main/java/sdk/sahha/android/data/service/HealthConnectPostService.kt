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
import sdk.sahha.android.domain.model.device.PhoneUsage
import sdk.sahha.android.domain.use_case.query.QueryHealthConnectRecordsUseCase
import sdk.sahha.android.source.Sahha
import sdk.sahha.android.source.SahhaConverterUtility
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit

class HealthConnectPostService : Service() {
    val ioScope = CoroutineScope(Dispatchers.IO)
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        ioScope.launch {
            SahhaReconfigure(this@HealthConnectPostService)
            startNotification(intent)
            queryAndPostHealthConnectData()
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun startNotification(intent: Intent) {
        Sahha.di.sahhaNotificationManager.setNewNotification(
            title = intent.getStringExtra("title") ?: "Sending data for analysis...",
            channelId = Constants.HEALTH_CONNECT_NOTIFICATION_CHANNEL_ID,
            "Health Connect Sync",
            HealthConnectPostService::class.java
        )
        startForeground(
            Constants.NOTIFICATION_HEALTH_CONNECT,
            Sahha.di.sahhaNotificationManager.notification
        )
    }

    private fun setNextAlarmTime(
        amountToAdd: Long,
        timeUnit: TemporalUnit
    ) {
        val nextTimeStampEpochMillis = Instant.now().plus(amountToAdd, timeUnit).toEpochMilli()

        Sahha.di.sahhaAlarmManager.setAlarm(
            this, nextTimeStampEpochMillis
        )
    }

    private suspend fun queryAndPostHealthConnectData() {
        val repo = Sahha.di.healthConnectRepo
        val granted = repo.getGrantedPermissions()
        val usecase = QueryHealthConnectRecordsUseCase(repo)

        granted.forEach {
            when (it) {
                HealthPermission.getReadPermission(StepsRecord::class) -> {
                    val records = usecase(StepsRecord::class)
                    records?.also {
                        val json = SahhaConverterUtility.convertToJsonString(records)
                        Sahha.di.deviceUsageDao.saveUsage(
                            PhoneUsage(false, false, json)
                        )
                    }
                }

                HealthPermission.getReadPermission(SleepSessionRecord::class) -> {}
                HealthPermission.getReadPermission(SleepStageRecord::class) -> {}
                HealthPermission.getReadPermission(HeartRateRecord::class) -> {}
                HealthPermission.getReadPermission(RestingHeartRateRecord::class) -> {}
                HealthPermission.getReadPermission(BloodGlucoseRecord::class) -> {}
                HealthPermission.getReadPermission(BloodPressureRecord::class) -> {}
            }

        }
        setNextAlarmTime(1, ChronoUnit.HOURS)
    }
}