package io.github.hitoshura25.healthsyncapp.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

import io.github.hitoshura25.healthsyncapp.data.local.database.AppDatabase
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BloodGlucoseDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.HeartRateSampleDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.SleepSessionDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.SleepStageDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.StepsRecordDao
import io.github.hitoshura25.healthsyncapp.file.FileHandler
import io.github.hitoshura25.healthsyncapp.file.FileHandlerImpl
import javax.inject.Singleton

import io.github.hitoshura25.healthsyncapp.data.local.database.dao.ActiveCaloriesBurnedRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BasalBodyTemperatureRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BasalMetabolicRateRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BloodPressureRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BodyFatRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BodyTemperatureRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BodyWaterMassRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BoneMassRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.CyclingPedalingCadenceRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.DistanceRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.ElevationGainedRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.ExerciseSessionRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.FloorsClimbedRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.HeartRateVariabilityRmssdRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.HeightRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.HydrationRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.LeanBodyMassRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.NutritionRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.OxygenSaturationRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.PowerRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.RespiratoryRateRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.RestingHeartRateRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.SpeedRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.StepsCadenceRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.TotalCaloriesBurnedRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.Vo2MaxRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.WeightRecordDao

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "health_sync_app_database"
        )
            .fallbackToDestructiveMigration(true) // DB is not a priority, Avro files are
        .build()
    }

    @Provides
    @Singleton
    fun provideStepsRecordDao(appDatabase: AppDatabase): StepsRecordDao {
        return appDatabase.stepsRecordDao()
    }

    @Provides
    @Singleton
    fun provideHeartRateSampleDao(appDatabase: AppDatabase): HeartRateSampleDao {
        return appDatabase.heartRateSampleDao()
    }

    @Provides
    @Singleton
    fun provideSleepSessionDao(appDatabase: AppDatabase): SleepSessionDao {
        return appDatabase.sleepSessionDao()
    }

    @Provides
    @Singleton
    fun provideSleepStageDao(appDatabase: AppDatabase): SleepStageDao {
        return appDatabase.sleepStageDao()
    }

    @Provides
    @Singleton
    fun provideBloodGlucoseDao(appDatabase: AppDatabase): BloodGlucoseDao {
        return appDatabase.bloodGlucoseDao()
    }

    @Provides
    @Singleton
    fun provideFileHandler(@ApplicationContext appContext: Context): FileHandler {
        return FileHandlerImpl(appContext)
    }

    @Provides
    @Singleton
    fun provideActiveCaloriesBurnedRecordDao(appDatabase: AppDatabase): ActiveCaloriesBurnedRecordDao {
        return appDatabase.activeCaloriesBurnedRecordDao()
    }

    @Provides
    @Singleton
    fun provideBasalBodyTemperatureRecordDao(appDatabase: AppDatabase): BasalBodyTemperatureRecordDao {
        return appDatabase.basalBodyTemperatureRecordDao()
    }

    @Provides
    @Singleton
    fun provideBasalMetabolicRateRecordDao(appDatabase: AppDatabase): BasalMetabolicRateRecordDao {
        return appDatabase.basalMetabolicRateRecordDao()
    }

    @Provides
    @Singleton
    fun provideBloodPressureRecordDao(appDatabase: AppDatabase): BloodPressureRecordDao {
        return appDatabase.bloodPressureRecordDao()
    }

    @Provides
    @Singleton
    fun provideBodyFatRecordDao(appDatabase: AppDatabase): BodyFatRecordDao {
        return appDatabase.bodyFatRecordDao()
    }

    @Provides
    @Singleton
    fun provideBodyTemperatureRecordDao(appDatabase: AppDatabase): BodyTemperatureRecordDao {
        return appDatabase.bodyTemperatureRecordDao()
    }

    @Provides
    @Singleton
    fun provideBodyWaterMassRecordDao(appDatabase: AppDatabase): BodyWaterMassRecordDao {
        return appDatabase.bodyWaterMassRecordDao()
    }

    @Provides
    @Singleton
    fun provideBoneMassRecordDao(appDatabase: AppDatabase): BoneMassRecordDao {
        return appDatabase.boneMassRecordDao()
    }

    @Provides
    @Singleton
    fun provideCyclingPedalingCadenceRecordDao(appDatabase: AppDatabase): CyclingPedalingCadenceRecordDao {
        return appDatabase.cyclingPedalingCadenceRecordDao()
    }

    @Provides
    @Singleton
    fun provideDistanceRecordDao(appDatabase: AppDatabase): DistanceRecordDao {
        return appDatabase.distanceRecordDao()
    }

    @Provides
    @Singleton
    fun provideElevationGainedRecordDao(appDatabase: AppDatabase): ElevationGainedRecordDao {
        return appDatabase.elevationGainedRecordDao()
    }

    @Provides
    @Singleton
    fun provideExerciseSessionRecordDao(appDatabase: AppDatabase): ExerciseSessionRecordDao {
        return appDatabase.exerciseSessionRecordDao()
    }

    @Provides
    @Singleton
    fun provideFloorsClimbedRecordDao(appDatabase: AppDatabase): FloorsClimbedRecordDao {
        return appDatabase.floorsClimbedRecordDao()
    }

    @Provides
    @Singleton
    fun provideHeartRateVariabilityRmssdRecordDao(appDatabase: AppDatabase): HeartRateVariabilityRmssdRecordDao {
        return appDatabase.heartRateVariabilityRmssdRecordDao()
    }

    @Provides
    @Singleton
    fun provideHeightRecordDao(appDatabase: AppDatabase): HeightRecordDao {
        return appDatabase.heightRecordDao()
    }

    @Provides
    @Singleton
    fun provideHydrationRecordDao(appDatabase: AppDatabase): HydrationRecordDao {
        return appDatabase.hydrationRecordDao()
    }

    @Provides
    @Singleton
    fun provideLeanBodyMassRecordDao(appDatabase: AppDatabase): LeanBodyMassRecordDao {
        return appDatabase.leanBodyMassRecordDao()
    }

    @Provides
    @Singleton
    fun provideNutritionRecordDao(appDatabase: AppDatabase): NutritionRecordDao {
        return appDatabase.nutritionRecordDao()
    }

    @Provides
    @Singleton
    fun provideOxygenSaturationRecordDao(appDatabase: AppDatabase): OxygenSaturationRecordDao {
        return appDatabase.oxygenSaturationRecordDao()
    }

    @Provides
    @Singleton
    fun providePowerRecordDao(appDatabase: AppDatabase): PowerRecordDao {
        return appDatabase.powerRecordDao()
    }

    @Provides
    @Singleton
    fun provideRespiratoryRateRecordDao(appDatabase: AppDatabase): RespiratoryRateRecordDao {
        return appDatabase.respiratoryRateRecordDao()
    }

    @Provides
    @Singleton
    fun provideRestingHeartRateRecordDao(appDatabase: AppDatabase): RestingHeartRateRecordDao {
        return appDatabase.restingHeartRateRecordDao()
    }

    @Provides
    @Singleton
    fun provideSpeedRecordDao(appDatabase: AppDatabase): SpeedRecordDao {
        return appDatabase.speedRecordDao()
    }

    @Provides
    @Singleton
    fun provideStepsCadenceRecordDao(appDatabase: AppDatabase): StepsCadenceRecordDao {
        return appDatabase.stepsCadenceRecordDao()
    }

    @Provides
    @Singleton
    fun provideTotalCaloriesBurnedRecordDao(appDatabase: AppDatabase): TotalCaloriesBurnedRecordDao {
        return appDatabase.totalCaloriesBurnedRecordDao()
    }

    @Provides
    @Singleton
    fun provideVo2MaxRecordDao(appDatabase: AppDatabase): Vo2MaxRecordDao {
        return appDatabase.vo2MaxRecordDao()
    }

    @Provides
    @Singleton
    fun provideWeightRecordDao(appDatabase: AppDatabase): WeightRecordDao {
        return appDatabase.weightRecordDao()
    }
}