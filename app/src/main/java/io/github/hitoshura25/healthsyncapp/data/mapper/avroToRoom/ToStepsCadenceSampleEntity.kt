package io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom

import io.github.hitoshura25.healthsyncapp.data.avro.AvroStepsCadenceSample
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.StepsCadenceSampleEntity

fun AvroStepsCadenceSample.toStepsCadenceSampleEntity(parentRecordUid: String): StepsCadenceSampleEntity {
    return StepsCadenceSampleEntity(
        parentRecordUid = parentRecordUid,
        timeEpochMillis = this.timeEpochMillis,
        rate = this.rateInStepsPerMinute
    )
}
