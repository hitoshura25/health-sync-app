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
import io.github.hitoshura25.healthsyncapp.data.avro.AvroActiveCaloriesBurnedRecord
import io.github.hitoshura25.healthsyncapp.data.avro.AvroBasalBodyTemperatureRecord
import io.github.hitoshura25.healthsyncapp.data.avro.AvroBasalMetabolicRateRecord
import io.github.hitoshura25.healthsyncapp.data.avro.AvroBloodGlucoseRecord
import io.github.hitoshura25.healthsyncapp.data.avro.AvroBloodPressureRecord
import io.github.hitoshura25.healthsyncapp.data.avro.AvroBodyFatRecord
import io.github.hitoshura25.healthsyncapp.data.avro.AvroBodyTemperatureRecord
import io.github.hitoshura25.healthsyncapp.data.avro.AvroBodyWaterMassRecord
import io.github.hitoshura25.healthsyncapp.data.avro.AvroBoneMassRecord
import io.github.hitoshura25.healthsyncapp.data.avro.AvroDistanceRecord
import io.github.hitoshura25.healthsyncapp.data.avro.AvroElevationGainedRecord
import io.github.hitoshura25.healthsyncapp.data.avro.AvroExerciseSessionRecord
import io.github.hitoshura25.healthsyncapp.data.avro.AvroFloorsClimbedRecord
import io.github.hitoshura25.healthsyncapp.data.avro.AvroHeartRateVariabilityRmssdRecord
import io.github.hitoshura25.healthsyncapp.data.avro.AvroHeightRecord
import io.github.hitoshura25.healthsyncapp.data.avro.AvroHydrationRecord
import io.github.hitoshura25.healthsyncapp.data.avro.AvroLeanBodyMassRecord
import io.github.hitoshura25.healthsyncapp.data.avro.AvroNutritionRecord
import io.github.hitoshura25.healthsyncapp.data.avro.AvroOxygenSaturationRecord
import io.github.hitoshura25.healthsyncapp.data.avro.AvroRespiratoryRateRecord
import io.github.hitoshura25.healthsyncapp.data.avro.AvroRestingHeartRateRecord
import io.github.hitoshura25.healthsyncapp.data.avro.AvroTotalCaloriesBurnedRecord
import io.github.hitoshura25.healthsyncapp.data.avro.AvroVo2MaxRecord
import io.github.hitoshura25.healthsyncapp.data.avro.AvroWeightRecord
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.ActiveCaloriesBurnedRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BasalBodyTemperatureRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BasalMetabolicRateRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BloodGlucoseDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BloodPressureRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BodyFatRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BodyTemperatureRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BodyWaterMassRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BoneMassRecordDao
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
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.RespiratoryRateRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.RestingHeartRateRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.TotalCaloriesBurnedRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.Vo2MaxRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.WeightRecordDao
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toActiveCaloriesBurnedRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toBasalBodyTemperatureRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toBasalMetabolicRateRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toBloodGlucoseEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toBloodPressureRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toBodyFatRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toBodyTemperatureRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toBodyWaterMassRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toBoneMassRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toDistanceRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toElevationGainedRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toExerciseSessionRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toFloorsClimbedRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toHeartRateVariabilityRmssdRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toHeightRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toHydrationRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toLeanBodyMassRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toNutritionRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toOxygenSaturationRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toRespiratoryRateRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toRestingHeartRateRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toTotalCaloriesBurnedRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toVo2MaxRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toWeightRecordEntity
import io.github.hitoshura25.healthsyncapp.service.processing.CyclingPedalingCadenceRecordProcessor
import io.github.hitoshura25.healthsyncapp.service.processing.HeartRateRecordProcessor
import io.github.hitoshura25.healthsyncapp.service.processing.PowerRecordProcessor
import io.github.hitoshura25.healthsyncapp.service.processing.RecordProcessor
import io.github.hitoshura25.healthsyncapp.service.processing.RecordProcessorFactory
import io.github.hitoshura25.healthsyncapp.service.processing.SingleRecordTypeProcessor
import io.github.hitoshura25.healthsyncapp.service.processing.SleepSessionProcessor
import io.github.hitoshura25.healthsyncapp.service.processing.SpeedRecordProcessor
import io.github.hitoshura25.healthsyncapp.service.processing.StepsCadenceRecordProcessor
import io.github.hitoshura25.healthsyncapp.service.processing.StepsRecordProcessor
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
        distanceRecordDao: DistanceRecordDao,
        elevationGainedRecordDao: ElevationGainedRecordDao,
        exerciseSessionRecordDao: ExerciseSessionRecordDao,
        floorsClimbedRecordDao: FloorsClimbedRecordDao,
        heartRateVariabilityRmssdRecordDao: HeartRateVariabilityRmssdRecordDao,
        heightRecordDao: HeightRecordDao,
        hydrationRecordDao: HydrationRecordDao,
        leanBodyMassRecordDao: LeanBodyMassRecordDao,
        nutritionRecordDao: NutritionRecordDao,
        oxygenSaturationRecordDao: OxygenSaturationRecordDao,
        respiratoryRateRecordDao: RespiratoryRateRecordDao,
        restingHeartRateRecordDao: RestingHeartRateRecordDao,
        totalCaloriesBurnedRecordDao: TotalCaloriesBurnedRecordDao,
        vo2MaxRecordDao: Vo2MaxRecordDao,
        weightRecordDao: WeightRecordDao,

        // Specific Processors
        stepsRecordProcessor: Provider<StepsRecordProcessor>,
        sleepSessionProcessor: Provider<SleepSessionProcessor>,
        cyclingPedalingCadenceRecordProcessor: Provider<CyclingPedalingCadenceRecordProcessor>,
        heartRateRecordProcessor: Provider<HeartRateRecordProcessor>,
        powerRecordProcessor: Provider<PowerRecordProcessor>,
        speedRecordProcessor: Provider<SpeedRecordProcessor>,
        stepsCadenceRecordProcessor: Provider<StepsCadenceRecordProcessor>
    ): Map<KClass<out Record>,  Provider<out RecordProcessor>> {
        val processors = mutableMapOf<KClass<out Record>, Provider<out RecordProcessor>>()

        // Specific Processors
        processors[StepsRecord::class] = stepsRecordProcessor
        processors[SleepSessionRecord::class] = sleepSessionProcessor
        processors[CyclingPedalingCadenceRecord::class] = cyclingPedalingCadenceRecordProcessor
        processors[HeartRateRecord::class] = heartRateRecordProcessor
        processors[PowerRecord::class] = powerRecordProcessor
        processors[SpeedRecord::class] = speedRecordProcessor
        processors[StepsCadenceRecord::class] = stepsCadenceRecordProcessor

        // Generic Processors
        processors[ActiveCaloriesBurnedRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> avro.toActiveCaloriesBurnedRecordEntity() },
                daoInsertFunction = { entities -> activeCaloriesBurnedRecordDao.insertAll(entities) },
                recordTypeName = "ActiveCaloriesBurnedRecord",
                avroTypeClass = AvroActiveCaloriesBurnedRecord::class.java
            )
        }
        processors[BasalBodyTemperatureRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> avro.toBasalBodyTemperatureRecordEntity() },
                daoInsertFunction = { entities -> basalBodyTemperatureRecordDao.insertAll(entities) },
                recordTypeName = "BasalBodyTemperatureRecord",
                avroTypeClass = AvroBasalBodyTemperatureRecord::class.java
            )
        }
        processors[BasalMetabolicRateRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> avro.toBasalMetabolicRateRecordEntity() },
                daoInsertFunction = { entities -> basalMetabolicRateRecordDao.insertAll(entities) },
                recordTypeName = "BasalMetabolicRateRecord",
                avroTypeClass = AvroBasalMetabolicRateRecord::class.java
            )
        }
        processors[BloodGlucoseRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> avro.toBloodGlucoseEntity() },
                daoInsertFunction = { entities -> bloodGlucoseDao.insertAll(entities) },
                recordTypeName = "BloodGlucoseRecord",
                avroTypeClass = AvroBloodGlucoseRecord::class.java
            )
        }
        processors[BloodPressureRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> avro.toBloodPressureRecordEntity() },
                daoInsertFunction = { entities -> bloodPressureRecordDao.insertAll(entities) },
                recordTypeName = "BloodPressureRecord",
                avroTypeClass = AvroBloodPressureRecord::class.java
            )
        }
        processors[BodyFatRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> avro.toBodyFatRecordEntity() },
                daoInsertFunction = { entities -> bodyFatRecordDao.insertAll(entities) },
                recordTypeName = "BodyFatRecord",
                avroTypeClass = AvroBodyFatRecord::class.java
            )
        }
        processors[BodyTemperatureRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> avro.toBodyTemperatureRecordEntity() },
                daoInsertFunction = { entities -> bodyTemperatureRecordDao.insertAll(entities) },
                recordTypeName = "BodyTemperatureRecord",
                avroTypeClass = AvroBodyTemperatureRecord::class.java
            )
        }
        processors[BodyWaterMassRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> avro.toBodyWaterMassRecordEntity() },
                daoInsertFunction = { entities -> bodyWaterMassRecordDao.insertAll(entities) },
                recordTypeName = "BodyWaterMassRecord",
                avroTypeClass = AvroBodyWaterMassRecord::class.java
            )
        }
        processors[BoneMassRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> avro.toBoneMassRecordEntity() },
                daoInsertFunction = { entities -> boneMassRecordDao.insertAll(entities) },
                recordTypeName = "BoneMassRecord",
                avroTypeClass = AvroBoneMassRecord::class.java
            )
        }
        processors[DistanceRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> avro.toDistanceRecordEntity() },
                daoInsertFunction = { entities -> distanceRecordDao.insertAll(entities) },
                recordTypeName = "DistanceRecord",
                avroTypeClass = AvroDistanceRecord::class.java
            )
        }
        processors[ElevationGainedRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> avro.toElevationGainedRecordEntity() },
                daoInsertFunction = { entities -> elevationGainedRecordDao.insertAll(entities) },
                recordTypeName = "ElevationGainedRecord",
                avroTypeClass = AvroElevationGainedRecord::class.java
            )
        }
        processors[ExerciseSessionRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> avro.toExerciseSessionRecordEntity() },
                daoInsertFunction = { entities -> exerciseSessionRecordDao.insertAll(entities) },
                recordTypeName = "ExerciseSessionRecord",
                avroTypeClass = AvroExerciseSessionRecord::class.java
            )
        }
        processors[FloorsClimbedRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> avro.toFloorsClimbedRecordEntity() },
                daoInsertFunction = { entities -> floorsClimbedRecordDao.insertAll(entities) },
                recordTypeName = "FloorsClimbedRecord",
                avroTypeClass = AvroFloorsClimbedRecord::class.java
            )
        }
        processors[HeartRateVariabilityRmssdRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> avro.toHeartRateVariabilityRmssdRecordEntity() },
                daoInsertFunction = { entities -> heartRateVariabilityRmssdRecordDao.insertAll(entities) },
                recordTypeName = "HeartRateVariabilityRmssdRecord",
                avroTypeClass = AvroHeartRateVariabilityRmssdRecord::class.java
            )
        }
        processors[HeightRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> avro.toHeightRecordEntity() },
                daoInsertFunction = { entities -> heightRecordDao.insertAll(entities) },
                recordTypeName = "HeightRecord",
                avroTypeClass = AvroHeightRecord::class.java
            )
        }
        processors[HydrationRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> avro.toHydrationRecordEntity() },
                daoInsertFunction = { entities -> hydrationRecordDao.insertAll(entities) },
                recordTypeName = "HydrationRecord",
                avroTypeClass = AvroHydrationRecord::class.java
            )
        }
        processors[LeanBodyMassRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> avro.toLeanBodyMassRecordEntity() },
                daoInsertFunction = { entities -> leanBodyMassRecordDao.insertAll(entities) },
                recordTypeName = "LeanBodyMassRecord",
                avroTypeClass = AvroLeanBodyMassRecord::class.java
            )
        }
        processors[NutritionRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> avro.toNutritionRecordEntity() },
                daoInsertFunction = { entities -> nutritionRecordDao.insertAll(entities) },
                recordTypeName = "NutritionRecord",
                avroTypeClass = AvroNutritionRecord::class.java
            )
        }
        processors[OxygenSaturationRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> avro.toOxygenSaturationRecordEntity() },
                daoInsertFunction = { entities -> oxygenSaturationRecordDao.insertAll(entities) },
                recordTypeName = "OxygenSaturationRecord",
                avroTypeClass = AvroOxygenSaturationRecord::class.java
            )
        }
        processors[RespiratoryRateRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> avro.toRespiratoryRateRecordEntity() },
                daoInsertFunction = { entities -> respiratoryRateRecordDao.insertAll(entities) },
                recordTypeName = "RespiratoryRateRecord",
                avroTypeClass = AvroRespiratoryRateRecord::class.java
            )
        }
        processors[RestingHeartRateRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> avro.toRestingHeartRateRecordEntity() },
                daoInsertFunction = { entities -> restingHeartRateRecordDao.insertAll(entities) },
                recordTypeName = "RestingHeartRateRecord",
                avroTypeClass = AvroRestingHeartRateRecord::class.java
            )
        }
        processors[TotalCaloriesBurnedRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> avro.toTotalCaloriesBurnedRecordEntity() },
                daoInsertFunction = { entities -> totalCaloriesBurnedRecordDao.insertAll(entities) },
                recordTypeName = "TotalCaloriesBurnedRecord",
                avroTypeClass = AvroTotalCaloriesBurnedRecord::class.java
            )
        }
        processors[Vo2MaxRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> avro.toVo2MaxRecordEntity() },
                daoInsertFunction = { entities -> vo2MaxRecordDao.insertAll(entities) },
                recordTypeName = "Vo2MaxRecord",
                avroTypeClass = AvroVo2MaxRecord::class.java
            )
        }
        processors[WeightRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> avro.toWeightRecordEntity() },
                daoInsertFunction = { entities -> weightRecordDao.insertAll(entities) },
                recordTypeName = "WeightRecord",
                avroTypeClass = AvroWeightRecord::class.java
            )
        }
        return processors
    }
}