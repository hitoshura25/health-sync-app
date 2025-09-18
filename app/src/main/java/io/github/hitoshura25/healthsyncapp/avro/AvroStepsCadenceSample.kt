package io.github.hitoshura25.healthsyncapp.avro

import kotlinx.serialization.Serializable

@Serializable
data class AvroStepsCadenceSample(
    val timeEpochMillis: Long,
    val rateInStepsPerMinute: Double
)
