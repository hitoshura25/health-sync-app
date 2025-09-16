package io.github.hitoshura25.healthsyncapp.data

import androidx.health.connect.client.records.*
import androidx.health.connect.client.records.metadata.DataOrigin
import androidx.health.connect.client.records.metadata.Metadata
// Removed: import androidx.health.connect.client.units.BloodGlucose
// Removed: import io.github.hitoshura25.healthsyncapp.avro.AvroBloodGlucoseLevelUnit - No longer used
import io.github.hitoshura25.healthsyncapp.avro.AvroBloodGlucoseMealType
import io.github.hitoshura25.healthsyncapp.avro.AvroBloodGlucoseRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroBloodGlucoseRelationToMeal
import io.github.hitoshura25.healthsyncapp.avro.AvroBloodGlucoseSpecimenSource
import io.github.hitoshura25.healthsyncapp.avro.AvroHeartRateRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroHeartRateSample
import io.github.hitoshura25.healthsyncapp.avro.AvroSleepSessionRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroSleepStageRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroSleepStageType
import io.github.hitoshura25.healthsyncapp.avro.AvroStepsRecord
import java.time.Instant

/**
 * Maps Health Connect SDK record objects to Avro DTOs.
 */
object HealthConnectToAvroMapper {

    fun mapStepsRecord(record: StepsRecord, fetchedTimeEpochMillis: Long): AvroStepsRecord {
        return AvroStepsRecord(
            hcUid = record.metadata.id,
            startTimeEpochMillis = record.startTime.toEpochMilli(),
            endTimeEpochMillis = record.endTime.toEpochMilli(),
            startZoneOffsetId = record.startZoneOffset?.id,
            endZoneOffsetId = record.endZoneOffset?.id,
            count = record.count,
            dataOriginPackageName = record.metadata.dataOrigin.packageName,
            hcLastModifiedTimeEpochMillis = record.metadata.lastModifiedTime.toEpochMilli(),
            clientRecordId = record.metadata.clientRecordId,
            clientRecordVersion = record.metadata.clientRecordVersion,
            appRecordFetchTimeEpochMillis = fetchedTimeEpochMillis
        )
    }

    fun mapHeartRateRecord(record: HeartRateRecord, fetchedTimeEpochMillis: Long): AvroHeartRateRecord {
        val avroSamples = record.samples.map {
            AvroHeartRateSample(
                timeEpochMillis = it.time.toEpochMilli(),
                beatsPerMinute = it.beatsPerMinute
            )
        }
        return AvroHeartRateRecord(
            hcUid = record.metadata.id,
            startTimeEpochMillis = record.startTime.toEpochMilli(),
            endTimeEpochMillis = record.endTime.toEpochMilli(),
            startZoneOffsetId = record.startZoneOffset?.id,
            endZoneOffsetId = record.endZoneOffset?.id,
            dataOriginPackageName = record.metadata.dataOrigin.packageName,
            hcLastModifiedTimeEpochMillis = record.metadata.lastModifiedTime.toEpochMilli(),
            clientRecordId = record.metadata.clientRecordId,
            clientRecordVersion = record.metadata.clientRecordVersion,
            appRecordFetchTimeEpochMillis = fetchedTimeEpochMillis,
            samples = avroSamples
        )
    }

    fun mapSleepSessionRecord(record: SleepSessionRecord, fetchedTimeEpochMillis: Long): AvroSleepSessionRecord {
        val avroStages = record.stages.map { hcStage ->
            AvroSleepStageRecord(
                startTimeEpochMillis = hcStage.startTime.toEpochMilli(),
                endTimeEpochMillis = hcStage.endTime.toEpochMilli(),
                stage = mapHcSleepStageToAvro(hcStage.stage)
            )
        }

        return AvroSleepSessionRecord(
            hcUid = record.metadata.id,
            title = record.title,
            notes = record.notes,
            startTimeEpochMillis = record.startTime.toEpochMilli(),
            endTimeEpochMillis = record.endTime.toEpochMilli(),
            startZoneOffsetId = record.startZoneOffset?.id,
            endZoneOffsetId = record.endZoneOffset?.id,
            durationMillis = record.endTime.toEpochMilli() - record.startTime.toEpochMilli(), 
            dataOriginPackageName = record.metadata.dataOrigin.packageName,
            hcLastModifiedTimeEpochMillis = record.metadata.lastModifiedTime.toEpochMilli(),
            clientRecordId = record.metadata.clientRecordId,
            clientRecordVersion = record.metadata.clientRecordVersion,
            appRecordFetchTimeEpochMillis = fetchedTimeEpochMillis,
            stages = avroStages
        )
    }

    fun mapBloodGlucoseRecord(record: BloodGlucoseRecord, fetchedTimeEpochMillis: Long): AvroBloodGlucoseRecord {
        return AvroBloodGlucoseRecord(
            hcUid = record.metadata.id,
            timeEpochMillis = record.time.toEpochMilli(),
            zoneOffsetId = record.zoneOffset?.id,
            levelInMilligramsPerDeciliter = record.level.inMilligramsPerDeciliter, // Renamed field, value is mg/dL
            // levelUnit field removed from Avro DTO
            specimenSource = mapHcSpecimenSourceToAvro(record.specimenSource),
            mealType = mapHcMealTypeToAvro(record.mealType),
            relationToMeal = mapHcRelationToMealToAvro(record.relationToMeal),
            dataOriginPackageName = record.metadata.dataOrigin.packageName,
            hcLastModifiedTimeEpochMillis = record.metadata.lastModifiedTime.toEpochMilli(),
            clientRecordId = record.metadata.clientRecordId,
            clientRecordVersion = record.metadata.clientRecordVersion,
            appRecordFetchTimeEpochMillis = fetchedTimeEpochMillis
        )
    }

    private fun mapHcSleepStageToAvro(hcStageTypeAsInt: Int): AvroSleepStageType {
        return when (hcStageTypeAsInt) {
            SleepSessionRecord.STAGE_TYPE_AWAKE -> AvroSleepStageType.AWAKE
            SleepSessionRecord.STAGE_TYPE_SLEEPING -> AvroSleepStageType.SLEEPING
            SleepSessionRecord.STAGE_TYPE_OUT_OF_BED -> AvroSleepStageType.OUT_OF_BED
            SleepSessionRecord.STAGE_TYPE_LIGHT -> AvroSleepStageType.LIGHT
            SleepSessionRecord.STAGE_TYPE_DEEP -> AvroSleepStageType.DEEP
            SleepSessionRecord.STAGE_TYPE_REM -> AvroSleepStageType.REM
            SleepSessionRecord.STAGE_TYPE_UNKNOWN -> AvroSleepStageType.UNKNOWN
            else -> AvroSleepStageType.UNKNOWN 
        }
    }

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
}
