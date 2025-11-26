package io.github.hitoshura25.healthsyncapp.data.avro

import kotlinx.serialization.Serializable

@Serializable
enum class AvroVo2MaxMeasurementMethod {
    UNKNOWN,
    OTHER,
    METABOLIC_CART,
    HEART_RATE_RATIO,
    COOPER_TEST,
    MULTISTAGE_FITNESS_TEST,
    ROCKPORT_FITNESS_TEST
}