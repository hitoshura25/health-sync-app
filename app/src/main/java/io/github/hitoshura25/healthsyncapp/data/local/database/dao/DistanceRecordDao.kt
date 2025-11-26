package io.github.hitoshura25.healthsyncapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.DistanceRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DistanceRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DistanceRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<DistanceRecordEntity>)

    @Query("SELECT * FROM distance_records WHERE health_connect_uid = :hcUid LIMIT 1")
    suspend fun getRecordByHcUid(hcUid: String): DistanceRecordEntity?

    @Query("DELETE FROM distance_records WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>): Int

    @Query("DELETE FROM distance_records")
    suspend fun clearTable()

    /**
     * Observes all distance records from the table, ordered by start time.
     * Useful for displaying data in the UI.
     */
    @Query("SELECT * FROM distance_records ORDER BY start_time_epoch_millis DESC")
    fun getAllObservable(): Flow<List<DistanceRecordEntity>>
}
