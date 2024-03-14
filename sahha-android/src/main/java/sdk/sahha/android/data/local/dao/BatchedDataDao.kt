package sdk.sahha.android.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import sdk.sahha.android.domain.model.data_log.SahhaDataLog

@Dao
internal interface BatchedDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBatchedData(data: List<SahhaDataLog>)

    @Query("SELECT * FROM SahhaDataLog")
    suspend fun getBatchedData(): List<SahhaDataLog>

    @Delete
    suspend fun deleteBatchedData(data: List<SahhaDataLog>)

    @Query("DELETE FROM SahhaDataLog")
    suspend fun deleteAllBatchedData()
}