package io.github.hitoshura25.healthsyncapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BodyTemperatureRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyTemperatureRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<BodyTemperatureRecordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: BodyTemperatureRecordEntity)

    @Query("SELECT * FROM body_temperature_records ORDER BY time_epoch_millis DESC")
    fun getAllObservable(): Flow<List<BodyTemperatureRecordEntity>>

    @Query("SELECT * FROM body_temperature_records ORDER BY time_epoch_millis DESC LIMIT 1")
    suspend fun getLastRecord(): BodyTemperatureRecordEntity?

    @Query("SELECT * FROM body_temperature_records WHERE health_connect_uid = :hcUid LIMIT 1")
    suspend fun getRecordByHcUid(hcUid: String): BodyTemperatureRecordEntity?
}