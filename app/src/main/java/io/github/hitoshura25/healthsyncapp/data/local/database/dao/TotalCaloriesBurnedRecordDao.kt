package io.github.hitoshura25.healthsyncapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.TotalCaloriesBurnedRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TotalCaloriesBurnedRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<TotalCaloriesBurnedRecordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TotalCaloriesBurnedRecordEntity): Long

    @Query("SELECT * FROM total_calories_burned_records WHERE health_connect_uid = :hcUid LIMIT 1")
    suspend fun getRecordByHcUid(hcUid: String): TotalCaloriesBurnedRecordEntity?

    @Query("DELETE FROM total_calories_burned_records WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>): Int

    @Query("DELETE FROM total_calories_burned_records")
    suspend fun clearTable()

    /**
     * Observes all total calories burned records from the table, ordered by start time.
     * Useful for displaying data in the UI.
     */
    @Query("SELECT * FROM total_calories_burned_records ORDER BY start_time_epoch_millis DESC")
    fun getAllObservable(): Flow<List<TotalCaloriesBurnedRecordEntity>>
}