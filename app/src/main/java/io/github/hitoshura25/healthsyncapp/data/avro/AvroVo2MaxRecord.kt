package io.github.hitoshura25.healthsyncapp.avro

import kotlinx.serialization.Serializable

@Serializable
data class AvroVo2MaxRecord(
    val metadata: AvroMetadata,
    val timeEpochMillis: Long,
    val zoneOffsetId: String? = null,
    val vo2MillilitersPerMinuteKilogram: Double,
    val measurementMethod: AvroVo2MaxMeasurementMethod,
    val appRecordFetchTimeEpochMillis: Long
)