package io.github.hitoshura25.healthsyncapp.data

import androidx.health.connect.client.records.BodyFatRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroBodyFatRecord

fun mapBodyFatRecord(record: BodyFatRecord, fetchedTimeEpochMillis: Long): AvroBodyFatRecord {
    return AvroBodyFatRecord(
        metadata = mapHealthConnectMetadataToAvroMetadata(record.metadata),
        timeEpochMillis = record.time.toEpochMilli(),
        zoneOffsetId = record.zoneOffset?.id,
        percentage = record.percentage.value,
        appRecordFetchTimeEpochMillis = fetchedTimeEpochMillis
    )
}