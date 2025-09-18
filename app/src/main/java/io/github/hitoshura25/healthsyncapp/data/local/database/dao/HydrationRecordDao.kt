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

    @Query("SELECT * FROM hydration_records ORDER BY startTimeEpochMillis DESC")
    fun getAllObservable(): Flow<List<HydrationRecordEntity>>

    @Query("SELECT * FROM hydration_records ORDER BY startTimeEpochMillis DESC LIMIT 1")
    suspend fun getLastRecord(): HydrationRecordEntity?
}