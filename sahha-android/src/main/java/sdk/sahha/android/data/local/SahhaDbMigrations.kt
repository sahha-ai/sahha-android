package sdk.sahha.android.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object SahhaDbMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            with(database) {
                execSQL("ALTER TABLE SleepDto ADD COLUMN sleepStage TEXT NOT NULL DEFAULT('asleep')")
                execSQL("ALTER TABLE SleepDto RENAME COLUMN minutesSlept TO durationInMinutes")
            }
        }
    }
}