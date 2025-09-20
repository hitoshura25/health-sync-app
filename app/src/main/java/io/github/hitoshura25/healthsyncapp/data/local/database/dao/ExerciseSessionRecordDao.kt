package io.github.hitoshura25.healthsyncapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.ExerciseSessionRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseSessionRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<ExerciseSessionRecordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: ExerciseSessionRecordEntity)

    @Query("SELECT * FROM exercise_session_records ORDER BY start_time_epoch_millis DESC")
    fun getAllObservable(): Flow<List<ExerciseSessionRecordEntity>>

    @Query("SELECT * FROM exercise_session_records ORDER BY start_time_epoch_millis DESC LIMIT 1")
    suspend fun getLastRecord(): ExerciseSessionRecordEntity?

    @Query("SELECT * FROM exercise_session_records WHERE health_connect_uid = :hcUid LIMIT 1")
    suspend fun getRecordByHcUid(hcUid: String): ExerciseSessionRecordEntity?
}