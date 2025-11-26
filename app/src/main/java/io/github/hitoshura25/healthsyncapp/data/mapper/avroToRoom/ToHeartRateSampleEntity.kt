package io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom

import io.github.hitoshura25.healthsyncapp.data.avro.AvroHeartRateSample
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.HeartRateSampleEntity

fun AvroHeartRateSample.toHeartRateSampleEntity(
    recordUid: String,
    appRecordFetchTimeEpochMillis: Long,
    dataOriginPackageName: String,
    hcLastModifiedTimeEpochMillis: Long,
    clientRecordId: String?,
    clientRecordVersion: Long,
    deviceManufacturer: String?,
    deviceModel: String?,
    deviceType: String?
): HeartRateSampleEntity {
    return HeartRateSampleEntity(
        hcRecordUid = recordUid,
        sampleTimeEpochMillis = this.timeEpochMillis,
        beatsPerMinute = this.beatsPerMinute,
        zoneOffsetId = null, // This is not available in AvroHeartRateSample
        appRecordFetchTimeEpochMillis = appRecordFetchTimeEpochMillis,
        dataOriginPackageName = dataOriginPackageName,
        hcLastModifiedTimeEpochMillis = hcLastModifiedTimeEpochMillis,
        clientRecordId = clientRecordId,
        clientRecordVersion = clientRecordVersion,
        deviceManufacturer = deviceManufacturer,
        deviceModel = deviceModel,
        deviceType = deviceType
    )
}
