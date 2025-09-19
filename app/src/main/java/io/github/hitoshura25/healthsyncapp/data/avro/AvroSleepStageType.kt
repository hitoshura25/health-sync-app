package io.github.hitoshura25.healthsyncapp.avro

import kotlinx.serialization.Serializable

@Serializable
enum class AvroSleepStageType {
    UNKNOWN,
    AWAKE,
    SLEEPING,
    OUT_OF_BED,
    LIGHT,
    DEEP,
    REM
}