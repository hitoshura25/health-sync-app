package io.github.hitoshura25.healthsyncapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BodyFatRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyFatRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BodyFatRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<BodyFatRecordEntity>)

    @Query("SELECT * FROM body_fat_records WHERE health_connect_uid = :hcUid LIMIT 1")
    suspend fun getRecordByHcUid(hcUid: String): BodyFatRecordEntity?

    @Query("DELETE FROM body_fat_records WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>): Int

    @Query("DELETE FROM body_fat_records")
    suspend fun clearTable()

    /**
     * Observes all body fat records from the table, ordered by time.
     * Useful for displaying data in the UI.
     */
    @Query("SELECT * FROM body_fat_records ORDER BY time_epoch_millis DESC")
    fun getAllObservable(): Flow<List<BodyFatRecordEntity>>
}
