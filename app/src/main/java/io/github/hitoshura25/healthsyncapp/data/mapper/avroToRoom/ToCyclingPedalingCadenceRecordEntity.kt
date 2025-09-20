package io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom

import io.github.hitoshura25.healthsyncapp.avro.AvroCyclingPedalingCadenceRecord
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.CyclingPedalingCadenceRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.CyclingPedalingCadenceSampleEntity

fun AvroCyclingPedalingCadenceRecord.toCyclingPedalingCadenceRecordEntity(): Pair<CyclingPedalingCadenceRecordEntity, List<CyclingPedalingCadenceSampleEntity>> {
    val recordEntity = CyclingPedalingCadenceRecordEntity(
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
        CyclingPedalingCadenceSampleEntity(
            parentRecordUid = this.metadata.id,
            timeEpochMillis = sample.timeEpochMillis,
            revolutionsPerMinute = sample.revolutionsPerMinute
        )
    }
    return Pair(
        recordEntity, sampleEntities)
}