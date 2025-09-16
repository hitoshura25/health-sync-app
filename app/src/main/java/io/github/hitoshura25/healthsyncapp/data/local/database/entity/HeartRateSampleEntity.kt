package io.github.hitoshura25.healthsyncapp.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "heart_rate_samples",
    indices = [
        // Index for is_synced removed
        Index(value = ["sample_time_epoch_millis"])
    ]
)
data class HeartRateSampleEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "hc_record_uid") // From the parent HealthConnectRecord.metadata.id
    val hcRecordUid: String,

    @ColumnInfo(name = "sample_time_epoch_millis")
    val sampleTimeEpochMillis: Long,

    @ColumnInfo(name = "beats_per_minute")
    val beatsPerMinute: Long,

    @ColumnInfo(name = "zone_offset_id")
    val zoneOffsetId: String? = null,

    @ColumnInfo(name = "app_record_fetch_time_epoch_millis")
    val appRecordFetchTimeEpochMillis: Long

    // isSynced property removed
)
