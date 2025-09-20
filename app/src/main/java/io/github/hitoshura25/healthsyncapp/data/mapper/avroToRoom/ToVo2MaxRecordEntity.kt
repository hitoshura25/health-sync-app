package io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom

import io.github.hitoshura25.healthsyncapp.data.avro.AvroVo2MaxRecord
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.Vo2MaxRecordEntity

fun AvroVo2MaxRecord.toVo2MaxRecordEntity(): Vo2MaxRecordEntity {
    return Vo2MaxRecordEntity(
        hcUid = this.metadata.id,
        timeEpochMillis = this.timeEpochMillis,
        zoneOffsetId = this.zoneOffsetId,
        vo2Max = this.vo2MillilitersPerMinuteKilogram,
        measurementMethod = this.measurementMethod.ordinal,
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