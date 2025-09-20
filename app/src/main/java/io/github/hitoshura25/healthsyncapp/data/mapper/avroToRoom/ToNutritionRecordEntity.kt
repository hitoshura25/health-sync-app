package io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom

import io.github.hitoshura25.healthsyncapp.data.avro.AvroNutritionRecord
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.NutritionRecordEntity

fun AvroNutritionRecord.toNutritionRecordEntity(): NutritionRecordEntity {
    return NutritionRecordEntity(
        hcUid = this.metadata.id,
        startTimeEpochMillis = this.startTimeEpochMillis,
        endTimeEpochMillis = this.endTimeEpochMillis,
        startZoneOffsetId = this.startZoneOffsetId,
        endZoneOffsetId = this.endZoneOffsetId,
        name = this.name,
        calories = this.calories,
        carbohydrates = this.carbohydrates,
        protein = this.protein,
        totalFat = this.totalFat,
        saturatedFat = this.saturatedFat,
        unsaturatedFat = this.unsaturatedFat,
        transFat = this.transFat,
        sodium = this.sodium,
        potassium = this.potassium,
        cholesterol = this.cholesterol,
        fiber = this.fiber,
        sugar = this.sugar,
        vitaminC = this.vitaminC,
        vitaminD = this.vitaminD,
        calcium = this.calcium,
        iron = this.iron,
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