package io.github.hitoshura25.healthsyncapp.data

import androidx.health.connect.client.records.BodyWaterMassRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroBodyWaterMassRecord

fun mapBodyWaterMassRecord(record: BodyWaterMassRecord, fetchedTimeEpochMillis: Long): AvroBodyWaterMassRecord {
    return AvroBodyWaterMassRecord(
        metadata = mapHealthConnectMetadataToAvroMetadata(record.metadata),
        timeEpochMillis = record.time.toEpochMilli(),
        zoneOffsetId = record.zoneOffset?.id,
        massInKilograms = record.mass.inKilograms,
        appRecordFetchTimeEpochMillis = fetchedTimeEpochMillis
    )
}