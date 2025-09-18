package io.github.hitoshura25.healthsyncapp.data

import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroActiveCaloriesBurnedRecord

fun mapActiveCaloriesBurnedRecord(record: ActiveCaloriesBurnedRecord, fetchedTimeEpochMillis: Long): AvroActiveCaloriesBurnedRecord {
    return AvroActiveCaloriesBurnedRecord(
        metadata = mapHealthConnectMetadataToAvroMetadata(record.metadata),
        startTimeEpochMillis = record.startTime.toEpochMilli(),
        endTimeEpochMillis = record.endTime.toEpochMilli(),
        startZoneOffsetId = record.startZoneOffset?.id,
        endZoneOffsetId = record.endZoneOffset?.id,
        energyInKilocalories = record.energy.inKilocalories,
        appRecordFetchTimeEpochMillis = fetchedTimeEpochMillis
    )
}
