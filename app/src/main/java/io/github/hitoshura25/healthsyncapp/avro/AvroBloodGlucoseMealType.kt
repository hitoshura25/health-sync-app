package io.github.hitoshura25.healthsyncapp.avro

import kotlinx.serialization.Serializable

@Serializable
enum class AvroBloodGlucoseMealType {
    UNKNOWN,
    BREAKFAST,
    LUNCH,
    DINNER,
    SNACK
}