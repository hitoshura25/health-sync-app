package io.github.hitoshura25.healthsyncapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BasalBodyTemperatureRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BasalBodyTemperatureRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BasalBodyTemperatureRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<BasalBodyTemperatureRecordEntity>)

    @Query("SELECT * FROM basal_body_temperature_records ORDER BY time_epoch_millis DESC")
    fun getAllObservable(): Flow<List<BasalBodyTemperatureRecordEntity>>

    @Query("SELECT * FROM basal_body_temperature_records WHERE health_connect_uid = :hcUid")
    suspend fun getRecordByHcUid(hcUid: String): BasalBodyTemperatureRecordEntity?
}
