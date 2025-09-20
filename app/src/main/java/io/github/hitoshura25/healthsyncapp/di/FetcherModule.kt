package io.github.hitoshura25.healthsyncapp.di

import androidx.health.connect.client.records.Record
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.hitoshura25.healthsyncapp.worker.fetcher.ActiveCaloriesBurnedRecordFetcher
import io.github.hitoshura25.healthsyncapp.worker.fetcher.BasalBodyTemperatureRecordFetcher
import io.github.hitoshura25.healthsyncapp.worker.fetcher.BasalMetabolicRateRecordFetcher
import io.github.hitoshura25.healthsyncapp.worker.fetcher.BloodGlucoseRecordFetcher
import io.github.hitoshura25.healthsyncapp.worker.fetcher.BloodPressureRecordFetcher
import io.github.hitoshura25.healthsyncapp.worker.fetcher.BodyFatRecordFetcher
import io.github.hitoshura25.healthsyncapp.worker.fetcher.BodyTemperatureRecordFetcher
import io.github.hitoshura25.healthsyncapp.worker.fetcher.BodyWaterMassRecordFetcher
import io.github.hitoshura25.healthsyncapp.worker.fetcher.BoneMassRecordFetcher
import io.github.hitoshura25.healthsyncapp.worker.fetcher.CyclingPedalingCadenceRecordFetcher
import io.github.hitoshura25.healthsyncapp.worker.fetcher.DistanceRecordFetcher
import io.github.hitoshura25.healthsyncapp.worker.fetcher.ElevationGainedRecordFetcher
import io.github.hitoshura25.healthsyncapp.worker.fetcher.ExerciseSessionRecordFetcher
import io.github.hitoshura25.healthsyncapp.worker.fetcher.FloorsClimbedRecordFetcher
import io.github.hitoshura25.healthsyncapp.worker.fetcher.HeartRateRecordFetcher
import io.github.hitoshura25.healthsyncapp.worker.fetcher.HeartRateVariabilityRmssdRecordFetcher
import io.github.hitoshura25.healthsyncapp.worker.fetcher.HeightRecordFetcher
import io.github.hitoshura25.healthsyncapp.worker.fetcher.HydrationRecordFetcher
import io.github.hitoshura25.healthsyncapp.worker.fetcher.LeanBodyMassRecordFetcher
import io.github.hitoshura25.healthsyncapp.worker.fetcher.NutritionRecordFetcher
import io.github.hitoshura25.healthsyncapp.worker.fetcher.OxygenSaturationRecordFetcher
import io.github.hitoshura25.healthsyncapp.worker.fetcher.PowerRecordFetcher
import io.github.hitoshura25.healthsyncapp.worker.fetcher.RecordFetcher
import io.github.hitoshura25.healthsyncapp.worker.fetcher.RecordFetcherFactory
import io.github.hitoshura25.healthsyncapp.worker.fetcher.RespiratoryRateRecordFetcher
import io.github.hitoshura25.healthsyncapp.worker.fetcher.RestingHeartRateRecordFetcher
import io.github.hitoshura25.healthsyncapp.worker.fetcher.SleepSessionRecordFetcher
import io.github.hitoshura25.healthsyncapp.worker.fetcher.SpeedRecordFetcher
import io.github.hitoshura25.healthsyncapp.worker.fetcher.StepsCadenceRecordFetcher
import io.github.hitoshura25.healthsyncapp.worker.fetcher.StepsRecordFetcher
import io.github.hitoshura25.healthsyncapp.worker.fetcher.TotalCaloriesBurnedRecordFetcher
import io.github.hitoshura25.healthsyncapp.worker.fetcher.Vo2MaxRecordFetcher
import io.github.hitoshura25.healthsyncapp.worker.fetcher.WeightRecordFetcher
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.reflect.KClass

@Module
@InstallIn(SingletonComponent::class)
object FetcherModule {

    @Provides
    @Singleton
    fun provideRecordFetcherFactory(
        fetchers: Map<KClass<out Record>, @JvmSuppressWildcards Provider<out RecordFetcher>>
    ): RecordFetcherFactory {
        return RecordFetcherFactory(fetchers)
    }

