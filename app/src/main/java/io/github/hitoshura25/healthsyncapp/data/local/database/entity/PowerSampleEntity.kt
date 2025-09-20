package io.github.hitoshura25.healthsyncapp.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "power_samples",
    indices = [Index(value = ["parent_record_uid"])],
    foreignKeys = [
        ForeignKey(
            entity = PowerRecordEntity::class,
            parentColumns = ["health_connect_uid"],
            childColumns = ["parent_record_uid"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PowerSampleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "parent_record_uid")
    val parentRecordUid: String,
    @ColumnInfo(name = "time_epoch_millis")
    val timeEpochMillis: Long,
    @ColumnInfo(name = "power_in_watts")
    val powerInWatts: Double
)
