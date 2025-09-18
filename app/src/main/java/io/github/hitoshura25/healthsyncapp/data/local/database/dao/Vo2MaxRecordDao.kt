package io.github.hitoshura25.healthsyncapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.Vo2MaxRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface Vo2MaxRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<Vo2MaxRecordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: Vo2MaxRecordEntity): Long

    @Query("SELECT * FROM vo2_max_records WHERE health_connect_uid = :hcUid LIMIT 1")
    suspend fun getRecordByHcUid(hcUid: String): Vo2MaxRecordEntity?

    @Query("DELETE FROM vo2_max_records")
    suspend fun clearTable()

    /**
     * Observes all VO2 max records from the table, ordered by time.
     * Useful for displaying data in the UI.
     */
    @Query("SELECT * FROM vo2_max_records ORDER BY time_epoch_millis DESC")
    fun getAllObservable(): Flow<List<Vo2MaxRecordEntity>>
}
