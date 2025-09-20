package io.github.hitoshura25.healthsyncapp.data.avro

import kotlinx.serialization.Serializable

@Serializable
enum class AvroBodyTemperatureMeasurementLocation {
    UNKNOWN,
    ARMPIT,
    FINGER,
    FOREHEAD,
    MOUTH,
    RECTUM,
    TEMPORAL_ARTERY,
    TOE,
    EAR,
    WRIST,
    VAGINA
}