package io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro

import androidx.health.connect.client.records.SpeedRecord
import io.github.hitoshura25.healthsyncapp.data.avro.AvroSpeedRecord
import io.github.hitoshura25.healthsyncapp.data.avro.AvroSpeedSample

fun mapSpeedRecord(record: SpeedRecord, fetchedTimeEpochMillis: Long): AvroSpeedRecord {
    return AvroSpeedRecord(
        metadata = mapHealthConnectMetadataToAvroMetadata(record.metadata),
        startTimeEpochMillis = record.startTime.toEpochMilli(),
        endTimeEpochMillis = record.endTime.toEpochMilli(),
        startZoneOffsetId = record.startZoneOffset?.id,
        endZoneOffsetId = record.endZoneOffset?.id,
        samples = record.samples.map {
            AvroSpeedSample(
                timeEpochMillis = it.time.toEpochMilli(),
                speedInMetersPerSecond = it.speed.inMetersPerSecond
            )
        },
        appRecordFetchTimeEpochMillis = fetchedTimeEpochMillis
    )
}