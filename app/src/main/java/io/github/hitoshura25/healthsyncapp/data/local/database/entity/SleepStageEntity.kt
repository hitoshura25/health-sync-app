package io.github.hitoshura25.healthsyncapp.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sleep_stages",
    foreignKeys = [
        ForeignKey(
            entity = SleepSessionEntity::class,
            parentColumns = ["health_connect_uid"], // Foreign key to SleepSessionEntity.hcUid
            childColumns = ["session_hc_uid"],    // This entity's column that holds the foreign key
            onDelete = ForeignKey.CASCADE      // If a sleep session is deleted, its stages are also deleted
        )
    ],
    indices = [
        Index(value = ["session_hc_uid"]), // Index for faster queries of stages by session
        Index(value = ["start_time_epoch_millis"])
    ]
)
data class SleepStageEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "session_hc_uid") // Foreign key linking to SleepSessionEntity.hcUid
    val sessionHcUid: String,

    @ColumnInfo(name = "start_time_epoch_millis")
    val startTimeEpochMillis: Long,

    @ColumnInfo(name = "end_time_epoch_millis")
    val endTimeEpochMillis: Long,

    @ColumnInfo(name = "stage") // Store AvroSleepStageType.name as String, or map to Int if preferred
    val stage: String
)
