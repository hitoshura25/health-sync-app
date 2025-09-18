package io.github.hitoshura25.healthsyncapp.data

import androidx.health.connect.client.records.StepsRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroStepsRecord

fun mapStepsRecord(record: StepsRecord, fetchedTimeEpochMillis: Long): AvroStepsRecord {
    return AvroStepsRecord(
        metadata = mapHealthConnectMetadataToAvroMetadata(record.metadata),
        startTimeEpochMillis = record.startTime.toEpochMilli(),
        endTimeEpochMillis = record.endTime.toEpochMilli(),
        startZoneOffsetId = record.startZoneOffset?.id,
        endZoneOffsetId = record.endZoneOffset?.id,
        count = record.count,
        appRecordFetchTimeEpochMillis = fetchedTimeEpochMillis
    )
}