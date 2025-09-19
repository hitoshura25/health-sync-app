package io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom

import io.github.hitoshura25.healthsyncapp.avro.AvroHeartRateVariabilityRmssdRecord
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.HeartRateVariabilityRmssdRecordEntity

fun AvroHeartRateVariabilityRmssdRecord.toHeartRateVariabilityRmssdRecordEntity(): HeartRateVariabilityRmssdRecordEntity {
    return HeartRateVariabilityRmssdRecordEntity(
        hcUid = this.metadata.id,
        timeEpochMillis = this.timeEpochMillis,
        zoneOffsetId = this.zoneOffsetId,
        heartRateVariabilityRmssd = this.heartRateVariabilityRmssd,
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