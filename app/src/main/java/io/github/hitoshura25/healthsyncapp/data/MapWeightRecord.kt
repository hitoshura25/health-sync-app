package io.github.hitoshura25.healthsyncapp.data

import androidx.health.connect.client.records.WeightRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroWeightRecord

fun mapWeightRecord(record: WeightRecord, fetchedTimeEpochMillis: Long): AvroWeightRecord {
    return AvroWeightRecord(
        metadata = mapHealthConnectMetadataToAvroMetadata(record.metadata),
        timeEpochMillis = record.time.toEpochMilli(),
        zoneOffsetId = record.zoneOffset?.id,
        weightInKilograms = record.weight.inKilograms,
        appRecordFetchTimeEpochMillis = fetchedTimeEpochMillis
    )
}