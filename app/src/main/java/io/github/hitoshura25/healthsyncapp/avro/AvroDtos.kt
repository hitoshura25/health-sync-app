package io.github.hitoshura25.healthsyncapp.avro

import kotlinx.serialization.Serializable

/**
 * Data Transfer Objects for Avro serialization.
 */

@Serializable
data class AvroStepsRecord(
    val hcUid: String,
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long,
    val startZoneOffsetId: String? = null,
    val endZoneOffsetId: String? = null,
    val count: Long,
    val dataOriginPackageName: String,
    val hcLastModifiedTimeEpochMillis: Long,
    val clientRecordId: String? = null,
    val clientRecordVersion: Long = 0L, // Consistent with Avro schema default
    val appRecordFetchTimeEpochMillis: Long
)

@Serializable
data class AvroHeartRateSample(
    val timeEpochMillis: Long,
    val beatsPerMinute: Long
)

@Serializable
data class AvroHeartRateRecord(
    val hcUid: String,
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long,
    val startZoneOffsetId: String? = null,
    val endZoneOffsetId: String? = null,
    val dataOriginPackageName: String,
    val hcLastModifiedTimeEpochMillis: Long,
    val clientRecordId: String? = null,
    val clientRecordVersion: Long = 0L,
    val appRecordFetchTimeEpochMillis: Long,
    val samples: List<AvroHeartRateSample>
)

@Serializable
enum class AvroSleepStageType {
    UNKNOWN,
    AWAKE,
    SLEEPING,
    OUT_OF_BED,
    LIGHT,
    DEEP,
    REM
}

@Serializable
data class AvroSleepStageRecord(
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long,
    val stage: AvroSleepStageType
)

@Serializable
data class AvroSleepSessionRecord(
    val hcUid: String,
    val title: String? = null,
    val notes: String? = null,
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long,
    val startZoneOffsetId: String? = null,
    val endZoneOffsetId: String? = null,
    val durationMillis: Long? = null,
    val dataOriginPackageName: String,
    val hcLastModifiedTimeEpochMillis: Long,
    val clientRecordId: String? = null,
    val clientRecordVersion: Long = 0L,
    val appRecordFetchTimeEpochMillis: Long,
    val stages: List<AvroSleepStageRecord> = emptyList()
)

// --- Appended Blood Glucose Avro DTOs and Enums ---

@Serializable
enum class AvroBloodGlucoseLevelUnit {
    MILLIGRAMS_PER_DECILITER, // Corresponds to BloodGlucose.UNIT_MILLIGRAMS_PER_DECILITER
    MILLIMOLES_PER_LITER,     // Corresponds to BloodGlucose.UNIT_MILLIMOLES_PER_LITER
    UNKNOWN
}

@Serializable
enum class AvroSpecimenSource {
    INTERSTITIAL_FLUID,       // Corresponds to BloodGlucoseRecord.SPECIMEN_SOURCE_INTERSTITIAL_FLUID
    CAPILLARY_BLOOD,          // Corresponds to BloodGlucoseRecord.SPECIMEN_SOURCE_CAPILLARY_BLOOD
    PLASMA,                   // Corresponds to BloodGlucoseRecord.SPECIMEN_SOURCE_PLASMA
    SERUM,                    // Corresponds to BloodGlucoseRecord.SPECIMEN_SOURCE_SERUM
    TEARS,                    // Corresponds to BloodGlucoseRecord.SPECIMEN_SOURCE_TEARS
    WHOLE_BLOOD,              // Corresponds to BloodGlucoseRecord.SPECIMEN_SOURCE_WHOLE_BLOOD
    UNKNOWN
}

@Serializable
enum class AvroMealType {
    UNKNOWN,                  // Corresponds to BloodGlucoseRecord.MEAL_TYPE_UNKNOWN
    BREAKFAST,                // Corresponds to BloodGlucoseRecord.MEAL_TYPE_BREAKFAST
    LUNCH,                    // Corresponds to BloodGlucoseRecord.MEAL_TYPE_LUNCH
    DINNER,                   // Corresponds to BloodGlucoseRecord.MEAL_TYPE_DINNER
    SNACK                     // Corresponds to BloodGlucoseRecord.MEAL_TYPE_SNACK
}

@Serializable
enum class AvroRelationToMeal {
    UNKNOWN,                  // Corresponds to BloodGlucoseRecord.RELATION_TO_MEAL_UNKNOWN
    GENERAL,                  // Corresponds to BloodGlucoseRecord.RELATION_TO_MEAL_GENERAL
    FASTING,                  // Corresponds to BloodGlucoseRecord.RELATION_TO_MEAL_FASTING
    BEFORE_MEAL,              // Corresponds to BloodGlucoseRecord.RELATION_TO_MEAL_BEFORE_MEAL
    AFTER_MEAL                // Corresponds to BloodGlucoseRecord.RELATION_TO_MEAL_AFTER_MEAL
}

@Serializable
data class AvroBloodGlucoseRecord(
    val hcUid: String,
    val timeEpochMillis: Long,
    val zoneOffsetId: String? = null,
    val levelValue: Double, // From BloodGlucose.value
    val levelUnit: AvroBloodGlucoseLevelUnit, // From BloodGlucose.unit
    val specimenSource: AvroSpecimenSource,
    val mealType: AvroMealType,
    val relationToMeal: AvroRelationToMeal,
    val dataOriginPackageName: String,
    val hcLastModifiedTimeEpochMillis: Long,
    val clientRecordId: String? = null,
    val clientRecordVersion: Long = 0L,
    val appRecordFetchTimeEpochMillis: Long
)
