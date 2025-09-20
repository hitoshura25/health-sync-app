package io.github.hitoshura25.healthsyncapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BoneMassRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BoneMassRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<BoneMassRecordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: BoneMassRecordEntity)

    @Query("SELECT * FROM bone_mass_records ORDER BY time_epoch_millis DESC")
    fun getAllObservable(): Flow<List<BoneMassRecordEntity>>

    @Query("SELECT * FROM bone_mass_records ORDER BY time_epoch_millis DESC LIMIT 1")
    suspend fun getLastRecord(): BoneMassRecordEntity?

    @Query("SELECT * FROM bone_mass_records WHERE health_connect_uid = :hcUid LIMIT 1")
    suspend fun getRecordByHcUid(hcUid: String): BoneMassRecordEntity?
}