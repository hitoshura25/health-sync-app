package io.github.hitoshura25.healthsyncapp.avro

import kotlinx.serialization.Serializable

/**
 * Data Transfer Objects for Avro serialization.
 */

@Serializable
data class AvroDevice(
    val manufacturer: String?,
    val model: String?,
    val type: String?
)

@Serializable
data class AvroMetadata(
    val id: String,
    val dataOriginPackageName: String,
    val lastModifiedTimeEpochMillis: Long,
    val clientRecordId: String?,
    val clientRecordVersion: Long,
    val device: AvroDevice?
)

@Serializable
data class AvroStepsRecord(
    val metadata: AvroMetadata,
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long,
    val startZoneOffsetId: String? = null,
    val endZoneOffsetId: String? = null,
    val count: Long,
    val appRecordFetchTimeEpochMillis: Long
)

@Serializable
data class AvroHeartRateSample(
    val timeEpochMillis: Long,
    val beatsPerMinute: Long
)

@Serializable
data class AvroHeartRateRecord(
    val metadata: AvroMetadata,
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long,
    val startZoneOffsetId: String? = null,
    val endZoneOffsetId: String? = null,
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
    val metadata: AvroMetadata,
    val title: String? = null,
    val notes: String? = null,
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long,
    val startZoneOffsetId: String? = null,
    val endZoneOffsetId: String? = null,
    val durationMillis: Long? = null,
    val appRecordFetchTimeEpochMillis: Long,
    val stages: List<AvroSleepStageRecord> = emptyList()
)

// --- Appended Blood Glucose Avro DTOs and Enums ---

@Serializable
enum class AvroBloodGlucoseSpecimenSource {
    INTERSTITIAL_FLUID,
    CAPILLARY_BLOOD,
    PLASMA,
    SERUM,
    TEARS,
    WHOLE_BLOOD,
    UNKNOWN
}

@Serializable
enum class AvroBloodGlucoseMealType {
    UNKNOWN,
    BREAKFAST,
    LUNCH,
    DINNER,
    SNACK
}

@Serializable
enum class AvroBloodGlucoseRelationToMeal {
    UNKNOWN,
    GENERAL,
    FASTING,
    BEFORE_MEAL,
    AFTER_MEAL
}

@Serializable
data class AvroBloodGlucoseRecord(
    val metadata: AvroMetadata,
    val timeEpochMillis: Long,
    val zoneOffsetId: String? = null,
    val levelInMilligramsPerDeciliter: Double,
    val specimenSource: AvroBloodGlucoseSpecimenSource,
    val mealType: AvroBloodGlucoseMealType,
    val relationToMeal: AvroBloodGlucoseRelationToMeal,
    val appRecordFetchTimeEpochMillis: Long
)
