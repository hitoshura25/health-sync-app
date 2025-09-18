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

    @Query("SELECT * FROM respiratory_rate_records ORDER BY timeEpochMillis DESC")
    fun getAllObservable(): Flow<List<RespiratoryRateRecordEntity>>

    @Query("SELECT * FROM respiratory_rate_records ORDER BY timeEpochMillis DESC LIMIT 1")
    suspend fun getLastRecord(): RespiratoryRateRecordEntity?
}