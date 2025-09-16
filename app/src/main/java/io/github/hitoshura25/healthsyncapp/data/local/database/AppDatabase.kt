package io.github.hitoshura25.healthsyncapp.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BloodGlucoseDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.HeartRateSampleDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.SleepSessionDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.SleepStageDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.StepsRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BloodGlucoseEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.HeartRateSampleEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.SleepSessionEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.SleepStageEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.StepsRecordEntity

@Database(
    entities = [
        StepsRecordEntity::class,
        HeartRateSampleEntity::class,
        SleepSessionEntity::class,
        SleepStageEntity::class,
        BloodGlucoseEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun stepsRecordDao(): StepsRecordDao
    abstract fun heartRateSampleDao(): HeartRateSampleDao
    abstract fun sleepSessionDao(): SleepSessionDao
    abstract fun sleepStageDao(): SleepStageDao
    abstract fun bloodGlucoseDao(): BloodGlucoseDao
}
