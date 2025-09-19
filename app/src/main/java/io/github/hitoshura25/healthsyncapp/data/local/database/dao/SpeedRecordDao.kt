package io.github.hitoshura25.healthsyncapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.SpeedRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.SpeedRecordWithSamples
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.SpeedSampleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpeedRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: SpeedRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSamples(samples: List<SpeedSampleEntity>)

    @Transaction
    suspend fun insertRecordWithSamples(record: SpeedRecordEntity, samples: List<SpeedSampleEntity>) {
        insert(record)
        insertAllSamples(samples)
    }

    @Transaction
    @Query("SELECT * FROM speed_records ORDER BY start_time_epoch_millis DESC")
    fun getAllObservable(): Flow<List<SpeedRecordWithSamples>>
}
