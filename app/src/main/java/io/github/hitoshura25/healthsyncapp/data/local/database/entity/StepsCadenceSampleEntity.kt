package io.github.hitoshura25.healthsyncapp.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "steps_cadence_samples",
    indices = [Index(value = ["parentRecordUid"])],
    foreignKeys = [
        ForeignKey(
            entity = StepsCadenceRecordEntity::class,
            parentColumns = ["health_connect_uid"],
            childColumns = ["parentRecordUid"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class StepsCadenceSampleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val parentRecordUid: String,
    val timeEpochMillis: Long,
    val rateInStepsPerMinute: Double
)
