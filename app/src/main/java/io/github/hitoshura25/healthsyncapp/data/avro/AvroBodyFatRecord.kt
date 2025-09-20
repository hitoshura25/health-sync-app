package io.github.hitoshura25.healthsyncapp.data.avro

import kotlinx.serialization.Serializable

@Serializable
data class AvroBodyFatRecord(
    val metadata: AvroMetadata,
    val timeEpochMillis: Long,
    val zoneOffsetId: String? = null,
    val percentage: Double,
    val appRecordFetchTimeEpochMillis: Long
)