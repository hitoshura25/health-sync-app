package io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom

import io.github.hitoshura25.healthsyncapp.data.avro.AvroPowerSample
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.PowerSampleEntity

fun AvroPowerSample.toPowerSampleEntity(parentRecordUid: String): PowerSampleEntity {
    return PowerSampleEntity(
        parentRecordUid = parentRecordUid,
        timeEpochMillis = this.timeEpochMillis,
        powerInWatts = this.powerInWatts
    )
}
