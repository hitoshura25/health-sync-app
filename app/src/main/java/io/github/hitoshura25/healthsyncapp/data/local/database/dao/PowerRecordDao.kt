package io.github.hitoshura25.healthsyncapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.PowerRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.PowerRecordWithSamples
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.PowerSampleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PowerRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: PowerRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSamples(samples: List<PowerSampleEntity>)

    @Transaction
    suspend fun insertRecordWithSamples(record: PowerRecordEntity, samples: List<PowerSampleEntity>) {
        insert(record)
        insertAllSamples(samples)
    }

    @Transaction
    @Query("SELECT * FROM power_records ORDER BY start_time_epoch_millis DESC")
    fun getAllObservable(): Flow<List<PowerRecordWithSamples>>
}