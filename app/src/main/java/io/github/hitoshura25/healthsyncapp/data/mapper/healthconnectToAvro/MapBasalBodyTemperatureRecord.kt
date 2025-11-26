package io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro

import androidx.health.connect.client.records.BasalBodyTemperatureRecord
import androidx.health.connect.client.records.BodyTemperatureMeasurementLocation
import io.github.hitoshura25.healthsyncapp.data.avro.AvroBasalBodyTemperatureRecord
import io.github.hitoshura25.healthsyncapp.data.avro.AvroBasalBodyTemperatureMeasurementLocation

private fun mapHcBasalBodyTemperatureMeasurementLocationToAvro(hcMeasurementLocation: Int): AvroBasalBodyTemperatureMeasurementLocation {
    return when (hcMeasurementLocation) {
        BodyTemperatureMeasurementLocation.MEASUREMENT_LOCATION_ARMPIT -> AvroBasalBodyTemperatureMeasurementLocation.ARMPIT
        BodyTemperatureMeasurementLocation.MEASUREMENT_LOCATION_FINGER -> AvroBasalBodyTemperatureMeasurementLocation.FINGER
        BodyTemperatureMeasurementLocation.MEASUREMENT_LOCATION_FOREHEAD -> AvroBasalBodyTemperatureMeasurementLocation.FOREHEAD
        BodyTemperatureMeasurementLocation.MEASUREMENT_LOCATION_MOUTH -> AvroBasalBodyTemperatureMeasurementLocation.MOUTH
        BodyTemperatureMeasurementLocation.MEASUREMENT_LOCATION_RECTUM -> AvroBasalBodyTemperatureMeasurementLocation.RECTUM
        BodyTemperatureMeasurementLocation.MEASUREMENT_LOCATION_TEMPORAL_ARTERY -> AvroBasalBodyTemperatureMeasurementLocation.TEMPORAL_ARTERY
        BodyTemperatureMeasurementLocation.MEASUREMENT_LOCATION_TOE -> AvroBasalBodyTemperatureMeasurementLocation.TOE
        BodyTemperatureMeasurementLocation.MEASUREMENT_LOCATION_EAR -> AvroBasalBodyTemperatureMeasurementLocation.EAR
        BodyTemperatureMeasurementLocation.MEASUREMENT_LOCATION_WRIST -> AvroBasalBodyTemperatureMeasurementLocation.WRIST
        BodyTemperatureMeasurementLocation.MEASUREMENT_LOCATION_VAGINA -> AvroBasalBodyTemperatureMeasurementLocation.VAGINA
        else -> AvroBasalBodyTemperatureMeasurementLocation.UNKNOWN
    }
}

fun mapBasalBodyTemperatureRecord(record: BasalBodyTemperatureRecord, fetchedTimeEpochMillis: Long): AvroBasalBodyTemperatureRecord {
    return AvroBasalBodyTemperatureRecord(
        metadata = mapHealthConnectMetadataToAvroMetadata(record.metadata),
        timeEpochMillis = record.time.toEpochMilli(),
        zoneOffsetId = record.zoneOffset?.id,
        temperatureInCelsius = record.temperature.inCelsius,
        measurementLocation = mapHcBasalBodyTemperatureMeasurementLocationToAvro(record.measurementLocation),
        appRecordFetchTimeEpochMillis = fetchedTimeEpochMillis
    )
}