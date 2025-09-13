package io.github.hitoshura25.healthsyncapp.avro

import kotlinx.serialization.Serializable

/**
 * Data Transfer Objects for Avro serialization.
 */

@Serializable
data class AvroStepsRecord(
    val hcUid: String,
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long,
    val startZoneOffsetId: String? = null,
    val endZoneOffsetId: String? = null,
    val count: Long,
    val dataOriginPackageName: String,
    val hcLastModifiedTimeEpochMillis: Long,
    val clientRecordId: String? = null,
    val clientRecordVersion: Long = 0L, // Consistent with Avro schema default
    val appRecordFetchTimeEpochMillis: Long
)

@Serializable
data class AvroHeartRateSample(
    val timeEpochMillis: Long,
    val beatsPerMinute: Long
)

@Serializable
data class AvroHeartRateRecord(
    val hcUid: String,
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long,
    val startZoneOffsetId: String? = null,
    val endZoneOffsetId: String? = null,
    val dataOriginPackageName: String,
    val hcLastModifiedTimeEpochMillis: Long,
    val clientRecordId: String? = null,
    val clientRecordVersion: Long = 0L,
    val appRecordFetchTimeEpochMillis: Long,
    val samples: List<AvroHeartRateSample>
)

@Serializable
enum class AvroSleepStageType {
    UNKNOWN,
    AWAKE,
    SLEEPING,
    OUT_OF_BED,
    LIGHT,
    DEEP,
    REM
}

@Serializable
data class AvroSleepStageRecord(
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long,
    val stage: AvroSleepStageType
)

@Serializable
data class AvroSleepSessionRecord(
    val hcUid: String,
    val title: String? = null,
    val notes: String? = null,
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long,
    val startZoneOffsetId: String? = null,
    val endZoneOffsetId: String? = null,
    val durationMillis: Long? = null,
    val dataOriginPackageName: String,
    val hcLastModifiedTimeEpochMillis: Long,
    val clientRecordId: String? = null,
    val clientRecordVersion: Long = 0L,
    val appRecordFetchTimeEpochMillis: Long,
    val stages: List<AvroSleepStageRecord> = emptyList()
)
