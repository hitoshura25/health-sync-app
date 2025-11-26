package io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom

import io.github.hitoshura25.healthsyncapp.data.avro.AvroCyclingPedalingCadenceSample
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.CyclingPedalingCadenceSampleEntity

fun AvroCyclingPedalingCadenceSample.toCyclingPedalingCadenceSampleEntity(parentRecordUid: String): CyclingPedalingCadenceSampleEntity {
    return CyclingPedalingCadenceSampleEntity(
        parentRecordUid = parentRecordUid,
        timeEpochMillis = this.timeEpochMillis,
        revolutionsPerMinute = this.revolutionsPerMinute
    )
}
