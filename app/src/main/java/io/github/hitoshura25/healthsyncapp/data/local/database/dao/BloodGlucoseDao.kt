package io.github.hitoshura25.healthsyncapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BloodGlucoseEntity

@Dao
interface BloodGlucoseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BloodGlucoseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<BloodGlucoseEntity>)

    @Query("SELECT * FROM blood_glucose_records WHERE health_connect_uid = :hcUid LIMIT 1")
    suspend fun getRecordByHcUid(hcUid: String): BloodGlucoseEntity?

    @Query("SELECT * FROM blood_glucose_records WHERE is_synced = 0 ORDER BY time_epoch_millis ASC")
    suspend fun getUnsyncedRecords(): List<BloodGlucoseEntity>

    @Query("UPDATE blood_glucose_records SET is_synced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Long>): Int

    @Query("DELETE FROM blood_glucose_records WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>): Int

    @Query("DELETE FROM blood_glucose_records")
    suspend fun clearTable()
}
