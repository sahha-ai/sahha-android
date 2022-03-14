package sdk.sahha.android._refactor.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import sdk.sahha.android._refactor.data.local.dao.MovementDao
import sdk.sahha.android._refactor.data.local.dao.SecurityDao
import sdk.sahha.android._refactor.domain.model.activities.PreviousActivity
import sdk.sahha.android._refactor.domain.model.activities.RecognisedActivity
import sdk.sahha.android._refactor.domain.model.security.EncryptUtility
import sdk.sahha.android._refactor.domain.model.steps.DetectedSteps
import sdk.sahha.android._refactor.domain.model.steps.LastDetectedSteps

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
    abstract fun movementDao(): MovementDao
    abstract fun securityDao(): SecurityDao
}
