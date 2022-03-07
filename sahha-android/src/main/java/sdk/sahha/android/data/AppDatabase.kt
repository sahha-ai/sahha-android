package sdk.sahha.android.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import sdk.sahha.android.data.dao.MovementDao
import sdk.sahha.android.data.dao.SecurityDao
import sdk.sahha.android.model.activities.PreviousActivity
import sdk.sahha.android.model.activities.RecognisedActivity
import sdk.sahha.android.model.security.EncryptUtility
import sdk.sahha.android.model.steps.DetectedSteps
import sdk.sahha.android.model.steps.LastDetectedSteps

class AppDatabase(context: Context) {
    val database by lazy {
        Room.databaseBuilder(
            context,
            MyRoomDatabase::class.java,
            "app-database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}

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
abstract class MyRoomDatabase : RoomDatabase() {
    abstract fun movementDao(): MovementDao
    abstract fun securityDao(): SecurityDao
}
