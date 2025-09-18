package io.github.hitoshura25.healthsyncapp.avro

import kotlinx.serialization.Serializable

@Serializable
data class AvroBasalMetabolicRateRecord(
    val metadata: AvroMetadata,
    val timeEpochMillis: Long,
    val zoneOffsetId: String? = null,
    val basalMetabolicRateInKilocaloriesPerDay: Double,
    val appRecordFetchTimeEpochMillis: Long
)