package io.github.hitoshura25.healthsyncapp.data.avro

import kotlinx.serialization.Serializable

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