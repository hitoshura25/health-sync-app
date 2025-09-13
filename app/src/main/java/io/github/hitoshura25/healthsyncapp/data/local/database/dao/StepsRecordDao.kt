package io.github.hitoshura25.healthsyncapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.StepsRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StepsRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: StepsRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<StepsRecordEntity>)

    @Query("SELECT * FROM steps_records WHERE health_connect_uid = :hcUid LIMIT 1")
    suspend fun getRecordByHcUid(hcUid: String): StepsRecordEntity?

    @Query("SELECT * FROM steps_records WHERE is_synced = 0 ORDER BY start_time_epoch_millis ASC")
    suspend fun getUnsyncedSteps(): List<StepsRecordEntity>

    @Query("UPDATE steps_records SET is_synced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Long>): Int

    @Query("DELETE FROM steps_records WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>): Int

    @Query("DELETE FROM steps_records")
    suspend fun clearTable()

    /**
     * Observes all steps records from the table, ordered by start time.
     * Useful for displaying data in the UI.
     */
    @Query("SELECT * FROM steps_records ORDER BY start_time_epoch_millis DESC")
    fun getAllObservable(): Flow<List<StepsRecordEntity>>
}
