package io.github.hitoshura25.healthsyncapp.data

import androidx.health.connect.client.records.*
import androidx.health.connect.client.records.metadata.DataOrigin
import androidx.health.connect.client.records.metadata.Metadata
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
                stage = mapSleepStageType(hcStage.stage) // Use .stage (Int) and map with STAGE_TYPE_* constants
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

    private fun mapSleepStageType(hcStageTypeAsInt: Int): AvroSleepStageType {
        return when (hcStageTypeAsInt) {
            SleepSessionRecord.STAGE_TYPE_AWAKE -> AvroSleepStageType.AWAKE
            SleepSessionRecord.STAGE_TYPE_SLEEPING -> AvroSleepStageType.SLEEPING
            SleepSessionRecord.STAGE_TYPE_OUT_OF_BED -> AvroSleepStageType.OUT_OF_BED
            SleepSessionRecord.STAGE_TYPE_LIGHT -> AvroSleepStageType.LIGHT
            SleepSessionRecord.STAGE_TYPE_DEEP -> AvroSleepStageType.DEEP
            SleepSessionRecord.STAGE_TYPE_REM -> AvroSleepStageType.REM
            SleepSessionRecord.STAGE_TYPE_UNKNOWN -> AvroSleepStageType.UNKNOWN
            else -> AvroSleepStageType.UNKNOWN // Default fallback
        }
    }
}
