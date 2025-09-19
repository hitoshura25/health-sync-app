package io.github.hitoshura25.healthsyncapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.CyclingPedalingCadenceRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.CyclingPedalingCadenceRecordWithSamples
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.CyclingPedalingCadenceSampleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CyclingPedalingCadenceRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: CyclingPedalingCadenceRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSamples(samples: List<CyclingPedalingCadenceSampleEntity>)

    @Transaction
    suspend fun insertRecordWithSamples(record: CyclingPedalingCadenceRecordEntity, samples: List<CyclingPedalingCadenceSampleEntity>) {
        insert(record)
        insertAllSamples(samples)
    }

    @Transaction
    @Query("SELECT * FROM cycling_pedaling_cadence_records ORDER BY startTimeEpochMillis DESC")
    fun getAllObservable(): Flow<List<CyclingPedalingCadenceRecordWithSamples>>

    @Query("SELECT * FROM cycling_pedaling_cadence_records ORDER BY startTimeEpochMillis DESC LIMIT :limit")
    suspend fun getLatest(limit: Int): List<CyclingPedalingCadenceRecordEntity>

    @Query("DELETE FROM cycling_pedaling_cadence_records")
    suspend fun deleteAll()
}