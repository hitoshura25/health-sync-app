package io.github.hitoshura25.healthsyncapp.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.ActiveCaloriesBurnedRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BasalBodyTemperatureRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BasalMetabolicRateRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BloodGlucoseDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.HeartRateSampleDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.SleepSessionDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.SleepStageDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.StepsRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.WeightRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.ActiveCaloriesBurnedRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BasalBodyTemperatureRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BasalMetabolicRateRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BloodGlucoseEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.HeartRateSampleEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.SleepSessionEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.SleepStageEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.WeightRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.CyclingPedalingCadenceRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.CyclingPedalingCadenceRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.StepsRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.DistanceRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.DistanceRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.ElevationGainedRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.ElevationGainedRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.ExerciseSessionRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.ExerciseSessionRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.FloorsClimbedRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.FloorsClimbedRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.HeartRateVariabilityRmssdRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.HeartRateVariabilityRmssdRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.PowerRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.PowerRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.RestingHeartRateRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.RestingHeartRateRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.SpeedRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.SpeedRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.StepsCadenceRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.StepsCadenceRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.TotalCaloriesBurnedRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.TotalCaloriesBurnedRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.Vo2MaxRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.Vo2MaxRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BodyFatRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BodyFatRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BodyTemperatureRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BodyTemperatureRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BodyWaterMassRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BodyWaterMassRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BoneMassRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BoneMassRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.HeightRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.HeightRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.LeanBodyMassRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.LeanBodyMassRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.HydrationRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.HydrationRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.NutritionRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.NutritionRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BloodPressureRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BloodPressureRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.OxygenSaturationRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.OxygenSaturationRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.RespiratoryRateRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.CyclingPedalingCadenceSampleEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.PowerSampleEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.RespiratoryRateRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.SpeedSampleEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.StepsCadenceSampleEntity

@Database(
    entities = [
        StepsRecordEntity::class,
        HeartRateSampleEntity::class,
        SleepSessionEntity::class,
        SleepStageEntity::class,
        BloodGlucoseEntity::class,
        WeightRecordEntity::class,
        ActiveCaloriesBurnedRecordEntity::class,
        BasalBodyTemperatureRecordEntity::class,
        BasalMetabolicRateRecordEntity::class,
        CyclingPedalingCadenceRecordEntity::class,
        CyclingPedalingCadenceSampleEntity::class,
        DistanceRecordEntity::class,
        ElevationGainedRecordEntity::class,
        ExerciseSessionRecordEntity::class,
        FloorsClimbedRecordEntity::class,
        HeartRateVariabilityRmssdRecordEntity::class,
        PowerRecordEntity::class,
        PowerSampleEntity::class,
        RestingHeartRateRecordEntity::class,
        SpeedRecordEntity::class,
        SpeedSampleEntity::class,
        StepsCadenceRecordEntity::class,
        StepsCadenceSampleEntity::class,
        TotalCaloriesBurnedRecordEntity::class,
        Vo2MaxRecordEntity::class,
        BodyFatRecordEntity::class,
        BodyTemperatureRecordEntity::class,
        BodyWaterMassRecordEntity::class,
        BoneMassRecordEntity::class,
        HeightRecordEntity::class,
        LeanBodyMassRecordEntity::class,
        HydrationRecordEntity::class,
        NutritionRecordEntity::class,
        BloodPressureRecordEntity::class,
        OxygenSaturationRecordEntity::class,
        RespiratoryRateRecordEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun stepsRecordDao(): StepsRecordDao
    abstract fun heartRateSampleDao(): HeartRateSampleDao
    abstract fun sleepSessionDao(): SleepSessionDao
    abstract fun sleepStageDao(): SleepStageDao
    abstract fun bloodGlucoseDao(): BloodGlucoseDao
    abstract fun weightRecordDao(): WeightRecordDao
    abstract fun activeCaloriesBurnedRecordDao(): ActiveCaloriesBurnedRecordDao
    abstract fun basalBodyTemperatureRecordDao(): BasalBodyTemperatureRecordDao
    abstract fun basalMetabolicRateRecordDao(): BasalMetabolicRateRecordDao
    abstract fun cyclingPedalingCadenceRecordDao(): CyclingPedalingCadenceRecordDao
    abstract fun distanceRecordDao(): DistanceRecordDao
    abstract fun elevationGainedRecordDao(): ElevationGainedRecordDao
    abstract fun exerciseSessionRecordDao(): ExerciseSessionRecordDao
    abstract fun floorsClimbedRecordDao(): FloorsClimbedRecordDao
    abstract fun heartRateVariabilityRmssdRecordDao(): HeartRateVariabilityRmssdRecordDao
    abstract fun powerRecordDao(): PowerRecordDao
    abstract fun restingHeartRateRecordDao(): RestingHeartRateRecordDao
    abstract fun speedRecordDao(): SpeedRecordDao
    abstract fun stepsCadenceRecordDao(): StepsCadenceRecordDao
    abstract fun totalCaloriesBurnedRecordDao(): TotalCaloriesBurnedRecordDao
    abstract fun vo2MaxRecordDao(): Vo2MaxRecordDao
    abstract fun bodyFatRecordDao(): BodyFatRecordDao
    abstract fun bodyTemperatureRecordDao(): BodyTemperatureRecordDao
    abstract fun bodyWaterMassRecordDao(): BodyWaterMassRecordDao
    abstract fun boneMassRecordDao(): BoneMassRecordDao
    abstract fun heightRecordDao(): HeightRecordDao
    abstract fun leanBodyMassRecordDao(): LeanBodyMassRecordDao
    abstract fun hydrationRecordDao(): HydrationRecordDao
    abstract fun nutritionRecordDao(): NutritionRecordDao
    abstract fun bloodPressureRecordDao(): BloodPressureRecordDao
    abstract fun oxygenSaturationRecordDao(): OxygenSaturationRecordDao
    abstract fun respiratoryRateRecordDao(): RespiratoryRateRecordDao
}
