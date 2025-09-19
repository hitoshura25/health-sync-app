package io.github.hitoshura25.healthsyncapp.avro

import kotlinx.serialization.Serializable

@Serializable
data class AvroHeightRecord(
    val metadata: AvroMetadata,
    val timeEpochMillis: Long,
    val zoneOffsetId: String? = null,
    val heightInMeters: Double,
    val appRecordFetchTimeEpochMillis: Long
)