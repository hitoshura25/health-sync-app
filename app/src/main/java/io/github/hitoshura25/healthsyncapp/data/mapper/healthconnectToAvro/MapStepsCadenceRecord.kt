package io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro

import androidx.health.connect.client.records.StepsCadenceRecord
import io.github.hitoshura25.healthsyncapp.data.avro.AvroStepsCadenceRecord
import io.github.hitoshura25.healthsyncapp.data.avro.AvroStepsCadenceSample

fun mapStepsCadenceRecord(record: StepsCadenceRecord, fetchedTimeEpochMillis: Long): AvroStepsCadenceRecord {
    return AvroStepsCadenceRecord(
        metadata = mapHealthConnectMetadataToAvroMetadata(record.metadata),
        startTimeEpochMillis = record.startTime.toEpochMilli(),
        endTimeEpochMillis = record.endTime.toEpochMilli(),
        startZoneOffsetId = record.startZoneOffset?.id,
        endZoneOffsetId = record.endZoneOffset?.id,
        samples = record.samples.map {
            AvroStepsCadenceSample(
                timeEpochMillis = it.time.toEpochMilli(),
                rateInStepsPerMinute = it.rate
            )
        },
        appRecordFetchTimeEpochMillis = fetchedTimeEpochMillis
    )
}