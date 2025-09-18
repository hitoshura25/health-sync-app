package io.github.hitoshura25.healthsyncapp.data

import androidx.health.connect.client.records.BodyTemperatureMeasurementLocation
import androidx.health.connect.client.records.BodyTemperatureRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroBodyTemperatureMeasurementLocation
import io.github.hitoshura25.healthsyncapp.avro.AvroBodyTemperatureRecord

private fun mapHcBodyTemperatureMeasurementLocationToAvro(hcMeasurementLocation: Int): AvroBodyTemperatureMeasurementLocation {
    return when (hcMeasurementLocation) {
        BodyTemperatureMeasurementLocation.MEASUREMENT_LOCATION_ARMPIT -> AvroBodyTemperatureMeasurementLocation.ARMPIT
        BodyTemperatureMeasurementLocation.MEASUREMENT_LOCATION_FINGER -> AvroBodyTemperatureMeasurementLocation.FINGER
        BodyTemperatureMeasurementLocation.MEASUREMENT_LOCATION_FOREHEAD -> AvroBodyTemperatureMeasurementLocation.FOREHEAD
        BodyTemperatureMeasurementLocation.MEASUREMENT_LOCATION_MOUTH -> AvroBodyTemperatureMeasurementLocation.MOUTH
        BodyTemperatureMeasurementLocation.MEASUREMENT_LOCATION_RECTUM -> AvroBodyTemperatureMeasurementLocation.RECTUM
        BodyTemperatureMeasurementLocation.MEASUREMENT_LOCATION_TEMPORAL_ARTERY -> AvroBodyTemperatureMeasurementLocation.TEMPORAL_ARTERY
        BodyTemperatureMeasurementLocation.MEASUREMENT_LOCATION_TOE -> AvroBodyTemperatureMeasurementLocation.TOE
        BodyTemperatureMeasurementLocation.MEASUREMENT_LOCATION_EAR -> AvroBodyTemperatureMeasurementLocation.EAR
        BodyTemperatureMeasurementLocation.MEASUREMENT_LOCATION_WRIST -> AvroBodyTemperatureMeasurementLocation.WRIST
        BodyTemperatureMeasurementLocation.MEASUREMENT_LOCATION_VAGINA -> AvroBodyTemperatureMeasurementLocation.VAGINA
        else -> AvroBodyTemperatureMeasurementLocation.UNKNOWN
    }
}

fun mapBodyTemperatureRecord(record: BodyTemperatureRecord, fetchedTimeEpochMillis: Long): AvroBodyTemperatureRecord {
    return AvroBodyTemperatureRecord(
        metadata = mapHealthConnectMetadataToAvroMetadata(record.metadata),
        timeEpochMillis = record.time.toEpochMilli(),
        zoneOffsetId = record.zoneOffset?.id,
        temperatureInCelsius = record.temperature.inCelsius,
        measurementLocation = mapHcBodyTemperatureMeasurementLocationToAvro(record.measurementLocation),
        appRecordFetchTimeEpochMillis = fetchedTimeEpochMillis
    )
}