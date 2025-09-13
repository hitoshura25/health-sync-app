package io.github.hitoshura25.healthsyncapp.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "heart_rate_samples",
    indices = [
        // Index to potentially speed up queries for unsynced records or records by fetch time.
        // A unique index on (hc_record_uid, sample_time_epoch_millis) could be an option
        // if you want to strictly prevent duplicate samples for the same original record and time,
        // but simple primary key `id` is often sufficient if upstream data is trusted.
        Index(value = ["is_synced"]),
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

    // ZoneOffset might not be directly available per sample, but from the parent record.
    // If consistently available from parent, store it. Otherwise, can be nullable or omitted if not needed.
    // For simplicity, let's assume we can get it from the parent HeartRateRecord's metadata or time range.
    // However, HeartRateRecord itself does not have a direct zoneOffset, samples have Instant.
    // We'll make it nullable for now, as it might be complex to derive reliably for every sample.
    @ColumnInfo(name = "zone_offset_id")
    val zoneOffsetId: String? = null,

    @ColumnInfo(name = "app_record_fetch_time_epoch_millis")
    val appRecordFetchTimeEpochMillis: Long,

    @ColumnInfo(name = "is_synced", defaultValue = "0")
    val isSynced: Boolean = false
)
