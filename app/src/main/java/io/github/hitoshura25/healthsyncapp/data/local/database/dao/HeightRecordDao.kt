package io.github.hitoshura25.healthsyncapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.HeightRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HeightRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<HeightRecordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: HeightRecordEntity)

    @Query("SELECT * FROM height_records ORDER BY time_epoch_millis DESC")
    fun getAllObservable(): Flow<List<HeightRecordEntity>>

    @Query("SELECT * FROM height_records ORDER BY time_epoch_millis DESC LIMIT 1")
    suspend fun getLastRecord(): HeightRecordEntity?

    @Query("SELECT * FROM height_records WHERE health_connect_uid = :hcUid LIMIT 1")
    suspend fun getRecordByHcUid(hcUid: String): HeightRecordEntity?
}