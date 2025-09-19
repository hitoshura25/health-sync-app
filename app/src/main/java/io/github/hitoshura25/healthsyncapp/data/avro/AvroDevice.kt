package io.github.hitoshura25.healthsyncapp.avro

import kotlinx.serialization.Serializable

@Serializable
data class AvroDevice(
    val manufacturer: String?,
    val model: String?,
    val type: String?
)