package io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro

import androidx.health.connect.client.records.LeanBodyMassRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroLeanBodyMassRecord

fun mapLeanBodyMassRecord(record: LeanBodyMassRecord, fetchedTimeEpochMillis: Long): AvroLeanBodyMassRecord {
    return AvroLeanBodyMassRecord(
        metadata = mapHealthConnectMetadataToAvroMetadata(record.metadata),
        timeEpochMillis = record.time.toEpochMilli(),
        zoneOffsetId = record.zoneOffset?.id,
        massInKilograms = record.mass.inKilograms,
        appRecordFetchTimeEpochMillis = fetchedTimeEpochMillis
    )
}