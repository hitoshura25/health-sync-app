package io.github.hitoshura25.healthsyncapp.avro

import kotlinx.serialization.Serializable

@Serializable
data class AvroHeartRateSample(
    val timeEpochMillis: Long,
    val beatsPerMinute: Long
)