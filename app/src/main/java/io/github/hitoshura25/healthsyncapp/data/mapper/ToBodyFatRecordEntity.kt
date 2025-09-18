package io.github.hitoshura25.healthsyncapp.data.mapper

import io.github.hitoshura25.healthsyncapp.avro.AvroBodyFatRecord
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BodyFatRecordEntity

fun AvroBodyFatRecord.toBodyFatRecordEntity(): BodyFatRecordEntity {
    return BodyFatRecordEntity(
        hcUid = this.metadata.id,
        timeEpochMillis = this.timeEpochMillis,
        zoneOffsetId = this.zoneOffsetId,
        percentage = this.percentage,
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