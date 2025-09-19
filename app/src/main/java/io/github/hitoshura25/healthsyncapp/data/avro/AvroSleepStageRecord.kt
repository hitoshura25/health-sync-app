package io.github.hitoshura25.healthsyncapp.avro

import kotlinx.serialization.Serializable

@Serializable
data class AvroSleepStageRecord(
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long,
    val stage: AvroSleepStageType
)