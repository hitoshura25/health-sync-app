package io.github.hitoshura25.healthsyncapp.data.avro

import kotlinx.serialization.Serializable

@Serializable
data class AvroBloodPressureRecord(
    val metadata: AvroMetadata,
    val timeEpochMillis: Long,
    val zoneOffsetId: String? = null,
    val systolic: Double,
    val diastolic: Double,
    val bodyPosition: AvroBloodPressureBodyPosition,
    val measurementLocation: AvroBloodPressureMeasurementLocation,
    val appRecordFetchTimeEpochMillis: Long
)