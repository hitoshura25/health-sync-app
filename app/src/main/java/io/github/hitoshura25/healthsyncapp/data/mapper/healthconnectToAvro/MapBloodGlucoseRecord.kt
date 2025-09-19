package io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro

import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.MealType
import io.github.hitoshura25.healthsyncapp.avro.AvroBloodGlucoseMealType
import io.github.hitoshura25.healthsyncapp.avro.AvroBloodGlucoseRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroBloodGlucoseRelationToMeal
import io.github.hitoshura25.healthsyncapp.avro.AvroBloodGlucoseSpecimenSource

private fun mapHcSpecimenSourceToAvro(hcSpecimenSource: Int): AvroBloodGlucoseSpecimenSource {
    return when (hcSpecimenSource) {
        BloodGlucoseRecord.SPECIMEN_SOURCE_INTERSTITIAL_FLUID -> AvroBloodGlucoseSpecimenSource.INTERSTITIAL_FLUID
        BloodGlucoseRecord.SPECIMEN_SOURCE_CAPILLARY_BLOOD -> AvroBloodGlucoseSpecimenSource.CAPILLARY_BLOOD
        BloodGlucoseRecord.SPECIMEN_SOURCE_PLASMA -> AvroBloodGlucoseSpecimenSource.PLASMA
        BloodGlucoseRecord.SPECIMEN_SOURCE_SERUM -> AvroBloodGlucoseSpecimenSource.SERUM
        BloodGlucoseRecord.SPECIMEN_SOURCE_TEARS -> AvroBloodGlucoseSpecimenSource.TEARS
        BloodGlucoseRecord.SPECIMEN_SOURCE_WHOLE_BLOOD -> AvroBloodGlucoseSpecimenSource.WHOLE_BLOOD
        BloodGlucoseRecord.SPECIMEN_SOURCE_UNKNOWN -> AvroBloodGlucoseSpecimenSource.UNKNOWN
        else -> AvroBloodGlucoseSpecimenSource.UNKNOWN
    }
}

private fun mapHcMealTypeToAvro(hcMealType: Int): AvroBloodGlucoseMealType {
    return when (hcMealType) {
        MealType.MEAL_TYPE_BREAKFAST -> AvroBloodGlucoseMealType.BREAKFAST
        MealType.MEAL_TYPE_LUNCH -> AvroBloodGlucoseMealType.LUNCH
        MealType.MEAL_TYPE_DINNER -> AvroBloodGlucoseMealType.DINNER
        MealType.MEAL_TYPE_SNACK -> AvroBloodGlucoseMealType.SNACK
        MealType.MEAL_TYPE_UNKNOWN -> AvroBloodGlucoseMealType.UNKNOWN
        else -> AvroBloodGlucoseMealType.UNKNOWN
    }
}

private fun mapHcRelationToMealToAvro(hcRelationToMeal: Int): AvroBloodGlucoseRelationToMeal {
    return when (hcRelationToMeal) {
        BloodGlucoseRecord.RELATION_TO_MEAL_GENERAL -> AvroBloodGlucoseRelationToMeal.GENERAL
        BloodGlucoseRecord.RELATION_TO_MEAL_FASTING -> AvroBloodGlucoseRelationToMeal.FASTING
        BloodGlucoseRecord.RELATION_TO_MEAL_BEFORE_MEAL -> AvroBloodGlucoseRelationToMeal.BEFORE_MEAL
        BloodGlucoseRecord.RELATION_TO_MEAL_AFTER_MEAL -> AvroBloodGlucoseRelationToMeal.AFTER_MEAL
        BloodGlucoseRecord.RELATION_TO_MEAL_UNKNOWN -> AvroBloodGlucoseRelationToMeal.UNKNOWN
        else -> AvroBloodGlucoseRelationToMeal.UNKNOWN
    }
}

fun mapBloodGlucoseRecord(record: BloodGlucoseRecord, fetchedTimeEpochMillis: Long): AvroBloodGlucoseRecord {
    return AvroBloodGlucoseRecord(
        metadata = mapHealthConnectMetadataToAvroMetadata(record.metadata),
        timeEpochMillis = record.time.toEpochMilli(),
        zoneOffsetId = record.zoneOffset?.id,
        levelInMilligramsPerDeciliter = record.level.inMilligramsPerDeciliter,
        specimenSource = mapHcSpecimenSourceToAvro(record.specimenSource),
        mealType = mapHcMealTypeToAvro(record.mealType),
        relationToMeal = mapHcRelationToMealToAvro(record.relationToMeal),
        appRecordFetchTimeEpochMillis = fetchedTimeEpochMillis
    )
}