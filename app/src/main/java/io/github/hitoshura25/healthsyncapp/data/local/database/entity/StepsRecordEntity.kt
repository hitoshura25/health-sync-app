package io.github.hitoshura25.healthsyncapp.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "steps_records",
    indices = [
        Index(value = ["health_connect_uid"], unique = true), // To prevent duplicate entries from Health Connect
        Index(value = ["is_synced"])
    ]
)
data class StepsRecordEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "health_connect_uid")
    val hcUid: String, // From HealthConnectRecord.metadata.id

    @ColumnInfo(name = "count")
    val count: Long,

    @ColumnInfo(name = "start_time_epoch_millis")
    val startTimeEpochMillis: Long,

    @ColumnInfo(name = "end_time_epoch_millis")
    val endTimeEpochMillis: Long,

    @ColumnInfo(name = "zone_offset_id")
    val zoneOffsetId: String?, // Store ZoneOffset.id as String, nullable

    @ColumnInfo(name = "app_record_fetch_time_epoch_millis")
    val appRecordFetchTimeEpochMillis: Long,

    @ColumnInfo(name = "is_synced", defaultValue = "0") // SQLite stores Boolean as 0 (false) or 1 (true)
    val isSynced: Boolean = false
)
