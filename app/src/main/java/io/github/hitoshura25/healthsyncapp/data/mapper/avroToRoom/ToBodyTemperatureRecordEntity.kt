package io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom

import io.github.hitoshura25.healthsyncapp.avro.AvroBodyTemperatureRecord
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BodyTemperatureRecordEntity

fun AvroBodyTemperatureRecord.toBodyTemperatureRecordEntity(): BodyTemperatureRecordEntity {
    return BodyTemperatureRecordEntity(
        hcUid = this.metadata.id,
        timeEpochMillis = this.timeEpochMillis,
        zoneOffsetId = this.zoneOffsetId,
        temperatureInCelsius = this.temperatureInCelsius,
        measurementLocation = this.measurementLocation.ordinal,
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