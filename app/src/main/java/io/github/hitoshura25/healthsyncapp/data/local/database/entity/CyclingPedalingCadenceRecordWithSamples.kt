package io.github.hitoshura25.healthsyncapp.data.local.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class CyclingPedalingCadenceRecordWithSamples(
    @Embedded val record: CyclingPedalingCadenceRecordEntity,
    @Relation(
        parentColumn = "health_connect_uid",
        entityColumn = "parent_record_uid"
    )
    val samples: List<CyclingPedalingCadenceSampleEntity>
)
