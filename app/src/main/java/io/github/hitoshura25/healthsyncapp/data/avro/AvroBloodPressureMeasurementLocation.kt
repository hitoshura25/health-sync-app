package io.github.hitoshura25.healthsyncapp.data.avro

import kotlinx.serialization.Serializable

@Serializable
enum class AvroBloodPressureMeasurementLocation {
    UNKNOWN,
    LEFT_WRIST,
    RIGHT_WRIST,
    LEFT_UPPER_ARM,
    RIGHT_UPPER_ARM
}