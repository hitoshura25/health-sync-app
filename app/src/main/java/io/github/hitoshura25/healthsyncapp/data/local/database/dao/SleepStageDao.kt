package io.github.hitoshura25.healthsyncapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.SleepStageEntity

@Dao
interface SleepStageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stages: List<SleepStageEntity>)

    @Query("SELECT * FROM sleep_stages WHERE session_hc_uid = :sessionHcUid ORDER BY start_time_epoch_millis ASC")
    suspend fun getStagesBySessionHcUid(sessionHcUid: String): List<SleepStageEntity>

    // Example delete (can be added if needed later)
    // @Query("DELETE FROM sleep_stages WHERE session_hc_uid = :sessionHcUid")
    // suspend fun deleteStagesForSession(sessionHcUid: String)
}
