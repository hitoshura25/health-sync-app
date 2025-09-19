package io.github.hitoshura25.healthsyncapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.StepsCadenceRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.StepsCadenceRecordWithSamples
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.StepsCadenceSampleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StepsCadenceRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: StepsCadenceRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSamples(samples: List<StepsCadenceSampleEntity>)

    @Transaction
    suspend fun insertRecordWithSamples(record: StepsCadenceRecordEntity, samples: List<StepsCadenceSampleEntity>) {
        insert(record)
        insertAllSamples(samples)
    }

    @Transaction
    @Query("SELECT * FROM steps_cadence_records ORDER BY start_time_epoch_millis DESC")
    fun getAllObservable(): Flow<List<StepsCadenceRecordWithSamples>>
}
