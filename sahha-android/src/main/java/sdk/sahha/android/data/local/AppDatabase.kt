package sdk.sahha.android.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sahha.android.model.SleepQueue
import com.sahha.android.model.SleepQueueHistory
import sdk.sahha.android.data.local.converter.Converter
import sdk.sahha.android.data.local.dao.*
import sdk.sahha.android.data.remote.dto.SleepDto
import sdk.sahha.android.domain.model.activities.PreviousActivity
import sdk.sahha.android.domain.model.activities.RecognisedActivity
import sdk.sahha.android.domain.model.config.SahhaConfiguration
import sdk.sahha.android.domain.model.device.AppUsage
import sdk.sahha.android.domain.model.device.DeviceUsage
import sdk.sahha.android.domain.model.device.PhoneUsage
import sdk.sahha.android.domain.model.security.EncryptUtility
import sdk.sahha.android.domain.model.steps.DetectedSteps
import sdk.sahha.android.domain.model.steps.LastDetectedSteps

@Database(
    version = 1,
    entities = [
        DetectedSteps::class,
        LastDetectedSteps::class,
        RecognisedActivity::class,
        PreviousActivity::class,
        EncryptUtility::class,
        SleepQueue::class,
        SleepQueueHistory::class,
        PhoneUsage::class,
        AppUsage::class,
        DeviceUsage::class,
        SahhaConfiguration::class,
        SleepDto::class
    ]
)

@TypeConverters(Converter::class)
abstract class SahhaDatabase : RoomDatabase() {
    internal abstract fun movementDao(): MovementDao
    internal abstract fun securityDao(): SecurityDao
    internal abstract fun sleepDao(): SleepDao
    internal abstract fun deviceUsageDao(): DeviceUsageDao
    internal abstract fun configurationDao(): ConfigurationDao
}
