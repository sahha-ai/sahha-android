package sdk.sahha.android.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import sdk.sahha.android.data.Constants

object SahhaDbMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            with(database) {
                execSQL("ALTER TABLE SleepDto ADD COLUMN sleepStage TEXT NOT NULL DEFAULT('asleep')")
                execSQL("ALTER TABLE SleepDto RENAME COLUMN minutesSlept TO durationInMinutes")
            }
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            with(database) {
                execSQL("ALTER TABLE PhoneUsage ADD COLUMN isScreenOn INTEGER NOT NULL DEFAULT(0)")
            }
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            with(database) {
                execSQL("DROP TABLE LastDetectedSteps")
                execSQL("DROP TABLE DetectedSteps")
                execSQL("CREATE TABLE StepData (id INTEGER NOT NULL, source TEXT NOT NULL, count INTEGER NOT NULL, detectedAt TEXT NOT NULL, PRIMARY KEY(id))")
            }
        }
    }

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            with(database) {
                execSQL("ALTER TABLE SleepDto ADD COLUMN source TEXT NOT NULL DEFAULT('${Constants.SLEEP_DATA_SOURCE}')")
            }
        }
    }

    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            with(database) {
                execSQL("CREATE TABLE DeviceInformation (id INTEGER NOT NULL, sdkId TEXT NOT NULL, sdkVersion TEXT NOT NULL, appId TEXT NOT NULL, deviceType TEXT NOT NULL, deviceModel TEXT NOT NULL, system TEXT NOT NULL, systemVersion TEXT NOT NULL, timeZone TEXT NOT NULL, PRIMARY KEY(id))")
            }
        }
    }

    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(database: SupportSQLiteDatabase) {
            with(database) {
                execSQL("CREATE TABLE LastHealthConnectPost (id INTEGER NOT NULL, epochMillis INTEGER NOT NULL, PRIMARY KEY(id))")
            }
        }
    }
}