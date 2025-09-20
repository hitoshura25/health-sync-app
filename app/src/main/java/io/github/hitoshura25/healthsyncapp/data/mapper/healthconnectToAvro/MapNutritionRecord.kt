package io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro

import androidx.health.connect.client.records.NutritionRecord
import io.github.hitoshura25.healthsyncapp.data.avro.AvroNutritionRecord

fun mapNutritionRecord(record: NutritionRecord, fetchedTimeEpochMillis: Long): AvroNutritionRecord {
    return AvroNutritionRecord(
        metadata = mapHealthConnectMetadataToAvroMetadata(record.metadata),
        startTimeEpochMillis = record.startTime.toEpochMilli(),
        endTimeEpochMillis = record.endTime.toEpochMilli(),
        startZoneOffsetId = record.startZoneOffset?.id,
        endZoneOffsetId = record.endZoneOffset?.id,
        name = record.name,
        calories = record.energy?.inKilocalories,
        carbohydrates = record.totalCarbohydrate?.inGrams,
        protein = record.protein?.inGrams,
        totalFat = record.totalFat?.inGrams,
        saturatedFat = record.saturatedFat?.inGrams,
        unsaturatedFat = record.unsaturatedFat?.inGrams,
        transFat = record.transFat?.inGrams,
        sodium = record.sodium?.inMilligrams,
        potassium = record.potassium?.inMilligrams,
        cholesterol = record.cholesterol?.inMilligrams,
        fiber = record.dietaryFiber?.inGrams,
        sugar = record.sugar?.inGrams,
        vitaminC = record.vitaminC?.inMilligrams,
        vitaminD = record.vitaminD?.inMicrograms,
        calcium = record.calcium?.inMilligrams,
        iron = record.iron?.inMilligrams,
        appRecordFetchTimeEpochMillis = fetchedTimeEpochMillis
    )
}