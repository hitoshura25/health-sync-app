package io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom

import io.github.hitoshura25.healthsyncapp.data.avro.AvroBloodGlucoseMealType
import io.github.hitoshura25.healthsyncapp.data.avro.AvroBloodGlucoseRecord
import io.github.hitoshura25.healthsyncapp.data.avro.AvroBloodGlucoseRelationToMeal
import io.github.hitoshura25.healthsyncapp.data.avro.AvroBloodGlucoseSpecimenSource
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BloodGlucoseEntity

private fun mapSpecimenSourceToInt(avroSpecimenSource: AvroBloodGlucoseSpecimenSource): Int {
    return when (avroSpecimenSource) {
        AvroBloodGlucoseSpecimenSource.INTERSTITIAL_FLUID -> 1
        AvroBloodGlucoseSpecimenSource.CAPILLARY_BLOOD -> 2
        AvroBloodGlucoseSpecimenSource.PLASMA -> 3
        AvroBloodGlucoseSpecimenSource.SERUM -> 4
        AvroBloodGlucoseSpecimenSource.TEARS -> 5
        AvroBloodGlucoseSpecimenSource.WHOLE_BLOOD -> 6
        AvroBloodGlucoseSpecimenSource.UNKNOWN -> 0
    }
}

private fun mapMealTypeToInt(avroMealType: AvroBloodGlucoseMealType): Int {
    return when (avroMealType) {
        AvroBloodGlucoseMealType.BREAKFAST -> 1
        AvroBloodGlucoseMealType.LUNCH -> 2
        AvroBloodGlucoseMealType.DINNER -> 3
        AvroBloodGlucoseMealType.SNACK -> 4
        AvroBloodGlucoseMealType.UNKNOWN -> 0
    }
}

private fun mapRelationToMealToInt(avroRelationToMeal: AvroBloodGlucoseRelationToMeal): Int {
    return when (avroRelationToMeal) {
        AvroBloodGlucoseRelationToMeal.GENERAL -> 1
        AvroBloodGlucoseRelationToMeal.FASTING -> 2
        AvroBloodGlucoseRelationToMeal.BEFORE_MEAL -> 3
        AvroBloodGlucoseRelationToMeal.AFTER_MEAL -> 4
        AvroBloodGlucoseRelationToMeal.UNKNOWN -> 0
    }
}

fun AvroBloodGlucoseRecord.toBloodGlucoseEntity(): BloodGlucoseEntity {
    return BloodGlucoseEntity(
        hcUid = this.metadata.id,
        timeEpochMillis = this.timeEpochMillis,
        zoneOffsetId = this.zoneOffsetId,
        levelInMilligramsPerDeciliter = this.levelInMilligramsPerDeciliter,
        specimenSource = mapSpecimenSourceToInt(this.specimenSource),
        mealType = mapMealTypeToInt(this.mealType),
        relationToMeal = mapRelationToMealToInt(this.relationToMeal),
        dataOriginPackageName = this.metadata.dataOriginPackageName,
        hcLastModifiedTimeEpochMillis = this.metadata.lastModifiedTimeEpochMillis,
        clientRecordId = this.metadata.clientRecordId,
        clientRecordVersion = this.metadata.clientRecordVersion,
        appRecordFetchTimeEpochMillis = this.appRecordFetchTimeEpochMillis,
        deviceManufacturer = this.metadata.device?.manufacturer,
        deviceModel = this.metadata.device?.model,
        deviceType = this.metadata.device?.type
    )
}