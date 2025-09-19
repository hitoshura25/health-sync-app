package io.github.hitoshura25.healthsyncapp.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sleep_sessions",
    indices = [
        Index(value = ["health_connect_uid"], unique = true),
        Index(value = ["start_time_epoch_millis"])
    ]
)
data class SleepSessionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "health_connect_uid")
    val hcUid: String,

    @ColumnInfo(name = "title")
    val title: String? = null,

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "start_time_epoch_millis")
    val startTimeEpochMillis: Long,

    @ColumnInfo(name = "start_zone_offset_id")
    val startZoneOffsetId: String?,

    @ColumnInfo(name = "end_time_epoch_millis")
    val endTimeEpochMillis: Long,

    @ColumnInfo(name = "end_zone_offset_id")
    val endZoneOffsetId: String?,

    @ColumnInfo(name = "duration_millis")
    val durationMillis: Long? = null,

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
