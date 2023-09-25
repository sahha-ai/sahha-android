package sdk.sahha.android.data.listeners

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener2
import android.os.Build
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Device.Companion.TYPE_PHONE
import androidx.health.connect.client.records.metadata.Metadata
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sdk.sahha.android.data.Constants
import sdk.sahha.android.domain.model.steps.StepSession
import sdk.sahha.android.source.Sahha
import java.time.Instant
import java.time.ZoneId

class StepDetectorListener : SensorEventListener2 {
    internal val steps = mutableListOf<Long>()
    internal var sessionJob: Job? = null
    private var timestampEpoch = 0L
    override fun onSensorChanged(sensorEvent: SensorEvent?) {
        timestampEpoch = Sahha.di.timeManager.nowInEpoch()
        incrementSessionSteps()
        processSession()
    }

    private fun incrementSessionSteps() {
        steps.add(timestampEpoch)
    }

    private fun processSession() {
        sessionJob?.cancel()
        sessionJob = Sahha.di.ioScope.launch {
            delay(Constants.STEP_SESSION_COOLDOWN_MILLIS)
            storeSessionSteps()
            resetSessionSteps()
        }
    }

    internal suspend fun storeSessionSteps() {
        if (steps.isNotEmpty()) {
            Sahha.di.sensorRepo.saveStepSession(
                StepSession(
                    count = steps.count(),
                    startDateTime = Sahha.di.timeManager.epochMillisToISO(steps.first()),
                    endDateTime = Sahha.di.timeManager.epochMillisToISO(steps.last())
                )
            )

            val grantedHealthConnect = Sahha.di.healthConnectRepo.getGrantedPermissions()
            if (
                grantedHealthConnect.contains(
                    HealthPermission
                        .getWritePermission(StepsRecord::class)
                )
            ) {
                val zoneOffset = ZoneId.systemDefault().rules.getOffset(
                    Instant.now()
                )
                Sahha.di.healthConnectClient
                    ?.insertRecords(
                        listOf(
                            StepsRecord(
                                startTime = Instant.ofEpochMilli(steps.first()),
                                startZoneOffset = zoneOffset,
                                count = steps.count().toLong(),
                                endTime = Instant.ofEpochMilli(steps.last()),
                                endZoneOffset = zoneOffset,
                                metadata = Metadata(
                                    device = Device(
                                        manufacturer = Build.MANUFACTURER,
                                        model = Build.MODEL,
                                        type = TYPE_PHONE
                                    )
                                )
                            )
                        )
                    )
            }
        }
    }

    internal fun resetSessionSteps() {
        steps.clear()
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
    override fun onFlushCompleted(p0: Sensor?) {}
}