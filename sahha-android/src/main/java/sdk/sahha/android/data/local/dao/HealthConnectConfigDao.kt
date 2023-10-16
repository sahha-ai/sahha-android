package sdk.sahha.android.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import sdk.sahha.android.domain.model.health_connect.HealthConnectQuery

@Dao
interface HealthConnectConfigDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveQuery(query: HealthConnectQuery)

    @Query("SELECT * FROM HealthConnectQuery WHERE id = :recordType")
    suspend fun getQueryOf(recordType: String): HealthConnectQuery?

    @Query("DELETE FROM HealthConnectQuery")
    suspend fun clearAllQueries()

    @Delete
    suspend fun clearQueries(queries: List<HealthConnectQuery>)
}