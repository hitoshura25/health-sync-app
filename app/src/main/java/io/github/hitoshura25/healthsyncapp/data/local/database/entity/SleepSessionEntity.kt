package io.github.hitoshura25.healthsyncapp.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sleep_sessions",
    indices = [
        Index(value = ["health_connect_uid"], unique = true),
        // Index for is_synced removed
        Index(value = ["start_time_epoch_millis"])
    ]
)
data class SleepSessionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "health_connect_uid")
    val hcUid: String, // From HealthConnectRecord.metadata.id

    @ColumnInfo(name = "title")
    val title: String? = null,

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "start_time_epoch_millis")
    val startTimeEpochMillis: Long,

    @ColumnInfo(name = "start_zone_offset_id")
    val startZoneOffsetId: String?, // Store ZoneOffset.id as String, nullable

    @ColumnInfo(name = "end_time_epoch_millis")
    val endTimeEpochMillis: Long,

    @ColumnInfo(name = "end_zone_offset_id")
    val endZoneOffsetId: String?, // Store ZoneOffset.id as String, nullable

    @ColumnInfo(name = "duration_millis")
    val durationMillis: Long? = null, // Can be calculated: endTime - startTime

    @ColumnInfo(name = "app_record_fetch_time_epoch_millis")
    val appRecordFetchTimeEpochMillis: Long

    // isSynced property removed
)
