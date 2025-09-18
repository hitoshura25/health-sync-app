package io.github.hitoshura25.healthsyncapp.data

import androidx.health.connect.client.records.HeightRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroHeightRecord

fun mapHeightRecord(record: HeightRecord, fetchedTimeEpochMillis: Long): AvroHeightRecord {
    return AvroHeightRecord(
        metadata = mapHealthConnectMetadataToAvroMetadata(record.metadata),
        timeEpochMillis = record.time.toEpochMilli(),
        zoneOffsetId = record.zoneOffset?.id,
        heightInMeters = record.height.inMeters,
        appRecordFetchTimeEpochMillis = fetchedTimeEpochMillis
    )
}