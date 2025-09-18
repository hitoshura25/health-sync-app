package io.github.hitoshura25.healthsyncapp.data

import androidx.health.connect.client.records.DistanceRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroDistanceRecord

fun mapDistanceRecord(record: DistanceRecord, fetchedTimeEpochMillis: Long): AvroDistanceRecord {
    return AvroDistanceRecord(
        metadata = mapHealthConnectMetadataToAvroMetadata(record.metadata),
        startTimeEpochMillis = record.startTime.toEpochMilli(),
        endTimeEpochMillis = record.endTime.toEpochMilli(),
        startZoneOffsetId = record.startZoneOffset?.id,
        endZoneOffsetId = record.endZoneOffset?.id,
        distanceInMeters = record.distance.inMeters,
        appRecordFetchTimeEpochMillis = fetchedTimeEpochMillis
    )
}