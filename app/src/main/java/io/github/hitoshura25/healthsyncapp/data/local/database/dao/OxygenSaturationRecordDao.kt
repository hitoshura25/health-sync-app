package io.github.hitoshura25.healthsyncapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.OxygenSaturationRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OxygenSaturationRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<OxygenSaturationRecordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: OxygenSaturationRecordEntity)

    @Query("SELECT * FROM oxygen_saturation_records ORDER BY timeEpochMillis DESC")
    fun getAllObservable(): Flow<List<OxygenSaturationRecordEntity>>

    @Query("SELECT * FROM oxygen_saturation_records ORDER BY timeEpochMillis DESC LIMIT 1")
    suspend fun getLastRecord(): OxygenSaturationRecordEntity?
}