package io.github.hitoshura25.healthsyncapp.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nutrition_records")
data class NutritionRecordEntity(
    @PrimaryKey
    val hcUid: String,
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long,
    val startZoneOffsetId: String?,
    val endZoneOffsetId: String?,
    val name: String?,
    val calories: Double?,
    val carbohydrates: Double?,
    val protein: Double?,
    val totalFat: Double?,
    val saturatedFat: Double?,
    val unsaturatedFat: Double?,
    val transFat: Double?,
    val sodium: Double?,
    val potassium: Double?,
    val cholesterol: Double?,
    val fiber: Double?,
    val sugar: Double?,
    val vitaminC: Double?,
    val vitaminD: Double?,
    val calcium: Double?,
    val iron: Double?,
    val appRecordFetchTimeEpochMillis: Long,
    val dataOriginPackageName: String,
    val hcLastModifiedTimeEpochMillis: Long,
    val clientRecordId: String?,
    val clientRecordVersion: Long,
    val deviceManufacturer: String?,
    val deviceModel: String?,
    val deviceType: String?
)