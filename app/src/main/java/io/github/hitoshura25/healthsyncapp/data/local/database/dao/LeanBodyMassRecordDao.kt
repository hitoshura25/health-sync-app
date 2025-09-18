package io.github.hitoshura25.healthsyncapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.LeanBodyMassRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LeanBodyMassRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<LeanBodyMassRecordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: LeanBodyMassRecordEntity)

    @Query("SELECT * FROM lean_body_mass_records ORDER BY timeEpochMillis DESC")
    fun getAllObservable(): Flow<List<LeanBodyMassRecordEntity>>

    @Query("SELECT * FROM lean_body_mass_records ORDER BY timeEpochMillis DESC LIMIT 1")
    suspend fun getLastRecord(): LeanBodyMassRecordEntity?
}