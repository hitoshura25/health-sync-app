package io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro

import androidx.health.connect.client.records.BasalBodyTemperatureRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroBasalBodyTemperatureRecord

fun mapBasalBodyTemperatureRecord(record: BasalBodyTemperatureRecord, fetchedTimeEpochMillis: Long): AvroBasalBodyTemperatureRecord {
    return AvroBasalBodyTemperatureRecord(
        metadata = mapHealthConnectMetadataToAvroMetadata(record.metadata),
        timeEpochMillis = record.time.toEpochMilli(),
        zoneOffsetId = record.zoneOffset?.id,
        temperatureInCelsius = record.temperature.inCelsius,
        measurementLocation = record.measurementLocation,
        appRecordFetchTimeEpochMillis = fetchedTimeEpochMillis
    )
}