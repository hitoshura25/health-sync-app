package io.github.hitoshura25.healthsyncapp.data.avro

import kotlinx.serialization.Serializable

@Serializable
enum class AvroBloodGlucoseRelationToMeal {
    UNKNOWN,
    GENERAL,
    FASTING,
    BEFORE_MEAL,
    AFTER_MEAL
}