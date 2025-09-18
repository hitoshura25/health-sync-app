package io.github.hitoshura25.healthsyncapp.avro

import kotlinx.serialization.Serializable

@Serializable
data class AvroSleepSessionRecord(
    val metadata: AvroMetadata,
    val title: String? = null,
    val notes: String? = null,
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long,
    val startZoneOffsetId: String? = null,
    val endZoneOffsetId: String? = null,
    val durationMillis: Long? = null,
    val appRecordFetchTimeEpochMillis: Long,
    val stages: List<AvroSleepStageRecord> = emptyList()
)