package io.github.hitoshura25.healthsyncapp.avro

import kotlinx.serialization.Serializable

@Serializable
data class AvroCyclingPedalingCadenceSample(
    val timeEpochMillis: Long,
    val revolutionsPerMinute: Double
)
