package sdk.sahha.android.data.local.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import sdk.sahha.android.domain.model.device.AppUsage
import sdk.sahha.android.domain.model.device.DeviceUsage
import sdk.sahha.android.domain.model.device.PhoneUsage

interface DeviceUsageDao {
    // Phone Usage
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUsage(usage: PhoneUsage)

    @Query("SELECT * FROM PhoneUsage")
    suspend fun getUsages(): List<PhoneUsage>

    @Query("DELETE FROM PhoneUsage")
    suspend fun clearUsages()

    // Device Usage
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDeviceUsage(usage: DeviceUsage)

    @Query("SELECT * FROM DeviceUsage")
    suspend fun getDeviceUsages(): List<DeviceUsage>

    @Query("DELETE FROM DeviceUsage")
    suspend fun clearDeviceUsages()

    // App Usage
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAppUsage(usage: AppUsage)

    @Query("SELECT * FROM AppUsage")
    suspend fun getAppUsages(): List<AppUsage>

    @Query("DELETE FROM AppUsage")
    suspend fun clearAppUsages()
}