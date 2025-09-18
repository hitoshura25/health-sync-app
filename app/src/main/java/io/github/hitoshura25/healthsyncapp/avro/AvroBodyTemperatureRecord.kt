package io.github.hitoshura25.healthsyncapp.avro

import kotlinx.serialization.Serializable

@Serializable
data class AvroBodyTemperatureRecord(
    val metadata: AvroMetadata,
    val timeEpochMillis: Long,
    val zoneOffsetId: String? = null,
    val temperatureInCelsius: Double,
    val measurementLocation: AvroBodyTemperatureMeasurementLocation,
    val appRecordFetchTimeEpochMillis: Long
)