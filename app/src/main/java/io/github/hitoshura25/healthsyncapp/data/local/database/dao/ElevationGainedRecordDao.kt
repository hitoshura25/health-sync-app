package io.github.hitoshura25.healthsyncapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.ElevationGainedRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ElevationGainedRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<ElevationGainedRecordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: ElevationGainedRecordEntity)

    @Query("SELECT * FROM elevation_gained_records ORDER BY startTimeEpochMillis DESC")
    fun getAllObservable(): Flow<List<ElevationGainedRecordEntity>>

    @Query("SELECT * FROM elevation_gained_records ORDER BY startTimeEpochMillis DESC LIMIT 1")
    suspend fun getLastRecord(): ElevationGainedRecordEntity?
}