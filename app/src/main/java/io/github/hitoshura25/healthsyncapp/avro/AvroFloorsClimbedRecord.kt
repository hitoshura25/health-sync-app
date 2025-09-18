package io.github.hitoshura25.healthsyncapp.avro

import kotlinx.serialization.Serializable

@Serializable
data class AvroFloorsClimbedRecord(
    val metadata: AvroMetadata,
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long,
    val startZoneOffsetId: String? = null,
    val endZoneOffsetId: String? = null,
    val floors: Double,
    val appRecordFetchTimeEpochMillis: Long
)