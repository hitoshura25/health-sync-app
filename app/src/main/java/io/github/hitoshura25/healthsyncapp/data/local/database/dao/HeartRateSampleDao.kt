package io.github.hitoshura25.healthsyncapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.HeartRateSampleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HeartRateSampleDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: HeartRateSampleEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entities: List<HeartRateSampleEntity>)

    // Corrected column name in the query to hc_record_uid
    @Query("SELECT * FROM heart_rate_samples WHERE hc_record_uid = :hcRecordUid ORDER BY sample_time_epoch_millis ASC")
    suspend fun getSamplesByRecordHcUid(hcRecordUid: String): List<HeartRateSampleEntity>

    // getUnsyncedSamples() removed

    // markAsSynced(ids: List<Long>) removed

    @Query("DELETE FROM heart_rate_samples WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>): Int

    @Query("DELETE FROM heart_rate_samples")
    suspend fun clearTable()

    /**
     * Observes all heart rate samples from the table, ordered by sample time.
     */
    @Query("SELECT * FROM heart_rate_samples ORDER BY sample_time_epoch_millis DESC")
    fun getAllObservable(): Flow<List<HeartRateSampleEntity>>
}
