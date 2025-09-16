package io.github.hitoshura25.healthsyncapp.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "blood_glucose_records",
    indices = [
        Index(value = ["health_connect_uid"], unique = true),
        Index(value = ["is_synced"]),
        Index(value = ["time_epoch_millis"]),
        Index(value = ["hc_last_modified_time_epoch_millis"]) 
    ]
)
data class BloodGlucoseEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "health_connect_uid")
    val hcUid: String, 

    @ColumnInfo(name = "time_epoch_millis")
    val timeEpochMillis: Long,

    @ColumnInfo(name = "zone_offset_id")
    val zoneOffsetId: String?,

    @ColumnInfo(name = "level_in_milligrams_per_deciliter") // Renamed from level_value, unit is implicit
    val levelInMilligramsPerDeciliter: Double,

    // levelUnit field removed

    @ColumnInfo(name = "specimen_source")
    val specimenSource: Int, 

    @ColumnInfo(name = "meal_type")
    val mealType: Int, 

    @ColumnInfo(name = "relation_to_meal")
    val relationToMeal: Int, 

    @ColumnInfo(name = "data_origin_package_name") 
    val dataOriginPackageName: String,

    @ColumnInfo(name = "hc_last_modified_time_epoch_millis") 
    val hcLastModifiedTimeEpochMillis: Long,

    @ColumnInfo(name = "client_record_id") 
    val clientRecordId: String?,

    @ColumnInfo(name = "client_record_version", defaultValue = "0") 
    val clientRecordVersion: Long = 0L,

    @ColumnInfo(name = "app_record_fetch_time_epoch_millis")
    val appRecordFetchTimeEpochMillis: Long,

    @ColumnInfo(name = "is_synced", defaultValue = "0")
    val isSynced: Boolean = false
)
