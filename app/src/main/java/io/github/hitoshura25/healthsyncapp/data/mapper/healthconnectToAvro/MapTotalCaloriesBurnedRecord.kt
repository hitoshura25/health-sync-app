package io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro

import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroTotalCaloriesBurnedRecord

fun mapTotalCaloriesBurnedRecord(record: TotalCaloriesBurnedRecord, fetchedTimeEpochMillis: Long): AvroTotalCaloriesBurnedRecord {
    return AvroTotalCaloriesBurnedRecord(
        metadata = mapHealthConnectMetadataToAvroMetadata(record.metadata),
        startTimeEpochMillis = record.startTime.toEpochMilli(),
        endTimeEpochMillis = record.endTime.toEpochMilli(),
        startZoneOffsetId = record.startZoneOffset?.id,
        endZoneOffsetId = record.endZoneOffset?.id,
        energyInKilocalories = record.energy.inKilocalories,
        appRecordFetchTimeEpochMillis = fetchedTimeEpochMillis
    )
}