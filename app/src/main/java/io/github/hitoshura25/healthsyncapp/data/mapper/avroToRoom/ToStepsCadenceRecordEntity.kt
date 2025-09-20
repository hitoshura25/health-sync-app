package io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom

import io.github.hitoshura25.healthsyncapp.data.avro.AvroStepsCadenceRecord
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.StepsCadenceRecordEntity

fun AvroStepsCadenceRecord.toStepsCadenceRecordEntity(): StepsCadenceRecordEntity {
    return StepsCadenceRecordEntity(
        hcUid = this.metadata.id,
        startTimeEpochMillis = this.startTimeEpochMillis,
        endTimeEpochMillis = this.endTimeEpochMillis,
        startZoneOffsetId = this.startZoneOffsetId,
        endZoneOffsetId = this.endZoneOffsetId,
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
