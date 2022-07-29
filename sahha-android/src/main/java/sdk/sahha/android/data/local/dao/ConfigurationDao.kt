package sdk.sahha.android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import sdk.sahha.android.domain.model.config.SahhaConfiguration
import sdk.sahha.android.source.SahhaNotificationConfiguration

@Dao
interface ConfigurationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: SahhaConfiguration)

    @Query("SELECT * FROM SahhaConfiguration WHERE id=1")
    suspend fun getConfig(): SahhaConfiguration

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveNotificationConfig(notificationConfiguration: SahhaNotificationConfiguration)

    @Query("SELECT * FROM SahhaNotificationConfiguration WHERE id=1")
    suspend fun getNotificationConfig(): SahhaNotificationConfiguration
}