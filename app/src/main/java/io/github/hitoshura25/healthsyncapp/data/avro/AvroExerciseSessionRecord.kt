package io.github.hitoshura25.healthsyncapp.data.avro

import kotlinx.serialization.Serializable

@Serializable
data class AvroExerciseSessionRecord(
    val metadata: AvroMetadata,
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long,
    val startZoneOffsetId: String? = null,
    val endZoneOffsetId: String? = null,
    val exerciseType: AvroExerciseType,
    val title: String? = null,
    val notes: String? = null,
    val appRecordFetchTimeEpochMillis: Long
)