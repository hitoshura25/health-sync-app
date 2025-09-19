package io.github.hitoshura25.healthsyncapp.avro

import kotlinx.serialization.Serializable

@Serializable
data class AvroRestingHeartRateRecord(
    val metadata: AvroMetadata,
    val timeEpochMillis: Long,
    val zoneOffsetId: String? = null,
    val beatsPerMinute: Long,
    val appRecordFetchTimeEpochMillis: Long
)