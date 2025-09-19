package io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro

import androidx.health.connect.client.records.BoneMassRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroBoneMassRecord

fun mapBoneMassRecord(record: BoneMassRecord, fetchedTimeEpochMillis: Long): AvroBoneMassRecord {
    return AvroBoneMassRecord(
        metadata = mapHealthConnectMetadataToAvroMetadata(record.metadata),
        timeEpochMillis = record.time.toEpochMilli(),
        zoneOffsetId = record.zoneOffset?.id,
        massInKilograms = record.mass.inKilograms,
        appRecordFetchTimeEpochMillis = fetchedTimeEpochMillis
    )
}