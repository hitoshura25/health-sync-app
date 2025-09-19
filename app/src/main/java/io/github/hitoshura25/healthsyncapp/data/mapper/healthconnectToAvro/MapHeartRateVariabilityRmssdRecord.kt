package io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro

import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroHeartRateVariabilityRmssdRecord

fun mapHeartRateVariabilityRmssdRecord(record: HeartRateVariabilityRmssdRecord, fetchedTimeEpochMillis: Long): AvroHeartRateVariabilityRmssdRecord {
    return AvroHeartRateVariabilityRmssdRecord(
        metadata = mapHealthConnectMetadataToAvroMetadata(record.metadata),
        timeEpochMillis = record.time.toEpochMilli(),
        zoneOffsetId = record.zoneOffset?.id,
        heartRateVariabilityRmssd = record.heartRateVariabilityMillis,
        appRecordFetchTimeEpochMillis = fetchedTimeEpochMillis
    )
}