    @Provides
    @Singleton
    fun provideRecordFetchers(
        activeCaloriesBurnedRecordFetcher: Provider<ActiveCaloriesBurnedRecordFetcher>,
        basalBodyTemperatureRecordFetcher: Provider<BasalBodyTemperatureRecordFetcher>,
        basalMetabolicRateRecordFetcher: Provider<BasalMetabolicRateRecordFetcher>,
        bloodGlucoseRecordFetcher: Provider<BloodGlucoseRecordFetcher>,
        bloodPressureRecordFetcher: Provider<BloodPressureRecordFetcher>,
        bodyFatRecordFetcher: Provider<BodyFatRecordFetcher>,
        bodyTemperatureRecordFetcher: Provider<BodyTemperatureRecordFetcher>,
        bodyWaterMassRecordFetcher: Provider<BodyWaterMassRecordFetcher>,
        boneMassRecordFetcher: Provider<BoneMassRecordFetcher>,
        cyclingPedalingCadenceRecordFetcher: Provider<CyclingPedalingCadenceRecordFetcher>,
        distanceRecordFetcher: Provider<DistanceRecordFetcher>,
        elevationGainedRecordFetcher: Provider<ElevationGainedRecordFetcher>,
        exerciseSessionRecordFetcher: Provider<ExerciseSessionRecordFetcher>,
        floorsClimbedRecordFetcher: Provider<FloorsClimbedRecordFetcher>,
        heartRateRecordFetcher: Provider<HeartRateRecordFetcher>,
        heartRateVariabilityRmssdRecordFetcher: Provider<HeartRateVariabilityRmssdRecordFetcher>,
        heightRecordFetcher: Provider<HeightRecordFetcher>,
        hydrationRecordFetcher: Provider<HydrationRecordFetcher>,
        leanBodyMassRecordFetcher: Provider<LeanBodyMassRecordFetcher>,
        nutritionRecordFetcher: Provider<NutritionRecordFetcher>,
        oxygenSaturationRecordFetcher: Provider<OxygenSaturationRecordFetcher>,
        powerRecordFetcher: Provider<PowerRecordFetcher>,
        respiratoryRateRecordFetcher: Provider<RespiratoryRateRecordFetcher>,
        restingHeartRateRecordFetcher: Provider<RestingHeartRateRecordFetcher>,
        sleepSessionRecordFetcher: Provider<SleepSessionRecordFetcher>,
        speedRecordFetcher: Provider<SpeedRecordFetcher>,
        stepsCadenceRecordFetcher: Provider<StepsCadenceRecordFetcher>,
        stepsRecordFetcher: Provider<StepsRecordFetcher>,
        totalCaloriesBurnedRecordFetcher: Provider<TotalCaloriesBurnedRecordFetcher>,
        vo2MaxRecordFetcher: Provider<Vo2MaxRecordFetcher>,
        weightRecordFetcher: Provider<WeightRecordFetcher>
    ): Map<KClass<out Record>, @JvmSuppressWildcards Provider<out RecordFetcher>> {
        val fetchers = mutableMapOf<KClass<out Record>, Provider<out RecordFetcher>>()
        fetchers[androidx.health.connect.client.records.ActiveCaloriesBurnedRecord::class] = activeCaloriesBurnedRecordFetcher
        fetchers[androidx.health.connect.client.records.BasalBodyTemperatureRecord::class] = basalBodyTemperatureRecordFetcher
        fetchers[androidx.health.connect.client.records.BasalMetabolicRateRecord::class] = basalMetabolicRateRecordFetcher
        fetchers[androidx.health.connect.client.records.BloodGlucoseRecord::class] = bloodGlucoseRecordFetcher
        fetchers[androidx.health.connect.client.records.BloodPressureRecord::class] = bloodPressureRecordFetcher
        fetchers[androidx.health.connect.client.records.BodyFatRecord::class] = bodyFatRecordFetcher
        fetchers[androidx.health.connect.client.records.BodyTemperatureRecord::class] = bodyTemperatureRecordFetcher
        fetchers[androidx.health.connect.client.records.BodyWaterMassRecord::class] = bodyWaterMassRecordFetcher
        fetchers[androidx.health.connect.client.records.BoneMassRecord::class] = boneMassRecordFetcher
        fetchers[androidx.health.connect.client.records.CyclingPedalingCadenceRecord::class] = cyclingPedalingCadenceRecordFetcher
        fetchers[androidx.health.connect.client.records.DistanceRecord::class] = distanceRecordFetcher
        fetchers[androidx.health.connect.client.records.ElevationGainedRecord::class] = elevationGainedRecordFetcher
        fetchers[androidx.health.connect.client.records.ExerciseSessionRecord::class] = exerciseSessionRecordFetcher
        fetchers[androidx.health.connect.client.records.FloorsClimbedRecord::class] = floorsClimbedRecordFetcher
        fetchers[androidx.health.connect.client.records.HeartRateRecord::class] = heartRateRecordFetcher
        fetchers[androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord::class] = heartRateVariabilityRmssdRecordFetcher
        fetchers[androidx.health.connect.client.records.HeightRecord::class] = heightRecordFetcher
        fetchers[androidx.health.connect.client.records.HydrationRecord::class] = hydrationRecordFetcher
        fetchers[androidx.health.connect.client.records.LeanBodyMassRecord::class] = leanBodyMassRecordFetcher
        fetchers[androidx.health.connect.client.records.NutritionRecord::class] = nutritionRecordFetcher
        fetchers[androidx.health.connect.client.records.OxygenSaturationRecord::class] = oxygenSaturationRecordFetcher
        fetchers[androidx.health.connect.client.records.PowerRecord::class] = powerRecordFetcher
        fetchers[androidx.health.connect.client.records.RespiratoryRateRecord::class] = respiratoryRateRecordFetcher
        fetchers[androidx.health.connect.client.records.RestingHeartRateRecord::class] = restingHeartRateRecordFetcher
        fetchers[androidx.health.connect.client.records.SleepSessionRecord::class] = sleepSessionRecordFetcher
        fetchers[androidx.health.connect.client.records.SpeedRecord::class] = speedRecordFetcher
        fetchers[androidx.health.connect.client.records.StepsCadenceRecord::class] = stepsCadenceRecordFetcher
        fetchers[androidx.health.connect.client.records.StepsRecord::class] = stepsRecordFetcher
        fetchers[androidx.health.connect.client.records.TotalCaloriesBurnedRecord::class] = totalCaloriesBurnedRecordFetcher
        fetchers[androidx.health.connect.client.records.Vo2MaxRecord::class] = vo2MaxRecordFetcher
        fetchers[androidx.health.connect.client.records.WeightRecord::class] = weightRecordFetcher
        return fetchers
    }
}
