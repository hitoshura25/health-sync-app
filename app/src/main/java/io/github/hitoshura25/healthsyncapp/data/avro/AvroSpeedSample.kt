package io.github.hitoshura25.healthsyncapp.avro

import kotlinx.serialization.Serializable

@Serializable
data class AvroSpeedSample(
    val timeEpochMillis: Long,
    val speedInMetersPerSecond: Double
)
