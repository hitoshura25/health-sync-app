package io.github.hitoshura25.healthsyncapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BodyWaterMassRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyWaterMassRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<BodyWaterMassRecordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: BodyWaterMassRecordEntity)

    @Query("SELECT * FROM body_water_mass_records ORDER BY timeEpochMillis DESC")
    fun getAllObservable(): Flow<List<BodyWaterMassRecordEntity>>

    @Query("SELECT * FROM body_water_mass_records ORDER BY timeEpochMillis DESC LIMIT 1")
    suspend fun getLastRecord(): BodyWaterMassRecordEntity?
}