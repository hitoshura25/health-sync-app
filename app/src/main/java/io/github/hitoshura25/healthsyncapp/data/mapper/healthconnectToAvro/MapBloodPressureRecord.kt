package io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro

import androidx.health.connect.client.records.BloodPressureRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroBloodPressureBodyPosition
import io.github.hitoshura25.healthsyncapp.avro.AvroBloodPressureMeasurementLocation
import io.github.hitoshura25.healthsyncapp.avro.AvroBloodPressureRecord

private fun mapHcBloodPressureBodyPositionToAvro(hcBodyPosition: Int): AvroBloodPressureBodyPosition {
    return when (hcBodyPosition) {
        BloodPressureRecord.BODY_POSITION_SITTING_DOWN -> AvroBloodPressureBodyPosition.SITTING_DOWN
        BloodPressureRecord.BODY_POSITION_STANDING_UP -> AvroBloodPressureBodyPosition.STANDING_UP
        BloodPressureRecord.BODY_POSITION_LYING_DOWN -> AvroBloodPressureBodyPosition.LYING_DOWN
        BloodPressureRecord.BODY_POSITION_RECLINING -> AvroBloodPressureBodyPosition.RECLINING
        else -> AvroBloodPressureBodyPosition.UNKNOWN
    }
}

private fun mapHcBloodPressureMeasurementLocationToAvro(hcMeasurementLocation: Int): AvroBloodPressureMeasurementLocation {
    return when (hcMeasurementLocation) {
        BloodPressureRecord.MEASUREMENT_LOCATION_LEFT_WRIST -> AvroBloodPressureMeasurementLocation.LEFT_WRIST
        BloodPressureRecord.MEASUREMENT_LOCATION_RIGHT_WRIST -> AvroBloodPressureMeasurementLocation.RIGHT_WRIST
        BloodPressureRecord.MEASUREMENT_LOCATION_LEFT_UPPER_ARM -> AvroBloodPressureMeasurementLocation.LEFT_UPPER_ARM
        BloodPressureRecord.MEASUREMENT_LOCATION_RIGHT_UPPER_ARM -> AvroBloodPressureMeasurementLocation.RIGHT_UPPER_ARM
        else -> AvroBloodPressureMeasurementLocation.UNKNOWN
    }
}

fun mapBloodPressureRecord(record: BloodPressureRecord, fetchedTimeEpochMillis: Long): AvroBloodPressureRecord {
    return AvroBloodPressureRecord(
        metadata = mapHealthConnectMetadataToAvroMetadata(record.metadata),
        timeEpochMillis = record.time.toEpochMilli(),
        zoneOffsetId = record.zoneOffset?.id,
        systolic = record.systolic.inMillimetersOfMercury,
        diastolic = record.diastolic.inMillimetersOfMercury,
        bodyPosition = mapHcBloodPressureBodyPositionToAvro(record.bodyPosition),
        measurementLocation = mapHcBloodPressureMeasurementLocationToAvro(record.measurementLocation),
        appRecordFetchTimeEpochMillis = fetchedTimeEpochMillis
    )
}