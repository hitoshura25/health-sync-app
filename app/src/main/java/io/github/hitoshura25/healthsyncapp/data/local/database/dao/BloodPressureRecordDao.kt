package io.github.hitoshura25.healthsyncapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BloodPressureRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BloodPressureRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<BloodPressureRecordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: BloodPressureRecordEntity)

    @Query("SELECT * FROM blood_pressure_records ORDER BY timeEpochMillis DESC")
    fun getAllObservable(): Flow<List<BloodPressureRecordEntity>>

    @Query("SELECT * FROM blood_pressure_records ORDER BY timeEpochMillis DESC LIMIT 1")
    suspend fun getLastRecord(): BloodPressureRecordEntity?
}