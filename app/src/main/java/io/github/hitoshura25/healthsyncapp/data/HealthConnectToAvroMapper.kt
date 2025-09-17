package io.github.hitoshura25.healthsyncapp.data

import androidx.health.connect.client.records.*
import androidx.health.connect.client.records.metadata.DataOrigin
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Metadata
import io.github.hitoshura25.healthsyncapp.avro.AvroBloodGlucoseMealType
import io.github.hitoshura25.healthsyncapp.avro.AvroBloodGlucoseRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroBloodGlucoseRelationToMeal
import io.github.hitoshura25.healthsyncapp.avro.AvroBloodGlucoseSpecimenSource
import io.github.hitoshura25.healthsyncapp.avro.AvroDevice
import io.github.hitoshura25.healthsyncapp.avro.AvroHeartRateRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroHeartRateSample
import io.github.hitoshura25.healthsyncapp.avro.AvroMetadata
import io.github.hitoshura25.healthsyncapp.avro.AvroSleepSessionRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroSleepStageRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroSleepStageType
import io.github.hitoshura25.healthsyncapp.avro.AvroStepsRecord
import java.time.Instant

/**
 * Maps Health Connect SDK record objects to Avro DTOs.
 */
object HealthConnectToAvroMapper {

    private fun mapMetadata(metadata: Metadata): AvroMetadata {
        val device = metadata.device
        val avroDevice = device?.let {
            AvroDevice(
                manufacturer = it.manufacturer,
                model = it.model,
                type = mapDeviceType(it.type)
            )
        }
        return AvroMetadata(
            id = metadata.id,
            dataOriginPackageName = metadata.dataOrigin.packageName,
            lastModifiedTimeEpochMillis = metadata.lastModifiedTime.toEpochMilli(),
            clientRecordId = metadata.clientRecordId,
            clientRecordVersion = metadata.clientRecordVersion,
            device = avroDevice
        )
    }

    private fun mapDeviceType(deviceType: Int): String {
        return when (deviceType) {
            Device.TYPE_PHONE -> "PHONE"
            Device.TYPE_WATCH -> "WATCH"
            // Device.TYPE_TABLET -> "TABLET"
            Device.TYPE_HEAD_MOUNTED -> "HEAD_MOUNTED"
            Device.TYPE_RING -> "RING"
            Device.TYPE_SCALE -> "SCALE"
            Device.TYPE_FITNESS_BAND -> "FITNESS_BAND"
            Device.TYPE_CHEST_STRAP -> "CHEST_STRAP"
            Device.TYPE_SMART_DISPLAY -> "SMART_DISPLAY"
            else -> "UNKNOWN"
        }
    }

    fun mapStepsRecord(record: StepsRecord, fetchedTimeEpochMillis: Long): AvroStepsRecord {
        return AvroStepsRecord(
            metadata = mapMetadata(record.metadata),
            startTimeEpochMillis = record.startTime.toEpochMilli(),
            endTimeEpochMillis = record.endTime.toEpochMilli(),
            startZoneOffsetId = record.startZoneOffset?.id,
            endZoneOffsetId = record.endZoneOffset?.id,
            count = record.count,
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
            metadata = mapMetadata(record.metadata),
            startTimeEpochMillis = record.startTime.toEpochMilli(),
            endTimeEpochMillis = record.endTime.toEpochMilli(),
            startZoneOffsetId = record.startZoneOffset?.id,
            endZoneOffsetId = record.endZoneOffset?.id,
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
            metadata = mapMetadata(record.metadata),
            title = record.title,
            notes = record.notes,
            startTimeEpochMillis = record.startTime.toEpochMilli(),
            endTimeEpochMillis = record.endTime.toEpochMilli(),
            startZoneOffsetId = record.startZoneOffset?.id,
            endZoneOffsetId = record.endZoneOffset?.id,
            durationMillis = record.endTime.toEpochMilli() - record.startTime.toEpochMilli(),
            appRecordFetchTimeEpochMillis = fetchedTimeEpochMillis,
            stages = avroStages
        )
    }

    fun mapBloodGlucoseRecord(record: BloodGlucoseRecord, fetchedTimeEpochMillis: Long): AvroBloodGlucoseRecord {
        return AvroBloodGlucoseRecord(
            metadata = mapMetadata(record.metadata),
            timeEpochMillis = record.time.toEpochMilli(),
            zoneOffsetId = record.zoneOffset?.id,
            levelInMilligramsPerDeciliter = record.level.inMilligramsPerDeciliter,
            specimenSource = mapHcSpecimenSourceToAvro(record.specimenSource),
            mealType = mapHcMealTypeToAvro(record.mealType),
            relationToMeal = mapHcRelationToMealToAvro(record.relationToMeal),
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
