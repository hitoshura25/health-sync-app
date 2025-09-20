package io.github.hitoshura25.healthsyncapp.data.avro

import kotlinx.serialization.Serializable

@Serializable
data class AvroRespiratoryRateRecord(
    val metadata: AvroMetadata,
    val timeEpochMillis: Long,
    val zoneOffsetId: String? = null,
    val rate: Double,
    val appRecordFetchTimeEpochMillis: Long
)