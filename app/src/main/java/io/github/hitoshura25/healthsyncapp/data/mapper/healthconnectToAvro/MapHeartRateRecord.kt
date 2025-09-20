package io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro

import androidx.health.connect.client.records.HeartRateRecord
import io.github.hitoshura25.healthsyncapp.data.avro.AvroHeartRateRecord
import io.github.hitoshura25.healthsyncapp.data.avro.AvroHeartRateSample

fun mapHeartRateRecord(record: HeartRateRecord, fetchedTimeEpochMillis: Long): AvroHeartRateRecord {
    val avroSamples = record.samples.map {
        AvroHeartRateSample(
            timeEpochMillis = it.time.toEpochMilli(),
            beatsPerMinute = it.beatsPerMinute
        )
    }
    return AvroHeartRateRecord(
        metadata = mapHealthConnectMetadataToAvroMetadata(record.metadata),
        startTimeEpochMillis = record.startTime.toEpochMilli(),
        endTimeEpochMillis = record.endTime.toEpochMilli(),
        startZoneOffsetId = record.startZoneOffset?.id,
        endZoneOffsetId = record.endZoneOffset?.id,
        appRecordFetchTimeEpochMillis = fetchedTimeEpochMillis,
        samples = avroSamples
    )
}