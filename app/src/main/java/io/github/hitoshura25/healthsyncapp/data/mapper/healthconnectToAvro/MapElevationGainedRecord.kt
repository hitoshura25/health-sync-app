package io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro

import androidx.health.connect.client.records.ElevationGainedRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroElevationGainedRecord

fun mapElevationGainedRecord(record: ElevationGainedRecord, fetchedTimeEpochMillis: Long): AvroElevationGainedRecord {
    return AvroElevationGainedRecord(
        metadata = mapHealthConnectMetadataToAvroMetadata(record.metadata),
        startTimeEpochMillis = record.startTime.toEpochMilli(),
        endTimeEpochMillis = record.endTime.toEpochMilli(),
        startZoneOffsetId = record.startZoneOffset?.id,
        endZoneOffsetId = record.endZoneOffset?.id,
        elevationInMeters = record.elevation.inMeters,
        appRecordFetchTimeEpochMillis = fetchedTimeEpochMillis
    )
}