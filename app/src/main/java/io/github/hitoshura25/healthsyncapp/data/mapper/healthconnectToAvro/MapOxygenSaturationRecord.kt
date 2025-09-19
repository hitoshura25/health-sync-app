package io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro

import androidx.health.connect.client.records.OxygenSaturationRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroOxygenSaturationRecord

fun mapOxygenSaturationRecord(record: OxygenSaturationRecord, fetchedTimeEpochMillis: Long): AvroOxygenSaturationRecord {
    return AvroOxygenSaturationRecord(
        metadata = mapHealthConnectMetadataToAvroMetadata(record.metadata),
        timeEpochMillis = record.time.toEpochMilli(),
        zoneOffsetId = record.zoneOffset?.id,
        percentage = record.percentage.value,
        appRecordFetchTimeEpochMillis = fetchedTimeEpochMillis
    )
}