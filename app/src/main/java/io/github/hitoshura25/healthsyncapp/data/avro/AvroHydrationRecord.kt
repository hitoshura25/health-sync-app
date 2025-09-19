package io.github.hitoshura25.healthsyncapp.avro

import kotlinx.serialization.Serializable

@Serializable
data class AvroHydrationRecord(
    val metadata: AvroMetadata,
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long,
    val startZoneOffsetId: String? = null,
    val endZoneOffsetId: String? = null,
    val volumeInMilliliters: Double,
    val appRecordFetchTimeEpochMillis: Long
)