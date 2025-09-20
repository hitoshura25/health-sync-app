package io.github.hitoshura25.healthsyncapp.data.avro

import kotlinx.serialization.Serializable

@Serializable
data class AvroDevice(
    val manufacturer: String?,
    val model: String?,
    val type: String?
)