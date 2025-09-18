package io.github.hitoshura25.healthsyncapp.data

import androidx.health.connect.client.records.Vo2MaxRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroVo2MaxMeasurementMethod
import io.github.hitoshura25.healthsyncapp.avro.AvroVo2MaxRecord

private fun mapHcVo2MaxMeasurementMethodToAvro(hcMeasurementMethod: Int): AvroVo2MaxMeasurementMethod {
    return when (hcMeasurementMethod) {
        Vo2MaxRecord.MEASUREMENT_METHOD_OTHER -> AvroVo2MaxMeasurementMethod.OTHER
        Vo2MaxRecord.MEASUREMENT_METHOD_METABOLIC_CART -> AvroVo2MaxMeasurementMethod.METABOLIC_CART
        Vo2MaxRecord.MEASUREMENT_METHOD_HEART_RATE_RATIO -> AvroVo2MaxMeasurementMethod.HEART_RATE_RATIO
        Vo2MaxRecord.MEASUREMENT_METHOD_COOPER_TEST -> AvroVo2MaxMeasurementMethod.COOPER_TEST
        Vo2MaxRecord.MEASUREMENT_METHOD_MULTISTAGE_FITNESS_TEST -> AvroVo2MaxMeasurementMethod.MULTISTAGE_FITNESS_TEST
        Vo2MaxRecord.MEASUREMENT_METHOD_ROCKPORT_FITNESS_TEST -> AvroVo2MaxMeasurementMethod.ROCKPORT_FITNESS_TEST
        else -> AvroVo2MaxMeasurementMethod.UNKNOWN
    }
}

fun mapVo2MaxRecord(record: Vo2MaxRecord, fetchedTimeEpochMillis: Long): AvroVo2MaxRecord {
    return AvroVo2MaxRecord(
        metadata = mapHealthConnectMetadataToAvroMetadata(record.metadata),
        timeEpochMillis = record.time.toEpochMilli(),
        zoneOffsetId = record.zoneOffset?.id,
        vo2MillilitersPerMinuteKilogram = record.vo2MillilitersPerMinuteKilogram,
        measurementMethod = mapHcVo2MaxMeasurementMethodToAvro(record.measurementMethod),
        appRecordFetchTimeEpochMillis = fetchedTimeEpochMillis
    )
}