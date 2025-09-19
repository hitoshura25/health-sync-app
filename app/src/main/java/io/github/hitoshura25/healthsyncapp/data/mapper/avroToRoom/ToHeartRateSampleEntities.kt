package io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom

import io.github.hitoshura25.healthsyncapp.avro.AvroHeartRateRecord
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.HeartRateSampleEntity

fun AvroHeartRateRecord.toHeartRateSampleEntities(): List<HeartRateSampleEntity> {
    val parentRecordMetadata = this.metadata
    val recordFetchedAtTime = this.appRecordFetchTimeEpochMillis
    val sessionStartZoneOffsetId = this.startZoneOffsetId

    return this.samples.map { avroSample ->
        HeartRateSampleEntity(
            hcRecordUid = parentRecordMetadata.id,
            sampleTimeEpochMillis = avroSample.timeEpochMillis,
            beatsPerMinute = avroSample.beatsPerMinute,
            zoneOffsetId = sessionStartZoneOffsetId,
            appRecordFetchTimeEpochMillis = recordFetchedAtTime,
            dataOriginPackageName = parentRecordMetadata.dataOriginPackageName,
            hcLastModifiedTimeEpochMillis = parentRecordMetadata.lastModifiedTimeEpochMillis,
            clientRecordId = parentRecordMetadata.clientRecordId,
            clientRecordVersion = parentRecordMetadata.clientRecordVersion,
            deviceManufacturer = parentRecordMetadata.device?.manufacturer,
            deviceModel = parentRecordMetadata.device?.model,
            deviceType = parentRecordMetadata.device?.type
        )
    }
}