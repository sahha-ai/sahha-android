package sdk.sahha.android.data.local.dao

import androidx.room.*
import sdk.sahha.android.domain.model.device.PhoneUsage

@Dao
internal interface DeviceUsageDao {
    // Phone Usage
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUsage(usage: PhoneUsage)

    @Query("SELECT * FROM PhoneUsage")
    suspend fun getUsages(): List<PhoneUsage>

    @Query("DELETE FROM PhoneUsage")
    suspend fun clearUsages()

    @Delete
    suspend fun clearUsages(usages: List<PhoneUsage>)
}