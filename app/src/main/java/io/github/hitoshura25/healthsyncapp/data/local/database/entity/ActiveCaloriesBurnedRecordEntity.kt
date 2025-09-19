package io.github.hitoshura25.healthsyncapp.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "active_calories_burned_records",
    indices = [
        Index(value = ["health_connect_uid"], unique = true)
    ]
)
data class ActiveCaloriesBurnedRecordEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "health_connect_uid")
    val hcUid: String,

    @ColumnInfo(name = "start_time_epoch_millis")
    val startTimeEpochMillis: Long,

    @ColumnInfo(name = "end_time_epoch_millis")
    val endTimeEpochMillis: Long,

    @ColumnInfo(name = "start_zone_offset_id")
    val startZoneOffsetId: String?,

    @ColumnInfo(name = "end_zone_offset_id")
    val endZoneOffsetId: String?,

    @ColumnInfo(name = "energy_in_kilocalories")
    val energyInKilocalories: Double,

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
