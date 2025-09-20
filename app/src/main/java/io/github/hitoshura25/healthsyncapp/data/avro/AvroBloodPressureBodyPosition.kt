package io.github.hitoshura25.healthsyncapp.data.avro

import kotlinx.serialization.Serializable

@Serializable
enum class AvroBloodPressureBodyPosition {
    UNKNOWN,
    SITTING_DOWN,
    STANDING_UP,
    LYING_DOWN,
    RECLINING
}