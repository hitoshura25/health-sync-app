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
        Index(value = ["time_epoch_millis"])
    ]
)
data class BloodGlucoseEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "health_connect_uid")
    val hcUid: String, // From HealthConnectRecord.metadata.id

    @ColumnInfo(name = "time_epoch_millis")
    val timeEpochMillis: Long,

    @ColumnInfo(name = "zone_offset_id")
    val zoneOffsetId: String?, // Store ZoneOffset.id as String, nullable

    @ColumnInfo(name = "level_mg_dl")
    val levelMgdL: Double, // Blood glucose level in mg/dL

    @ColumnInfo(name = "specimen_source")
    val specimenSource: Int,

    @ColumnInfo(name = "meal_type")
    val mealType: Int,

    @ColumnInfo(name = "relation_to_meal")
    val relationToMeal: Int,

    @ColumnInfo(name = "app_record_fetch_time_epoch_millis")
    val appRecordFetchTimeEpochMillis: Long,

    @ColumnInfo(name = "is_synced", defaultValue = "0")
    val isSynced: Boolean = false
)
