package io.github.hitoshura25.healthsyncapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.RespiratoryRateRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RespiratoryRateRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<RespiratoryRateRecordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: RespiratoryRateRecordEntity)

    @Query("SELECT * FROM respiratory_rate_records ORDER BY time_epoch_millis DESC")
    fun getAllObservable(): Flow<List<RespiratoryRateRecordEntity>>

    @Query("SELECT * FROM respiratory_rate_records ORDER BY time_epoch_millis DESC LIMIT 1")
    suspend fun getLastRecord(): RespiratoryRateRecordEntity?

    @Query("SELECT * FROM respiratory_rate_records WHERE health_connect_uid = :hcUid LIMIT 1")
    suspend fun getRecordByHcUid(hcUid: String): RespiratoryRateRecordEntity?
}