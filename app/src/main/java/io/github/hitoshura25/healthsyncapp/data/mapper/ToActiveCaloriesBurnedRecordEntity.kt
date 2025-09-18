package io.github.hitoshura25.healthsyncapp.data.mapper

import io.github.hitoshura25.healthsyncapp.avro.AvroActiveCaloriesBurnedRecord
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.ActiveCaloriesBurnedRecordEntity

fun AvroActiveCaloriesBurnedRecord.toActiveCaloriesBurnedRecordEntity(): ActiveCaloriesBurnedRecordEntity {
    return ActiveCaloriesBurnedRecordEntity(
        hcUid = this.metadata.id,
        startTimeEpochMillis = this.startTimeEpochMillis,
        endTimeEpochMillis = this.endTimeEpochMillis,
        startZoneOffsetId = this.startZoneOffsetId,
        endZoneOffsetId = this.endZoneOffsetId,
        energyInKilocalories = this.energyInKilocalories,
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