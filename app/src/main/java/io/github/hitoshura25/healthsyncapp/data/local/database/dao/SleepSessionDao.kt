package io.github.hitoshura25.healthsyncapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.SleepSessionEntity

@Dao
interface SleepSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SleepSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<SleepSessionEntity>)

    @Query("SELECT * FROM sleep_sessions WHERE health_connect_uid = :hcUid LIMIT 1")
    suspend fun getRecordByHcUid(hcUid: String): SleepSessionEntity?

    @Query("SELECT * FROM sleep_sessions WHERE is_synced = 0 ORDER BY start_time_epoch_millis ASC")
    suspend fun getUnsyncedSessions(): List<SleepSessionEntity>

    @Query("UPDATE sleep_sessions SET is_synced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Long>): Int

    @Query("DELETE FROM sleep_sessions WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>): Int

    @Query("DELETE FROM sleep_sessions")
    suspend fun clearTable()
}
