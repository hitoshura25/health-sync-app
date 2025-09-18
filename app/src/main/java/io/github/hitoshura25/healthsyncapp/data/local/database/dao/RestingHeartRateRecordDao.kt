package io.github.hitoshura25.healthsyncapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.RestingHeartRateRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RestingHeartRateRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RestingHeartRateRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<RestingHeartRateRecordEntity>)

    @Query("SELECT * FROM resting_heart_rate_records WHERE health_connect_uid = :hcUid LIMIT 1")
    suspend fun getRecordByHcUid(hcUid: String): RestingHeartRateRecordEntity?

    @Query("DELETE FROM resting_heart_rate_records WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>): Int

    @Query("DELETE FROM resting_heart_rate_records")
    suspend fun clearTable()

    /**
     * Observes all resting heart rate records from the table, ordered by time.
     * Useful for displaying data in the UI.
     */
    @Query("SELECT * FROM resting_heart_rate_records ORDER BY time_epoch_millis DESC")
    fun getAllObservable(): Flow<List<RestingHeartRateRecordEntity>>
}
