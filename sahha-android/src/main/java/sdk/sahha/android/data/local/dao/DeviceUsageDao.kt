package sdk.sahha.android.data.local.dao

import androidx.room.*
import sdk.sahha.android.domain.model.device.PhoneUsage
import sdk.sahha.android.domain.model.device.PhoneUsageSilver

@Dao
interface DeviceUsageDao {
    // Phone Usage
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUsage(usage: PhoneUsage)

    @Query("SELECT * FROM PhoneUsage")
    suspend fun getUsages(): List<PhoneUsage>

    @Query("DELETE FROM PhoneUsage")
    suspend fun clearUsages()

    @Delete
    suspend fun clearUsages(usages: List<PhoneUsage>)

    // Silver Layer
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSilverUsage(usage: PhoneUsageSilver)

    @Query("SELECT * FROM PhoneUsageSilver")
    suspend fun getSilverUsages(): List<PhoneUsageSilver>

    @Query("DELETE FROM PhoneUsageSilver")
    suspend fun clearSilverUsages()

    @Delete
    suspend fun clearSilverUsages(usages: List<PhoneUsageSilver>)
}