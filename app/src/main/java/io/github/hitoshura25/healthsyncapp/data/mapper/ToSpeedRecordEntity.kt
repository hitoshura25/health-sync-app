package io.github.hitoshura25.healthsyncapp.data.mapper

import io.github.hitoshura25.healthsyncapp.avro.AvroSpeedRecord
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.SpeedRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.SpeedSampleEntity

fun AvroSpeedRecord.toSpeedRecordEntity(): Pair<SpeedRecordEntity, List<SpeedSampleEntity>> {
    val recordEntity = SpeedRecordEntity(
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
        SpeedSampleEntity(
            parentRecordUid = this.metadata.id,
            timeEpochMillis = sample.timeEpochMillis,
            speedInMetersPerSecond = sample.speedInMetersPerSecond
        )
    }
    return Pair(recordEntity, sampleEntities)
}