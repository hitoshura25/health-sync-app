package io.github.hitoshura25.healthsyncapp.data.mapper

import io.github.hitoshura25.healthsyncapp.avro.AvroBloodPressureBodyPosition
import io.github.hitoshura25.healthsyncapp.avro.AvroBloodPressureMeasurementLocation
import io.github.hitoshura25.healthsyncapp.avro.AvroBloodPressureRecord
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BloodPressureRecordEntity

private fun mapBloodPressureBodyPositionToInt(avroBodyPosition: AvroBloodPressureBodyPosition): Int {
    return when (avroBodyPosition) {
        AvroBloodPressureBodyPosition.SITTING_DOWN -> 1
        AvroBloodPressureBodyPosition.STANDING_UP -> 2
        AvroBloodPressureBodyPosition.LYING_DOWN -> 3
        AvroBloodPressureBodyPosition.RECLINING -> 4
        AvroBloodPressureBodyPosition.UNKNOWN -> 0
    }
}

private fun mapBloodPressureMeasurementLocationToInt(avroMeasurementLocation: AvroBloodPressureMeasurementLocation): Int {
    return when (avroMeasurementLocation) {
        AvroBloodPressureMeasurementLocation.LEFT_WRIST -> 1
        AvroBloodPressureMeasurementLocation.RIGHT_WRIST -> 2
        AvroBloodPressureMeasurementLocation.LEFT_UPPER_ARM -> 3
        AvroBloodPressureMeasurementLocation.RIGHT_UPPER_ARM -> 4
        AvroBloodPressureMeasurementLocation.UNKNOWN -> 0
    }
}

fun AvroBloodPressureRecord.toBloodPressureRecordEntity(): BloodPressureRecordEntity {
    return BloodPressureRecordEntity(
        hcUid = this.metadata.id,
        timeEpochMillis = this.timeEpochMillis,
        zoneOffsetId = this.zoneOffsetId,
        systolic = this.systolic,
        diastolic = this.diastolic,
        bodyPosition = mapBloodPressureBodyPositionToInt(this.bodyPosition),
        measurementLocation = mapBloodPressureMeasurementLocationToInt(this.measurementLocation),
        appRecordFetchTimeEpochMillis = this.appRecordFetchTimeEpochMillis,
        dataOriginPackageName = this.metadata.dataOriginPackageName,
        hcLastModifiedTimeEpochMillis = this.metadata.lastModifiedTimeEpochMillis,
        clientRecordId = this.metadata.clientRecordId,
        clientRecordVersion = this.metadata.clientRecordVersion,
        deviceManufacturer = this.metadata.device?.manufacturer,
        deviceModel = this.metadata.device?.model,
        deviceType = this.metadata.device?.type
    )
}