package sdk.sahha.android.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sahha.android.model.SleepQueue
import com.sahha.android.model.SleepQueueHistory
import sdk.sahha.android.data.local.converter.Converter
import sdk.sahha.android.data.local.dao.*
import sdk.sahha.android.domain.model.activities.PreviousActivity
import sdk.sahha.android.domain.model.activities.RecognisedActivity
import sdk.sahha.android.domain.model.config.SahhaConfiguration
import sdk.sahha.android.domain.model.data_log.SahhaDataLog
import sdk.sahha.android.domain.model.device.AppUsage
import sdk.sahha.android.domain.model.device.DeviceUsage
import sdk.sahha.android.domain.model.device.PhoneUsage
import sdk.sahha.android.domain.model.device_info.DeviceInformation
import sdk.sahha.android.domain.model.dto.SleepDto
import sdk.sahha.android.domain.model.health_connect.HealthConnectChangeToken
import sdk.sahha.android.domain.model.health_connect.HealthConnectQuery
import sdk.sahha.android.domain.model.permissions.ManualPermission
import sdk.sahha.android.domain.model.security.EncryptUtility
import sdk.sahha.android.domain.model.steps.StepData
import sdk.sahha.android.domain.model.steps.StepSession
import sdk.sahha.android.domain.model.steps.StepsHealthConnect
import sdk.sahha.android.source.SahhaNotificationConfiguration

@Database(
    version = 11,
    entities = [
        RecognisedActivity::class,
        PreviousActivity::class,
        EncryptUtility::class,
        SleepQueue::class,
        SleepQueueHistory::class,
        PhoneUsage::class,
        AppUsage::class,
        DeviceUsage::class,
        SahhaConfiguration::class,
        SleepDto::class,
        SahhaNotificationConfiguration::class,
        StepData::class,
        DeviceInformation::class,
        StepSession::class,
        HealthConnectQuery::class,
        HealthConnectChangeToken::class,
        StepsHealthConnect::class,
        ManualPermission::class,
        SahhaDataLog::class,
    ],
    exportSchema = true
)

@TypeConverters(Converter::class)
internal abstract class SahhaDatabase : RoomDatabase() {
    internal abstract fun movementDao(): MovementDao
    internal abstract fun securityDao(): SecurityDao
    internal abstract fun sleepDao(): SleepDao
    internal abstract fun deviceUsageDao(): DeviceUsageDao
    internal abstract fun configurationDao(): ConfigurationDao
    internal abstract fun healthConnectConfigDao(): HealthConnectConfigDao
    internal abstract fun manualPermissionsDao(): ManualPermissionsDao
    internal abstract fun BatchedDataDao(): BatchedDataDao
}
