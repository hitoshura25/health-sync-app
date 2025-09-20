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
import io.github.hitoshura25.healthsyncapp.data.avro.*
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.*
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.*
import io.github.hitoshura25.healthsyncapp.service.processing.*
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.reflect.KClass

@Module
@InstallIn(SingletonComponent::class)
object ProcessingModule {

    @Provides
    @Singleton
    fun provideRecordProcessorFactory(
        processors: Map<KClass<out Record>, @JvmSuppressWildcards Provider<out RecordProcessor>>
    ): RecordProcessorFactory {
        return RecordProcessorFactory(processors)
    }

    @Provides
    @Singleton
    fun provideRecordProcessors(
        // DAOs
        activeCaloriesBurnedRecordDao: ActiveCaloriesBurnedRecordDao,
        basalBodyTemperatureRecordDao: BasalBodyTemperatureRecordDao,
        basalMetabolicRateRecordDao: BasalMetabolicRateRecordDao,
        bloodGlucoseDao: BloodGlucoseDao,
        bloodPressureRecordDao: BloodPressureRecordDao,
        bodyFatRecordDao: BodyFatRecordDao,
        bodyTemperatureRecordDao: BodyTemperatureRecordDao,
        bodyWaterMassRecordDao: BodyWaterMassRecordDao,
        boneMassRecordDao: BoneMassRecordDao,
        cyclingPedalingCadenceRecordDao: CyclingPedalingCadenceRecordDao,
        distanceRecordDao: DistanceRecordDao,
        elevationGainedRecordDao: ElevationGainedRecordDao,
        exerciseSessionRecordDao: ExerciseSessionRecordDao,
        floorsClimbedRecordDao: FloorsClimbedRecordDao,
        heartRateSampleDao: HeartRateSampleDao,
        heartRateVariabilityRmssdRecordDao: HeartRateVariabilityRmssdRecordDao,
        heightRecordDao: HeightRecordDao,
        hydrationRecordDao: HydrationRecordDao,
        leanBodyMassRecordDao: LeanBodyMassRecordDao,
        nutritionRecordDao: NutritionRecordDao,
        oxygenSaturationRecordDao: OxygenSaturationRecordDao,
        powerRecordDao: PowerRecordDao,
        respiratoryRateRecordDao: RespiratoryRateRecordDao,
        restingHeartRateRecordDao: RestingHeartRateRecordDao,
        speedRecordDao: SpeedRecordDao,
        stepsCadenceRecordDao: StepsCadenceRecordDao,
        stepsRecordDao: StepsRecordDao,
        totalCaloriesBurnedRecordDao: TotalCaloriesBurnedRecordDao,
        vo2MaxRecordDao: Vo2MaxRecordDao,
        weightRecordDao: WeightRecordDao,

        // Specific Processors
        sleepSessionProcessor: Provider<SleepSessionProcessor>,
        heartRateRecordProcessor: Provider<HeartRateRecordProcessor>,
        cyclingPedalingCadenceRecordProcessor: Provider<CyclingPedalingCadenceRecordProcessor>,
        powerRecordProcessor: Provider<PowerRecordProcessor>,
        speedRecordProcessor: Provider<SpeedRecordProcessor>,
        stepsCadenceRecordProcessor: Provider<StepsCadenceRecordProcessor>
    ): Map<KClass<out Record>, Provider<out RecordProcessor>> {
        val processors = mutableMapOf<KClass<out Record>, Provider<out RecordProcessor>>()

        // Specific Processors
        processors[SleepSessionRecord::class] = sleepSessionProcessor
        processors[HeartRateRecord::class] = heartRateRecordProcessor
        processors[CyclingPedalingCadenceRecord::class] = cyclingPedalingCadenceRecordProcessor
        processors[PowerRecord::class] = powerRecordProcessor
        processors[SpeedRecord::class] = speedRecordProcessor
        processors[StepsCadenceRecord::class] = stepsCadenceRecordProcessor

        // Generic Processors
        processors[StepsRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro: AvroStepsRecord -> avro.toStepsRecordEntity() },
                daoInsertFunction = { entities -> stepsRecordDao.insertAll(entities) },
                recordTypeName = "StepsRecord",
                avroTypeClass = AvroStepsRecord::class.java
            )
        }
        processors[ActiveCaloriesBurnedRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro: AvroActiveCaloriesBurnedRecord -> avro.toActiveCaloriesBurnedRecordEntity() },
                daoInsertFunction = { entities -> activeCaloriesBurnedRecordDao.insertAll(entities) },
                recordTypeName = "ActiveCaloriesBurnedRecord",
                avroTypeClass = AvroActiveCaloriesBurnedRecord::class.java
            )
        }
        processors[BasalBodyTemperatureRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro: AvroBasalBodyTemperatureRecord -> avro.toBasalBodyTemperatureRecordEntity() },
                daoInsertFunction = { entities -> basalBodyTemperatureRecordDao.insertAll(entities) },
                recordTypeName = "BasalBodyTemperatureRecord",
                avroTypeClass = AvroBasalBodyTemperatureRecord::class.java
            )
        }
        processors[BasalMetabolicRateRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro: AvroBasalMetabolicRateRecord -> avro.toBasalMetabolicRateRecordEntity() },
                daoInsertFunction = { entities -> basalMetabolicRateRecordDao.insertAll(entities) },
                recordTypeName = "BasalMetabolicRateRecord",
                avroTypeClass = AvroBasalMetabolicRateRecord::class.java
            )
        }
        processors[BloodGlucoseRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro: AvroBloodGlucoseRecord -> avro.toBloodGlucoseEntity() },
                daoInsertFunction = { entities -> bloodGlucoseDao.insertAll(entities) },
                recordTypeName = "BloodGlucoseRecord",
                avroTypeClass = AvroBloodGlucoseRecord::class.java
            )
        }
        processors[BloodPressureRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro: AvroBloodPressureRecord -> avro.toBloodPressureRecordEntity() },
                daoInsertFunction = { entities -> bloodPressureRecordDao.insertAll(entities) },
                recordTypeName = "BloodPressureRecord",
                avroTypeClass = AvroBloodPressureRecord::class.java
            )
        }
        processors[BodyFatRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro: AvroBodyFatRecord -> avro.toBodyFatRecordEntity() },
                daoInsertFunction = { entities -> bodyFatRecordDao.insertAll(entities) },
                recordTypeName = "BodyFatRecord",
                avroTypeClass = AvroBodyFatRecord::class.java
            )
        }
        processors[BodyTemperatureRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro: AvroBodyTemperatureRecord -> avro.toBodyTemperatureRecordEntity() },
                daoInsertFunction = { entities -> bodyTemperatureRecordDao.insertAll(entities) },
                recordTypeName = "BodyTemperatureRecord",
                avroTypeClass = AvroBodyTemperatureRecord::class.java
            )
        }
        processors[BodyWaterMassRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro: AvroBodyWaterMassRecord -> avro.toBodyWaterMassRecordEntity() },
                daoInsertFunction = { entities -> bodyWaterMassRecordDao.insertAll(entities) },
                recordTypeName = "BodyWaterMassRecord",
                avroTypeClass = AvroBodyWaterMassRecord::class.java
            )
        }
        processors[BoneMassRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro: AvroBoneMassRecord -> avro.toBoneMassRecordEntity() },
                daoInsertFunction = { entities -> boneMassRecordDao.insertAll(entities) },
                recordTypeName = "BoneMassRecord",
                avroTypeClass = AvroBoneMassRecord::class.java
            )
        }
        processors[DistanceRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro: AvroDistanceRecord -> avro.toDistanceRecordEntity() },
                daoInsertFunction = { entities -> distanceRecordDao.insertAll(entities) },
                recordTypeName = "DistanceRecord",
                avroTypeClass = AvroDistanceRecord::class.java
            )
        }
        processors[ElevationGainedRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro: AvroElevationGainedRecord -> avro.toElevationGainedRecordEntity() },
                daoInsertFunction = { entities -> elevationGainedRecordDao.insertAll(entities) },
                recordTypeName = "ElevationGainedRecord",
                avroTypeClass = AvroElevationGainedRecord::class.java
            )
        }
        processors[ExerciseSessionRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro: AvroExerciseSessionRecord -> avro.toExerciseSessionRecordEntity() },
                daoInsertFunction = { entities -> exerciseSessionRecordDao.insertAll(entities) },
                recordTypeName = "ExerciseSessionRecord",
                avroTypeClass = AvroExerciseSessionRecord::class.java
            )
        }
        processors[FloorsClimbedRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro: AvroFloorsClimbedRecord -> avro.toFloorsClimbedRecordEntity() },
                daoInsertFunction = { entities -> floorsClimbedRecordDao.insertAll(entities) },
                recordTypeName = "FloorsClimbedRecord",
                avroTypeClass = AvroFloorsClimbedRecord::class.java
            )
        }
        processors[HeartRateVariabilityRmssdRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro: AvroHeartRateVariabilityRmssdRecord -> avro.toHeartRateVariabilityRmssdRecordEntity() },
                daoInsertFunction = { entities -> heartRateVariabilityRmssdRecordDao.insertAll(entities) },
                recordTypeName = "HeartRateVariabilityRmssdRecord",
                avroTypeClass = AvroHeartRateVariabilityRmssdRecord::class.java
            )
        }
        processors[HeightRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro: AvroHeightRecord -> avro.toHeightRecordEntity() },
                daoInsertFunction = { entities -> heightRecordDao.insertAll(entities) },
                recordTypeName = "HeightRecord",
                avroTypeClass = AvroHeightRecord::class.java
            )
        }
        processors[HydrationRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro: AvroHydrationRecord -> avro.toHydrationRecordEntity() },
                daoInsertFunction = { entities -> hydrationRecordDao.insertAll(entities) },
                recordTypeName = "HydrationRecord",
                avroTypeClass = AvroHydrationRecord::class.java
            )
        }
        processors[LeanBodyMassRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro: AvroLeanBodyMassRecord -> avro.toLeanBodyMassRecordEntity() },
                daoInsertFunction = { entities -> leanBodyMassRecordDao.insertAll(entities) },
                recordTypeName = "LeanBodyMassRecord",
                avroTypeClass = AvroLeanBodyMassRecord::class.java
            )
        }
        processors[NutritionRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro: AvroNutritionRecord -> avro.toNutritionRecordEntity() },
                daoInsertFunction = { entities -> nutritionRecordDao.insertAll(entities) },
                recordTypeName = "NutritionRecord",
                avroTypeClass = AvroNutritionRecord::class.java
            )
        }
        processors[OxygenSaturationRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro: AvroOxygenSaturationRecord -> avro.toOxygenSaturationRecordEntity() },
                daoInsertFunction = { entities -> oxygenSaturationRecordDao.insertAll(entities) },
                recordTypeName = "OxygenSaturationRecord",
                avroTypeClass = AvroOxygenSaturationRecord::class.java
            )
        }
        processors[RespiratoryRateRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro: AvroRespiratoryRateRecord -> avro.toRespiratoryRateRecordEntity() },
                daoInsertFunction = { entities -> respiratoryRateRecordDao.insertAll(entities) },
                recordTypeName = "RespiratoryRateRecord",
                avroTypeClass = AvroRespiratoryRateRecord::class.java
            )
        }
        processors[RestingHeartRateRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro: AvroRestingHeartRateRecord -> avro.toRestingHeartRateRecordEntity() },
                daoInsertFunction = { entities -> restingHeartRateRecordDao.insertAll(entities) },
                recordTypeName = "RestingHeartRateRecord",
                avroTypeClass = AvroRestingHeartRateRecord::class.java
            )
        }
        processors[TotalCaloriesBurnedRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro: AvroTotalCaloriesBurnedRecord -> avro.toTotalCaloriesBurnedRecordEntity() },
                daoInsertFunction = { entities -> totalCaloriesBurnedRecordDao.insertAll(entities) },
                recordTypeName = "TotalCaloriesBurnedRecord",
                avroTypeClass = AvroTotalCaloriesBurnedRecord::class.java
            )
        }
        processors[Vo2MaxRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro: AvroVo2MaxRecord -> avro.toVo2MaxRecordEntity() },
                daoInsertFunction = { entities -> vo2MaxRecordDao.insertAll(entities) },
                recordTypeName = "Vo2MaxRecord",
                avroTypeClass = AvroVo2MaxRecord::class.java
            )
        }
        processors[WeightRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro: AvroWeightRecord -> avro.toWeightRecordEntity() },
                daoInsertFunction = { entities -> weightRecordDao.insertAll(entities) },
                recordTypeName = "WeightRecord",
                avroTypeClass = AvroWeightRecord::class.java
            )
        }
        return processors
    }
}
