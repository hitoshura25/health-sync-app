package io.github.hitoshura25.healthsyncapp.data.avro

import kotlinx.serialization.Serializable

@Serializable
data class AvroStepsCadenceSample(
    val timeEpochMillis: Long,
    val rateInStepsPerMinute: Double
)
