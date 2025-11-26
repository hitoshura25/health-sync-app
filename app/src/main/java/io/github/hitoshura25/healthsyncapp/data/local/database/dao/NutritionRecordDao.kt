package io.github.hitoshura25.healthsyncapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.NutritionRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NutritionRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<NutritionRecordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: NutritionRecordEntity)

    @Query("SELECT * FROM nutrition_records ORDER BY start_time_epoch_millis DESC")
    fun getAllObservable(): Flow<List<NutritionRecordEntity>>

    @Query("SELECT * FROM nutrition_records ORDER BY start_time_epoch_millis DESC LIMIT 1")
    suspend fun getLastRecord(): NutritionRecordEntity?

    @Query("SELECT * FROM nutrition_records WHERE health_connect_uid = :hcUid LIMIT 1")
    suspend fun getRecordByHcUid(hcUid: String): NutritionRecordEntity?
}