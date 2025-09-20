package io.github.hitoshura25.healthsyncapp.data.avro

import kotlinx.serialization.Serializable

@Serializable
data class AvroNutritionRecord(
    val metadata: AvroMetadata,
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long,
    val startZoneOffsetId: String? = null,
    val endZoneOffsetId: String? = null,
    val name: String? = null,
    val calories: Double? = null,
    val carbohydrates: Double? = null,
    val protein: Double? = null,
    val totalFat: Double? = null,
    val saturatedFat: Double? = null,
    val unsaturatedFat: Double? = null,
    val transFat: Double? = null,
    val sodium: Double? = null,
    val potassium: Double? = null,
    val cholesterol: Double? = null,
    val fiber: Double? = null,
    val sugar: Double? = null,
    val vitaminC: Double? = null,
    val vitaminD: Double? = null,
    val calcium: Double? = null,
    val iron: Double? = null,
    val appRecordFetchTimeEpochMillis: Long
)