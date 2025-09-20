package io.github.hitoshura25.healthsyncapp.data.avro

import kotlinx.serialization.Serializable

@Serializable
data class AvroPowerSample(
    val timeEpochMillis: Long,
    val powerInWatts: Double
)
