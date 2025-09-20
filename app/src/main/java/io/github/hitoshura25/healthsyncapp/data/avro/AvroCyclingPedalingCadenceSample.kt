package io.github.hitoshura25.healthsyncapp.data.avro

import kotlinx.serialization.Serializable

@Serializable
data class AvroCyclingPedalingCadenceSample(
    val timeEpochMillis: Long,
    val revolutionsPerMinute: Double
)
