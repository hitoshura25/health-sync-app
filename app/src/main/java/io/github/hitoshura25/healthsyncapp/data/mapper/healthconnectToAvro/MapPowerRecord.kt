package io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro

import androidx.health.connect.client.records.PowerRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroPowerRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroPowerSample

fun mapPowerRecord(record: PowerRecord, fetchedTimeEpochMillis: Long): AvroPowerRecord {
    return AvroPowerRecord(
        metadata = mapHealthConnectMetadataToAvroMetadata(record.metadata),
        startTimeEpochMillis = record.startTime.toEpochMilli(),
        endTimeEpochMillis = record.endTime.toEpochMilli(),
        startZoneOffsetId = record.startZoneOffset?.id,
        endZoneOffsetId = record.endZoneOffset?.id,
        samples = record.samples.map {
            AvroPowerSample(
                timeEpochMillis = it.time.toEpochMilli(),
                powerInWatts = it.power.inWatts
            )
        },
        appRecordFetchTimeEpochMillis = fetchedTimeEpochMillis
    )
}