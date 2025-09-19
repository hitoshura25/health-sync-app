package io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro

import androidx.health.connect.client.records.SleepSessionRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroSleepSessionRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroSleepStageRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroSleepStageType

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

fun mapSleepSessionRecord(record: SleepSessionRecord, fetchedTimeEpochMillis: Long): AvroSleepSessionRecord {
    val avroStages = record.stages.map { hcStage ->
        AvroSleepStageRecord(
            startTimeEpochMillis = hcStage.startTime.toEpochMilli(),
            endTimeEpochMillis = hcStage.endTime.toEpochMilli(),
            stage = mapHcSleepStageToAvro(hcStage.stage)
        )
    }

    return AvroSleepSessionRecord(
        metadata = mapHealthConnectMetadataToAvroMetadata(record.metadata),
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