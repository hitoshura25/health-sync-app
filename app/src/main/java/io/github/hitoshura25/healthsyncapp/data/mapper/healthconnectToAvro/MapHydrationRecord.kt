package io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro

import androidx.health.connect.client.records.HydrationRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroHydrationRecord

fun mapHydrationRecord(record: HydrationRecord, fetchedTimeEpochMillis: Long): AvroHydrationRecord {
    return AvroHydrationRecord(
        metadata = mapHealthConnectMetadataToAvroMetadata(record.metadata),
        startTimeEpochMillis = record.startTime.toEpochMilli(),
        endTimeEpochMillis = record.endTime.toEpochMilli(),
        startZoneOffsetId = record.startZoneOffset?.id,
        endZoneOffsetId = record.endZoneOffset?.id,
        volumeInMilliliters = record.volume.inMilliliters,
        appRecordFetchTimeEpochMillis = fetchedTimeEpochMillis
    )
}