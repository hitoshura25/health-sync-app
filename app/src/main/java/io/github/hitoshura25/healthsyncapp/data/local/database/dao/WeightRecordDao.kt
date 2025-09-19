package io.github.hitoshura25.healthsyncapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.WeightRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WeightRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<WeightRecordEntity>)

    @Query("SELECT * FROM weight_records ORDER BY time_epoch_millis DESC")
    fun getAllObservable(): Flow<List<WeightRecordEntity>>

    @Query("SELECT * FROM weight_records WHERE health_connect_uid = :hcUid")
    suspend fun getRecordByHcUid(hcUid: String): WeightRecordEntity?
}
