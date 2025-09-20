package io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom

import io.github.hitoshura25.healthsyncapp.data.avro.AvroSleepSessionRecord
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.SleepSessionEntity

fun AvroSleepSessionRecord.toSleepSessionEntity(): SleepSessionEntity {
    return SleepSessionEntity(
        hcUid = this.metadata.id,
        title = this.title,
        notes = this.notes,
        startTimeEpochMillis = this.startTimeEpochMillis,
        startZoneOffsetId = this.startZoneOffsetId,
        endTimeEpochMillis = this.endTimeEpochMillis,
        endZoneOffsetId = this.endZoneOffsetId,
        durationMillis = this.durationMillis,
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