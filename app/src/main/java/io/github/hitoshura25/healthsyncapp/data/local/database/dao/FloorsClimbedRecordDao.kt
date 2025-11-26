package io.github.hitoshura25.healthsyncapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.FloorsClimbedRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FloorsClimbedRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FloorsClimbedRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<FloorsClimbedRecordEntity>)

    @Query("SELECT * FROM floors_climbed_records WHERE health_connect_uid = :hcUid LIMIT 1")
    suspend fun getRecordByHcUid(hcUid: String): FloorsClimbedRecordEntity?

    @Query("DELETE FROM floors_climbed_records WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>): Int

    @Query("DELETE FROM floors_climbed_records")
    suspend fun clearTable()

    /**
     * Observes all floors climbed records from the table, ordered by start time.
     * Useful for displaying data in the UI.
     */
    @Query("SELECT * FROM floors_climbed_records ORDER BY start_time_epoch_millis DESC")
    fun getAllObservable(): Flow<List<FloorsClimbedRecordEntity>>
}