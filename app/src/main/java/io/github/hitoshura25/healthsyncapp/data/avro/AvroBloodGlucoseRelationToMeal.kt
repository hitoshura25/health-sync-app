package io.github.hitoshura25.healthsyncapp.avro

import kotlinx.serialization.Serializable

@Serializable
enum class AvroBloodGlucoseRelationToMeal {
    UNKNOWN,
    GENERAL,
    FASTING,
    BEFORE_MEAL,
    AFTER_MEAL
}