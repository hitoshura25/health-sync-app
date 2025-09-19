package io.github.hitoshura25.healthsyncapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.ActiveCaloriesBurnedRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActiveCaloriesBurnedRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ActiveCaloriesBurnedRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ActiveCaloriesBurnedRecordEntity>)

    @Query("SELECT * FROM active_calories_burned_records ORDER BY start_time_epoch_millis DESC")
    fun getAllObservable(): Flow<List<ActiveCaloriesBurnedRecordEntity>>

    @Query("SELECT * FROM active_calories_burned_records WHERE health_connect_uid = :hcUid")
    suspend fun getRecordByHcUid(hcUid: String): ActiveCaloriesBurnedRecordEntity?
}
