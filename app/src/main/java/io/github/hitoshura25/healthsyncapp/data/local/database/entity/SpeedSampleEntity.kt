package io.github.hitoshura25.healthsyncapp.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "speed_samples",
    indices = [Index(value = ["parent_record_uid"])],
    foreignKeys = [
        ForeignKey(
            entity = SpeedRecordEntity::class,
            parentColumns = ["health_connect_uid"],
            childColumns = ["parent_record_uid"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SpeedSampleEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "parent_record_uid")
    val parentRecordUid: String,

    @ColumnInfo(name = "time_epoch_millis")
    val timeEpochMillis: Long,

    @ColumnInfo(name = "speed_in_meters_per_second")
    val speedInMetersPerSecond: Double
)
