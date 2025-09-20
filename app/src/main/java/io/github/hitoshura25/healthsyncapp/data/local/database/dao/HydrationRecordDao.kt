package io.github.hitoshura25.healthsyncapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.HydrationRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HydrationRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<HydrationRecordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: HydrationRecordEntity)

    @Query("SELECT * FROM hydration_records ORDER BY start_time_epoch_millis DESC")
    fun getAllObservable(): Flow<List<HydrationRecordEntity>>

    @Query("SELECT * FROM hydration_records ORDER BY start_time_epoch_millis DESC LIMIT 1")
    suspend fun getLastRecord(): HydrationRecordEntity?

    @Query("SELECT * FROM hydration_records WHERE health_connect_uid = :hcUid LIMIT 1")
    suspend fun getRecordByHcUid(hcUid: String): HydrationRecordEntity?
}