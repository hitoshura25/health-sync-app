package io.github.hitoshura25.healthsyncapp.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bone_mass_records")
data class BoneMassRecordEntity(
    @PrimaryKey
    val hcUid: String,
    val timeEpochMillis: Long,
    val zoneOffsetId: String?,
    val massInKilograms: Double,
    val appRecordFetchTimeEpochMillis: Long,
    val dataOriginPackageName: String,
    val hcLastModifiedTimeEpochMillis: Long,
    val clientRecordId: String?,
    val clientRecordVersion: Long,
    val deviceManufacturer: String?,
    val deviceModel: String?,
    val deviceType: String?
)