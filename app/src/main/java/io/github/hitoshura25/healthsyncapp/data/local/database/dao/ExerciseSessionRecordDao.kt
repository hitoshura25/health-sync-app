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

    @Query("SELECT * FROM exercise_session_records ORDER BY startTimeEpochMillis DESC")
    fun getAllObservable(): Flow<List<ExerciseSessionRecordEntity>>

    @Query("SELECT * FROM exercise_session_records ORDER BY startTimeEpochMillis DESC LIMIT 1")
    suspend fun getLastRecord(): ExerciseSessionRecordEntity?
}