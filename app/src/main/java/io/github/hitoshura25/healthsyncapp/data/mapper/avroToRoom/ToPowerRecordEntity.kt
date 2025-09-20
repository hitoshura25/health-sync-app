package io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom

import io.github.hitoshura25.healthsyncapp.data.avro.AvroPowerRecord
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.PowerRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.PowerSampleEntity

fun AvroPowerRecord.toPowerRecordEntity(): Pair<PowerRecordEntity, List<PowerSampleEntity>> {
    val recordEntity = PowerRecordEntity(
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

    val sampleEntities = this.samples.map { sample ->
        PowerSampleEntity(
            parentRecordUid = this.metadata.id,
            timeEpochMillis = sample.timeEpochMillis,
            powerInWatts = sample.powerInWatts
        )
    }
    return Pair(recordEntity, sampleEntities)
}