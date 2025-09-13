package io.github.hitoshura25.healthsyncapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.StepsRecordEntity

@Dao
interface StepsRecordDao {

    /**
     * Inserts a single steps record. If a record with the same `health_connect_uid` already exists,
     * it will be replaced. Consider if REPLAcE is the desired strategy or if IGNORE or an update
     * would be more appropriate depending on how you handle potential updates from Health Connect.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: StepsRecordEntity): Long

    /**
     * Inserts a list of steps records. Uses REPLACE strategy for conflicts by `health_connect_uid`.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<StepsRecordEntity>)

    /**
     * Retrieves a steps record by its Health Connect Unique ID.
     * Useful for checking if a record fetched from Health Connect already exists in the local DB.
     */
    @Query("SELECT * FROM steps_records WHERE health_connect_uid = :hcUid LIMIT 1")
    suspend fun getRecordByHcUid(hcUid: String): StepsRecordEntity?

    /**
     * Retrieves all steps records that have not yet been synced.
     */
    @Query("SELECT * FROM steps_records WHERE is_synced = 0 ORDER BY start_time_epoch_millis ASC")
    suspend fun getUnsyncedSteps(): List<StepsRecordEntity>

    /**
     * Marks a list of steps records as synced by their primary keys (id).
     * @param ids The list of primary keys of the records to mark as synced.
     * @return The number of rows updated.
     */
    @Query("UPDATE steps_records SET is_synced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Long>): Int

    /**
     * Deletes records by their primary keys (id).
     * Could be used after successful sync and if local persistence is not needed long-term for synced items.
     */
    @Query("DELETE FROM steps_records WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>): Int

    /**
     * Deletes all records from the table. Use with caution.
     */
    @Query("DELETE FROM steps_records")
    suspend fun clearTable()
}
