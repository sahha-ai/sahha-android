package sdk.sahha.android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import sdk.sahha.android.domain.model.permissions.ManualPermission

@Dao
internal interface ManualPermissionsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePermission(manualPermission: ManualPermission)

    @Query("SELECT * FROM ManualPermission WHERE sensorEnum=:sensorEnum")
    suspend fun getPermissionStatus(sensorEnum: Int): ManualPermission?
}