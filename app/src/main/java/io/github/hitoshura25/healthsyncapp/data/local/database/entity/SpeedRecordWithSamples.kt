package io.github.hitoshura25.healthsyncapp.data.local.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class SpeedRecordWithSamples(
    @Embedded val record: SpeedRecordEntity,
    @Relation(
        parentColumn = "health_connect_uid",
        entityColumn = "parentRecordUid"
    )
    @JvmField
    val samples: List<SpeedSampleEntity>
)
