package io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro

import androidx.health.connect.client.records.BasalMetabolicRateRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroBasalMetabolicRateRecord

fun mapBasalMetabolicRateRecord(record: BasalMetabolicRateRecord, fetchedTimeEpochMillis: Long): AvroBasalMetabolicRateRecord {
    return AvroBasalMetabolicRateRecord(
        metadata = mapHealthConnectMetadataToAvroMetadata(record.metadata),
        timeEpochMillis = record.time.toEpochMilli(),
        zoneOffsetId = record.zoneOffset?.id,
        basalMetabolicRateInKilocaloriesPerDay = record.basalMetabolicRate.inKilocaloriesPerDay,
        appRecordFetchTimeEpochMillis = fetchedTimeEpochMillis
    )
}