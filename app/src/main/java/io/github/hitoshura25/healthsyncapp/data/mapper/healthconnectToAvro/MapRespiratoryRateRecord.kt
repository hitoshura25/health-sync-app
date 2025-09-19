package io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro

import androidx.health.connect.client.records.RespiratoryRateRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroRespiratoryRateRecord

fun mapRespiratoryRateRecord(record: RespiratoryRateRecord, fetchedTimeEpochMillis: Long): AvroRespiratoryRateRecord {
    return AvroRespiratoryRateRecord(
        metadata = mapHealthConnectMetadataToAvroMetadata(record.metadata),
        timeEpochMillis = record.time.toEpochMilli(),
        zoneOffsetId = record.zoneOffset?.id,
        rate = record.rate,
        appRecordFetchTimeEpochMillis = fetchedTimeEpochMillis
    )
}