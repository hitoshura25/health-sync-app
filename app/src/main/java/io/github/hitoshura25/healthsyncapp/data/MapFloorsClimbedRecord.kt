package io.github.hitoshura25.healthsyncapp.data

import androidx.health.connect.client.records.FloorsClimbedRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroFloorsClimbedRecord

fun mapFloorsClimbedRecord(record: FloorsClimbedRecord, fetchedTimeEpochMillis: Long): AvroFloorsClimbedRecord {
    return AvroFloorsClimbedRecord(
        metadata = mapHealthConnectMetadataToAvroMetadata(record.metadata),
        startTimeEpochMillis = record.startTime.toEpochMilli(),
        endTimeEpochMillis = record.endTime.toEpochMilli(),
        startZoneOffsetId = record.startZoneOffset?.id,
        endZoneOffsetId = record.endZoneOffset?.id,
        floors = record.floors,
        appRecordFetchTimeEpochMillis = fetchedTimeEpochMillis
    )
}