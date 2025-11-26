package io.github.hitoshura25.healthsyncapp.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nutrition_records")
data class NutritionRecordEntity(
    @PrimaryKey
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

    @ColumnInfo(name = "name")
    val name: String?,

    @ColumnInfo(name = "calories")
    val calories: Double?,

    @ColumnInfo(name = "carbohydrates")
    val carbohydrates: Double?,

    @ColumnInfo(name = "protein")
    val protein: Double?,

    @ColumnInfo(name = "total_fat")
    val totalFat: Double?,

    @ColumnInfo(name = "saturated_fat")
    val saturatedFat: Double?,

    @ColumnInfo(name = "unsaturated_fat")
    val unsaturatedFat: Double?,

    @ColumnInfo(name = "trans_fat")
    val transFat: Double?,

    @ColumnInfo(name = "sodium")
    val sodium: Double?,

    @ColumnInfo(name = "potassium")
    val potassium: Double?,

    @ColumnInfo(name = "cholesterol")
    val cholesterol: Double?,

    @ColumnInfo(name = "fiber")
    val fiber: Double?,

    @ColumnInfo(name = "sugar")
    val sugar: Double?,

    @ColumnInfo(name = "vitamin_c")
    val vitaminC: Double?,

    @ColumnInfo(name = "vitamin_d")
    val vitaminD: Double?,

    @ColumnInfo(name = "calcium")
    val calcium: Double?,

    @ColumnInfo(name = "iron")
    val iron: Double?,

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