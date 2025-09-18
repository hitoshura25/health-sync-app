package io.github.hitoshura25.healthsyncapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.HeartRateVariabilityRmssdRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HeartRateVariabilityRmssdRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: HeartRateVariabilityRmssdRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<HeartRateVariabilityRmssdRecordEntity>)

    @Query("SELECT * FROM heart_rate_variability_rmssd_records WHERE health_connect_uid = :hcUid LIMIT 1")
    suspend fun getRecordByHcUid(hcUid: String): HeartRateVariabilityRmssdRecordEntity?

    @Query("DELETE FROM heart_rate_variability_rmssd_records WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>): Int

    @Query("DELETE FROM heart_rate_variability_rmssd_records")
    suspend fun clearTable()

    /**
     * Observes all heart rate variability rmssd records from the table, ordered by time.
     * Useful for displaying data in the UI.
     */
    @Query("SELECT * FROM heart_rate_variability_rmssd_records ORDER BY time_epoch_millis DESC")
    fun getAllObservable(): Flow<List<HeartRateVariabilityRmssdRecordEntity>>
}
