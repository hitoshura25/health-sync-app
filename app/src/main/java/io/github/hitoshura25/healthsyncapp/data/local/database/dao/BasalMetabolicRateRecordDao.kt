package io.github.hitoshura25.healthsyncapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BasalMetabolicRateRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BasalMetabolicRateRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BasalMetabolicRateRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<BasalMetabolicRateRecordEntity>)

    @Query("SELECT * FROM basal_metabolic_rate_records ORDER BY time_epoch_millis DESC")
    fun getAllObservable(): Flow<List<BasalMetabolicRateRecordEntity>>

    @Query("SELECT * FROM basal_metabolic_rate_records WHERE health_connect_uid = :hcUid")
    suspend fun getRecordByHcUid(hcUid: String): BasalMetabolicRateRecordEntity?
}
