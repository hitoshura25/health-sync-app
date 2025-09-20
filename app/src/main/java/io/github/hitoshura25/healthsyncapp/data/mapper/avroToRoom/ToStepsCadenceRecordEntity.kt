package io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom

import io.github.hitoshura25.healthsyncapp.avro.AvroStepsCadenceRecord
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.StepsCadenceRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.StepsCadenceSampleEntity

fun AvroStepsCadenceRecord.toStepsCadenceRecordEntity(): Pair<StepsCadenceRecordEntity, List<StepsCadenceSampleEntity>> {
    val recordEntity = StepsCadenceRecordEntity(
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
        StepsCadenceSampleEntity(
            parentRecordUid = this.metadata.id,
            timeEpochMillis = sample.timeEpochMillis,
            rate = sample.rateInStepsPerMinute
        )
    }
    return Pair(recordEntity, sampleEntities)
}