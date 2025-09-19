package io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom

import io.github.hitoshura25.healthsyncapp.avro.AvroStepsRecord
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.StepsRecordEntity

fun AvroStepsRecord.toStepsRecordEntity(): StepsRecordEntity {
    return StepsRecordEntity(
        hcUid = this.metadata.id,
        count = this.count,
        startTimeEpochMillis = this.startTimeEpochMillis,
        endTimeEpochMillis = this.endTimeEpochMillis,
        zoneOffsetId = this.startZoneOffsetId,
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