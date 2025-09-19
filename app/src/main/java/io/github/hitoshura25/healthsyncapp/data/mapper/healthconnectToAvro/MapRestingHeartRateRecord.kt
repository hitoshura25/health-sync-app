package io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro

import androidx.health.connect.client.records.RestingHeartRateRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroRestingHeartRateRecord

fun mapRestingHeartRateRecord(record: RestingHeartRateRecord, fetchedTimeEpochMillis: Long): AvroRestingHeartRateRecord {
    return AvroRestingHeartRateRecord(
        metadata = mapHealthConnectMetadataToAvroMetadata(record.metadata),
        timeEpochMillis = record.time.toEpochMilli(),
        zoneOffsetId = record.zoneOffset?.id,
        beatsPerMinute = record.beatsPerMinute.toLong(),
        appRecordFetchTimeEpochMillis = fetchedTimeEpochMillis
    )
}