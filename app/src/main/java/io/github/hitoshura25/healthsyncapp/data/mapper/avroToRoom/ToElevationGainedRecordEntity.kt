package io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom

import io.github.hitoshura25.healthsyncapp.avro.AvroElevationGainedRecord
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.ElevationGainedRecordEntity

fun AvroElevationGainedRecord.toElevationGainedRecordEntity(): ElevationGainedRecordEntity {
    return ElevationGainedRecordEntity(
        hcUid = this.metadata.id,
        startTimeEpochMillis = this.startTimeEpochMillis,
        endTimeEpochMillis = this.endTimeEpochMillis,
        startZoneOffsetId = this.startZoneOffsetId,
        endZoneOffsetId = this.endZoneOffsetId,
        elevationInMeters = this.elevationInMeters,
        appRecordFetchTimeEpochMillis = this.appRecordFetchTimeEpochMillis,
        dataOriginPackageName = this.metadata.dataOriginPackageName,
        hcLastModifiedTimeEpochMillis = this.metadata.lastModifiedTimeEpochMillis,
        clientRecordId = this.metadata.clientRecordId,
        clientRecordVersion = this.metadata.clientRecordVersion,
        deviceManufacturer = this.metadata.device?.manufacturer,
        deviceModel = this.metadata.device?.model,
        deviceType = this.metadata.device?.type
    )
}