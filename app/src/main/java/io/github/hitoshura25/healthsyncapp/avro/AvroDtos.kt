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

// AvroBloodGlucoseLevelUnit enum removed as it's no longer used.

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
    val hcUid: String,
    val timeEpochMillis: Long,
    val zoneOffsetId: String? = null,
    val levelInMilligramsPerDeciliter: Double, // Renamed from levelValue, unit is implicit
    // levelUnit field removed
    val specimenSource: AvroBloodGlucoseSpecimenSource,
    val mealType: AvroBloodGlucoseMealType,
    val relationToMeal: AvroBloodGlucoseRelationToMeal,
    val dataOriginPackageName: String,
    val hcLastModifiedTimeEpochMillis: Long,
    val clientRecordId: String? = null,
    val clientRecordVersion: Long = 0L,
    val appRecordFetchTimeEpochMillis: Long
)
