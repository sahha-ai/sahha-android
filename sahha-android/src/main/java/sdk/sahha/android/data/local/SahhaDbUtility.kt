package sdk.sahha.android.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import sdk.sahha.android.common.Constants

internal object SahhaDbUtility {
    fun getDb(context: Context): SahhaDatabase {
        return Room.databaseBuilder(
            context,
            SahhaDatabase::class.java,
            "sahha-database"
        )
            .fallbackToDestructiveMigration()
            .addMigrations(
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_5_6,
                MIGRATION_6_7,
                MIGRATION_7_8,
                MIGRATION_8_10,
                MIGRATION_9_10
            )
            .build()
    }

    internal val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            with(database) {
                execSQL("ALTER TABLE SleepDto ADD COLUMN sleepStage TEXT NOT NULL DEFAULT('asleep')")
                execSQL("ALTER TABLE SleepDto RENAME COLUMN minutesSlept TO durationInMinutes")
            }
        }
    }

    internal val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            with(database) {
                execSQL("ALTER TABLE PhoneUsage ADD COLUMN isScreenOn INTEGER NOT NULL DEFAULT(0)")
            }
        }
    }

    internal val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            with(database) {
                execSQL("DROP TABLE LastDetectedSteps")
                execSQL("DROP TABLE DetectedSteps")
                execSQL("CREATE TABLE StepData (id INTEGER NOT NULL, source TEXT NOT NULL, count INTEGER NOT NULL, detectedAt TEXT NOT NULL, PRIMARY KEY(id))")
            }
        }
    }

    internal val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            with(database) {
                execSQL("ALTER TABLE SleepDto ADD COLUMN source TEXT NOT NULL DEFAULT('${Constants.SLEEP_DATA_SOURCE}')")
            }
        }
    }

    internal val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            with(database) {
                execSQL("CREATE TABLE DeviceInformation (id INTEGER NOT NULL, sdkId TEXT NOT NULL, sdkVersion TEXT NOT NULL, appId TEXT NOT NULL, deviceType TEXT NOT NULL, deviceModel TEXT NOT NULL, system TEXT NOT NULL, systemVersion TEXT NOT NULL, timeZone TEXT NOT NULL, PRIMARY KEY(id))")
            }
        }
    }

    internal val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(database: SupportSQLiteDatabase) {
            with(database) {
                execSQL("CREATE TABLE StepSession (id INTEGER NOT NULL, count INTEGER NOT NULL, startDateTime TEXT NOT NULL, endDateTime TEXT NOT NULL, PRIMARY KEY (id))")
            }
        }
    }

    internal val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(database: SupportSQLiteDatabase) {
            with(database) {
                execSQL("CREATE TABLE HealthConnectQuery (id TEXT NOT NULL, lastSuccessfulTimeStampEpochMillis INTEGER NOT NULL, PRIMARY KEY (id))")
                execSQL("CREATE TABLE StepsHealthConnect (metaId TEXT NOT NULL, count INTEGER NOT NULL, dataType TEXT NOT NULL, source TEXT NOT NULL, startDateTime TEXT NOT NULL, endDateTime TEXT NOT NULL,  modifiedDateTime TEXT NOT NULL, recordingMethod TEXT NOT NULL, deviceType TEXT NOT NULL, deviceManufacturer TEXT NOT NULL, deviceModel TEXT NOT NULL, PRIMARY KEY (metaId))")
            }
        }
    }

    internal val MIGRATION_8_10 = object : Migration(8, 10) {
        override fun migrate(database: SupportSQLiteDatabase) {
            with(database) {
                execSQL("""
                    CREATE TABLE ManualPermission (
                        sensorEnum INTEGER NOT NULL,
                        statusEnum INTEGER NOT NULL,
                        PRIMARY KEY (sensorEnum)
                    )
                """.trimIndent())
                execSQL("""
                    CREATE TABLE SahhaDataLog (
                        id TEXT NOT NULL PRIMARY KEY,
                        logType TEXT NOT NULL,
                        dataType TEXT NOT NULL,
                        value REAL NOT NULL,
                        source TEXT NOT NULL,
                        startDateTime TEXT NOT NULL,
                        endDateTime TEXT NOT NULL,
                        unit TEXT NOT NULL,
                        recordingMethod TEXT NOT NULL DEFAULT 'RECORDING_METHOD_UNKNOWN',
                        deviceType TEXT NOT NULL DEFAULT 'UNKNOWN',
                        additionalProperties TEXT,
                        parentId TEXT
                    )
                """.trimIndent())
            }
        }
    }

    internal val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(database: SupportSQLiteDatabase) {
            with(database) {
                execSQL("""
                    CREATE TABLE SahhaDataLog (
                        id TEXT NOT NULL PRIMARY KEY,
                        logType TEXT NOT NULL,
                        dataType TEXT NOT NULL,
                        value REAL NOT NULL,
                        source TEXT NOT NULL,
                        startDateTime TEXT NOT NULL,
                        endDateTime TEXT NOT NULL,
                        unit TEXT NOT NULL,
                        recordingMethod TEXT NOT NULL DEFAULT 'RECORDING_METHOD_UNKNOWN',
                        deviceType TEXT NOT NULL DEFAULT 'UNKNOWN',
                        additionalProperties TEXT,
                        parentId TEXT
                    )
                """.trimIndent())
            }
        }
    }
}