package sdk.sahha.android.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import sdk.sahha.android.data.local.dao.MovementDao
import sdk.sahha.android.data.local.dao.SecurityDao
import sdk.sahha.android.domain.model.activities.PreviousActivity
import sdk.sahha.android.domain.model.activities.RecognisedActivity
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
        EncryptUtility::class
    ]
)

abstract class SahhaDatabase : RoomDatabase() {
    internal abstract fun movementDao(): MovementDao
    internal abstract fun securityDao(): SecurityDao
}
