package sdk.sahha.android.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sahha.android.model.SleepQueue
import com.sahha.android.model.SleepQueueHistory
import sdk.sahha.android.data.local.converter.Converter
import sdk.sahha.android.data.local.dao.BatchedDataDao
import sdk.sahha.android.data.local.dao.ConfigurationDao
import sdk.sahha.android.data.local.dao.DeviceUsageDao
import sdk.sahha.android.data.local.dao.HealthConnectConfigDao
import sdk.sahha.android.data.local.dao.ManualPermissionsDao
import sdk.sahha.android.data.local.dao.MovementDao
import sdk.sahha.android.data.local.dao.RationaleDao
import sdk.sahha.android.data.local.dao.SecurityDao
import sdk.sahha.android.data.local.dao.SleepDao
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
import sdk.sahha.android.domain.model.permissions.Rationale
import sdk.sahha.android.domain.model.security.EncryptUtility
import sdk.sahha.android.domain.model.steps.StepData
import sdk.sahha.android.domain.model.steps.StepSession
import sdk.sahha.android.domain.model.steps.StepsHealthConnect
import sdk.sahha.android.source.SahhaNotificationConfiguration

@Database(
    version = 13,
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
        Rationale::class
    ],
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 10, to = 11),
        AutoMigration(from = 11, to = 12),
        AutoMigration(from = 12, to = 13),
    ]
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
    internal abstract fun rationaleDao(): RationaleDao
}
