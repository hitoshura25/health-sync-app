package io.github.hitoshura25.healthsyncapp.data.avro

import kotlinx.serialization.Serializable

@Serializable
data class AvroBasalBodyTemperatureRecord(
    val metadata: AvroMetadata,
    val timeEpochMillis: Long,
    val zoneOffsetId: String? = null,
    val temperatureInCelsius: Double,
    val measurementLocation: Int,
    val appRecordFetchTimeEpochMillis: Long
)