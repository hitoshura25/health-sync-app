package io.github.hitoshura25.healthsyncapp.data

import androidx.health.connect.client.records.CyclingPedalingCadenceRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroCyclingPedalingCadenceRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroCyclingPedalingCadenceSample

fun mapCyclingPedalingCadenceRecord(record: CyclingPedalingCadenceRecord, fetchedTimeEpochMillis: Long): AvroCyclingPedalingCadenceRecord {
    return AvroCyclingPedalingCadenceRecord(
        metadata = mapHealthConnectMetadataToAvroMetadata(record.metadata),
        startTimeEpochMillis = record.startTime.toEpochMilli(),
        endTimeEpochMillis = record.endTime.toEpochMilli(),
        startZoneOffsetId = record.startZoneOffset?.id,
        endZoneOffsetId = record.endZoneOffset?.id,
        samples = record.samples.map {
            AvroCyclingPedalingCadenceSample(
                timeEpochMillis = it.time.toEpochMilli(),
                revolutionsPerMinute = it.revolutionsPerMinute
            )
        },
        appRecordFetchTimeEpochMillis = fetchedTimeEpochMillis
    )
}