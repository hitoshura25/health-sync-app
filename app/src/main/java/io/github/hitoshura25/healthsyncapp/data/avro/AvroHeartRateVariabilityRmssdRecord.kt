package io.github.hitoshura25.healthsyncapp.avro

import kotlinx.serialization.Serializable

@Serializable
data class AvroHeartRateVariabilityRmssdRecord(
    val metadata: AvroMetadata,
    val timeEpochMillis: Long,
    val zoneOffsetId: String? = null,
    val heartRateVariabilityRmssd: Double,
    val appRecordFetchTimeEpochMillis: Long
)