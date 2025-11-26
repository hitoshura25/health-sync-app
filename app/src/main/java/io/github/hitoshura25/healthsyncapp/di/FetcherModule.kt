package io.github.hitoshura25.healthsyncapp.di

import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.BasalBodyTemperatureRecord
import androidx.health.connect.client.records.BasalMetabolicRateRecord
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.BodyTemperatureRecord
import androidx.health.connect.client.records.BodyWaterMassRecord
import androidx.health.connect.client.records.BoneMassRecord
import androidx.health.connect.client.records.CyclingPedalingCadenceRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ElevationGainedRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.FloorsClimbedRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.HeightRecord
import androidx.health.connect.client.records.HydrationRecord
import androidx.health.connect.client.records.LeanBodyMassRecord
import androidx.health.connect.client.records.NutritionRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.PowerRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.RespiratoryRateRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.SpeedRecord
import androidx.health.connect.client.records.StepsCadenceRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.Vo2MaxRecord
import androidx.health.connect.client.records.WeightRecord
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.hitoshura25.healthsyncapp.data.HealthConnectConstants.RECORD_TYPES_SUPPORTED
import io.github.hitoshura25.healthsyncapp.data.SupportedHealthRecordType.ActiveCaloriesBurned
import io.github.hitoshura25.healthsyncapp.data.SupportedHealthRecordType.BasalBodyTemperature
import io.github.hitoshura25.healthsyncapp.data.SupportedHealthRecordType.BasalMetabolicRate
import io.github.hitoshura25.healthsyncapp.data.SupportedHealthRecordType.BloodGlucose
import io.github.hitoshura25.healthsyncapp.data.SupportedHealthRecordType.BloodPressure
import io.github.hitoshura25.healthsyncapp.data.SupportedHealthRecordType.BodyFat
import io.github.hitoshura25.healthsyncapp.data.SupportedHealthRecordType.BodyTemperature
import io.github.hitoshura25.healthsyncapp.data.SupportedHealthRecordType.BodyWaterMass
import io.github.hitoshura25.healthsyncapp.data.SupportedHealthRecordType.BoneMass
import io.github.hitoshura25.healthsyncapp.data.SupportedHealthRecordType.CyclingPedalingCadence
import io.github.hitoshura25.healthsyncapp.data.SupportedHealthRecordType.Distance
import io.github.hitoshura25.healthsyncapp.data.SupportedHealthRecordType.ElevationGained
import io.github.hitoshura25.healthsyncapp.data.SupportedHealthRecordType.ExerciseSession
import io.github.hitoshura25.healthsyncapp.data.SupportedHealthRecordType.FloorsClimbed
import io.github.hitoshura25.healthsyncapp.data.SupportedHealthRecordType.HeartRate
import io.github.hitoshura25.healthsyncapp.data.SupportedHealthRecordType.HeartRateVariabilityRmssd
import io.github.hitoshura25.healthsyncapp.data.SupportedHealthRecordType.Height
import io.github.hitoshura25.healthsyncapp.data.SupportedHealthRecordType.Hydration
import io.github.hitoshura25.healthsyncapp.data.SupportedHealthRecordType.LeanBodyMass
import io.github.hitoshura25.healthsyncapp.data.SupportedHealthRecordType.Nutrition
import io.github.hitoshura25.healthsyncapp.data.SupportedHealthRecordType.OxygenSaturation
import io.github.hitoshura25.healthsyncapp.data.SupportedHealthRecordType.Power
import io.github.hitoshura25.healthsyncapp.data.SupportedHealthRecordType.RespiratoryRate
import io.github.hitoshura25.healthsyncapp.data.SupportedHealthRecordType.RestingHeartRate
import io.github.hitoshura25.healthsyncapp.data.SupportedHealthRecordType.SleepSession
import io.github.hitoshura25.healthsyncapp.data.SupportedHealthRecordType.Speed
import io.github.hitoshura25.healthsyncapp.data.SupportedHealthRecordType.Steps
import io.github.hitoshura25.healthsyncapp.data.SupportedHealthRecordType.StepsCadence
import io.github.hitoshura25.healthsyncapp.data.SupportedHealthRecordType.TotalCaloriesBurned
import io.github.hitoshura25.healthsyncapp.data.SupportedHealthRecordType.Vo2Max
import io.github.hitoshura25.healthsyncapp.data.SupportedHealthRecordType.Weight
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
        RECORD_TYPES_SUPPORTED.forEach { supportedHealthRecordType ->
            when (supportedHealthRecordType) {
                ActiveCaloriesBurned -> fetchers[ActiveCaloriesBurnedRecord::class] = activeCaloriesBurnedRecordFetcher
                BasalBodyTemperature -> fetchers[BasalBodyTemperatureRecord::class] = basalBodyTemperatureRecordFetcher
                BasalMetabolicRate -> fetchers[BasalMetabolicRateRecord::class] = basalMetabolicRateRecordFetcher
                BloodGlucose -> fetchers[BloodGlucoseRecord::class] = bloodGlucoseRecordFetcher
                BloodPressure -> fetchers[BloodPressureRecord::class] = bloodPressureRecordFetcher
                BodyFat -> fetchers[BodyFatRecord::class] = bodyFatRecordFetcher
                BodyTemperature -> fetchers[BodyTemperatureRecord::class] = bodyTemperatureRecordFetcher
                BodyWaterMass -> fetchers[BodyWaterMassRecord::class] = bodyWaterMassRecordFetcher
                BoneMass -> fetchers[BoneMassRecord::class] = boneMassRecordFetcher
                CyclingPedalingCadence -> fetchers[CyclingPedalingCadenceRecord::class] = cyclingPedalingCadenceRecordFetcher
                Distance -> fetchers[DistanceRecord::class] = distanceRecordFetcher
                ElevationGained -> fetchers[ElevationGainedRecord::class] = elevationGainedRecordFetcher
                ExerciseSession -> fetchers[ExerciseSessionRecord::class] = exerciseSessionRecordFetcher
                FloorsClimbed -> fetchers[FloorsClimbedRecord::class] = floorsClimbedRecordFetcher
                HeartRate -> fetchers[HeartRateRecord::class] = heartRateRecordFetcher
                HeartRateVariabilityRmssd -> fetchers[HeartRateVariabilityRmssdRecord::class] = heartRateVariabilityRmssdRecordFetcher
                Height -> fetchers[HeightRecord::class] = heightRecordFetcher
                Hydration -> fetchers[HydrationRecord::class] = hydrationRecordFetcher
                LeanBodyMass -> fetchers[LeanBodyMassRecord::class] = leanBodyMassRecordFetcher
                Nutrition -> fetchers[NutritionRecord::class] = nutritionRecordFetcher
                OxygenSaturation -> fetchers[OxygenSaturationRecord::class] = oxygenSaturationRecordFetcher
                Power -> fetchers[PowerRecord::class] = powerRecordFetcher
                RespiratoryRate -> fetchers[RespiratoryRateRecord::class] = respiratoryRateRecordFetcher
                RestingHeartRate -> fetchers[RestingHeartRateRecord::class] = restingHeartRateRecordFetcher
                SleepSession -> fetchers[SleepSessionRecord::class] = sleepSessionRecordFetcher
                Speed -> fetchers[SpeedRecord::class] = speedRecordFetcher
                Steps -> fetchers[StepsRecord::class] = stepsRecordFetcher
                StepsCadence -> fetchers[StepsCadenceRecord::class] = stepsCadenceRecordFetcher
                TotalCaloriesBurned -> fetchers[TotalCaloriesBurnedRecord::class] = totalCaloriesBurnedRecordFetcher
                Vo2Max -> fetchers[Vo2MaxRecord::class] = vo2MaxRecordFetcher
                Weight -> fetchers[WeightRecord::class] = weightRecordFetcher
            }
        }
        return fetchers
    }
}
