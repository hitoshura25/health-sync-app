package io.github.hitoshura25.healthsyncapp.data.avro

import kotlinx.serialization.Serializable

@Serializable
data class AvroSpeedSample(
    val timeEpochMillis: Long,
    val speedInMetersPerSecond: Double
)
