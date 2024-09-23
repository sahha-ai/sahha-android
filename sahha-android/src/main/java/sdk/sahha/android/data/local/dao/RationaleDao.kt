package sdk.sahha.android.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import sdk.sahha.android.domain.model.permissions.Rationale

@Dao
internal interface RationaleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createRationale(rationale: Rationale)

    @Query("SELECT * FROM Rationale WHERE sensorType = :sensorType")
    suspend fun getRationale(sensorType: Int): Rationale?

    @Delete
    suspend fun removeRationale(rationales: List<Rationale>)

    @Query("DELETE FROM Rationale")
    suspend fun removeAllRationale()
}