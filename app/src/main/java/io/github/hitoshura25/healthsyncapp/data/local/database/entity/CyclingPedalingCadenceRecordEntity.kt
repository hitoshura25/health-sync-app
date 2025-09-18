package io.github.hitoshura25.healthsyncapp.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cycling_pedaling_cadence_records",
    indices = [Index(value = ["health_connect_uid"], unique = true)]
)
data class CyclingPedalingCadenceRecordEntity(
    @PrimaryKey
    @ColumnInfo(name = "health_connect_uid")
    val hcUid: String,
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long,
    val startZoneOffsetId: String?,
    val endZoneOffsetId: String?,

    val appRecordFetchTimeEpochMillis: Long,
    val dataOriginPackageName: String,
    val hcLastModifiedTimeEpochMillis: Long,
    val clientRecordId: String?,
    val clientRecordVersion: Long,
    val deviceManufacturer: String?,
    val deviceModel: String?,
    val deviceType: String?
)