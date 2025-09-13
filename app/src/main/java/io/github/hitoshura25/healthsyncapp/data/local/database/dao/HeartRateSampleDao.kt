package io.github.hitoshura25.healthsyncapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.HeartRateSampleEntity

@Dao
interface HeartRateSampleDao {

    /**
     * Inserts a single heart rate sample. If a conflict occurs with the primary key (which is auto-generated
     * and thus unlikely unless IDs are manually set), this strategy would matter more.
     * Given no unique constraints on data fields, IGNORE is safe for auto-generated PKs.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE) // Or .NONE if no specific conflict on data columns is expected
    suspend fun insert(entity: HeartRateSampleEntity): Long

    /**
     * Inserts a list of heart rate samples.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entities: List<HeartRateSampleEntity>)

    /**
     * Retrieves all heart rate samples that have not yet been synced.
     */
    @Query("SELECT * FROM heart_rate_samples WHERE is_synced = 0 ORDER BY sample_time_epoch_millis ASC")
    suspend fun getUnsyncedSamples(): List<HeartRateSampleEntity>

    /**
     * Marks a list of heart rate samples as synced by their primary keys (id).
     * @param ids The list of primary keys of the records to mark as synced.
     * @return The number of rows updated.
     */
    @Query("UPDATE heart_rate_samples SET is_synced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Long>): Int

    /**
     * Deletes records by their primary keys (id).
     */
    @Query("DELETE FROM heart_rate_samples WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>): Int

    /**
     * Deletes all records from the table. Use with caution.
     */
    @Query("DELETE FROM heart_rate_samples")
    suspend fun clearTable()
}
