package io.github.hitoshura25.healthsyncapp.data.avro

import kotlinx.serialization.Serializable

@Serializable
data class AvroTotalCaloriesBurnedRecord(
    val metadata: AvroMetadata,
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long,
    val startZoneOffsetId: String? = null,
    val endZoneOffsetId: String? = null,
    val energyInKilocalories: Double,
    val appRecordFetchTimeEpochMillis: Long
)