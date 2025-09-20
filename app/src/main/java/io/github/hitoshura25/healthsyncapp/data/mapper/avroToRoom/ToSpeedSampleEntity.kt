package io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom

import io.github.hitoshura25.healthsyncapp.data.avro.AvroSpeedSample
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.SpeedSampleEntity

fun AvroSpeedSample.toSpeedSampleEntity(parentRecordUid: String): SpeedSampleEntity {
    return SpeedSampleEntity(
        parentRecordUid = parentRecordUid,
        timeEpochMillis = this.timeEpochMillis,
        speedInMetersPerSecond = this.speedInMetersPerSecond
    )
}
