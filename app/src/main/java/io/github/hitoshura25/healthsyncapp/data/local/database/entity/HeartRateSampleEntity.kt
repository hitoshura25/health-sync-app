package io.github.hitoshura25.healthsyncapp.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "heart_rate_samples",
    indices = [
        Index(value = ["sample_time_epoch_millis"])
    ]
)
data class HeartRateSampleEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "hc_record_uid")
    val hcRecordUid: String,

    @ColumnInfo(name = "sample_time_epoch_millis")
    val sampleTimeEpochMillis: Long,

    @ColumnInfo(name = "beats_per_minute")
    val beatsPerMinute: Long,

    @ColumnInfo(name = "zone_offset_id")
    val zoneOffsetId: String? = null,

    @ColumnInfo(name = "app_record_fetch_time_epoch_millis")
    val appRecordFetchTimeEpochMillis: Long,

    @ColumnInfo(name = "data_origin_package_name")
    val dataOriginPackageName: String,

    @ColumnInfo(name = "hc_last_modified_time_epoch_millis")
    val hcLastModifiedTimeEpochMillis: Long,

    @ColumnInfo(name = "client_record_id")
    val clientRecordId: String?,

    @ColumnInfo(name = "client_record_version")
    val clientRecordVersion: Long,

    @ColumnInfo(name = "device_manufacturer")
    val deviceManufacturer: String?,

    @ColumnInfo(name = "device_model")
    val deviceModel: String?,

    @ColumnInfo(name = "device_type")
    val deviceType: String?
)
