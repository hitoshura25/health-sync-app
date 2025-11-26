package io.github.hitoshura25.healthsyncapp.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cycling_pedaling_cadence_samples",
    indices = [Index(value = ["parent_record_uid"])],
    foreignKeys = [
        ForeignKey(
            entity = CyclingPedalingCadenceRecordEntity::class,
            parentColumns = ["health_connect_uid"],
            childColumns = ["parent_record_uid"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CyclingPedalingCadenceSampleEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "parent_record_uid")
    val parentRecordUid: String,

    @ColumnInfo(name = "time_epoch_millis")
    val timeEpochMillis: Long,

    @ColumnInfo(name = "revolutions_per_minute")
    val revolutionsPerMinute: Double
)