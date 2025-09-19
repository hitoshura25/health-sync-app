package io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom

import io.github.hitoshura25.healthsyncapp.avro.AvroBasalBodyTemperatureRecord
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BasalBodyTemperatureRecordEntity

fun AvroBasalBodyTemperatureRecord.toBasalBodyTemperatureRecordEntity(): BasalBodyTemperatureRecordEntity {
    return BasalBodyTemperatureRecordEntity(
        hcUid = this.metadata.id,
        timeEpochMillis = this.timeEpochMillis,
        zoneOffsetId = this.zoneOffsetId,
        temperatureInCelsius = this.temperatureInCelsius,
        measurementLocation = this.measurementLocation,
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