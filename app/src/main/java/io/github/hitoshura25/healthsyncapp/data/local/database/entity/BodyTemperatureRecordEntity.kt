package io.github.hitoshura25.healthsyncapp.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "body_temperature_records")
data class BodyTemperatureRecordEntity(
    @PrimaryKey
    val hcUid: String,
    val timeEpochMillis: Long,
    val zoneOffsetId: String?,
    val temperatureInCelsius: Double,
    val measurementLocation: Int,
    val appRecordFetchTimeEpochMillis: Long,
    val dataOriginPackageName: String,
    val hcLastModifiedTimeEpochMillis: Long,
    val clientRecordId: String?,
    val clientRecordVersion: Long,
    val deviceManufacturer: String?,
    val deviceModel: String?,
    val deviceType: String?
)