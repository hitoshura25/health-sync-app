package io.github.hitoshura25.healthsyncapp.data.avro

import kotlinx.serialization.Serializable

@Serializable
data class AvroMetadata(
    val id: String,
    val dataOriginPackageName: String,
    val lastModifiedTimeEpochMillis: Long,
    val clientRecordId: String?,
    val clientRecordVersion: Long,
    val device: AvroDevice?
)