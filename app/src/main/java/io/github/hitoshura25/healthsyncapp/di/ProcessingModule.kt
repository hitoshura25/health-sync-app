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
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BasalMetabolicRateRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BloodGlucoseEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BloodPressureRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BodyFatRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BodyTemperatureRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BodyWaterMassRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BoneMassRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.DistanceRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.ElevationGainedRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.ExerciseSessionRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.FloorsClimbedRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.HeartRateVariabilityRmssdRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.HeightRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.HydrationRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.LeanBodyMassRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.NutritionRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.OxygenSaturationRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.RespiratoryRateRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.RestingHeartRateRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.TotalCaloriesBurnedRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.Vo2MaxRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.WeightRecordEntity
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
        processors: Map<KClass<out Record>, @JvmSuppressWildcards Provider<RecordProcessor>>
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
    ): Map<KClass<out Record>,  Provider<RecordProcessor>> {
        val processors = mutableMapOf<KClass<out Record>, Provider<RecordProcessor>>()

        // Specific Processors
        processors[StepsRecord::class] = stepsRecordProcessor as Provider<RecordProcessor>
        processors[SleepSessionRecord::class] = sleepSessionProcessor as Provider<RecordProcessor>
        processors[CyclingPedalingCadenceRecord::class] = cyclingPedalingCadenceRecordProcessor as Provider<RecordProcessor>
        processors[HeartRateRecord::class] = heartRateRecordProcessor as Provider<RecordProcessor>
        processors[PowerRecord::class] = powerRecordProcessor as Provider<RecordProcessor>
        processors[SpeedRecord::class] = speedRecordProcessor as Provider<RecordProcessor>
        processors[StepsCadenceRecord::class] = stepsCadenceRecordProcessor as Provider<RecordProcessor>

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
                daoInsertFunction = { entities -> basalMetabolicRateRecordDao.insertAll(entities as List<BasalMetabolicRateRecordEntity>) },
                recordTypeName = "BasalMetabolicRateRecord",
                avroTypeClass = AvroBasalMetabolicRateRecord::class.java
            )
        }
        processors[BloodGlucoseRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> (avro as AvroBloodGlucoseRecord).toBloodGlucoseEntity() },
                daoInsertFunction = { entities -> bloodGlucoseDao.insertAll(entities as List<BloodGlucoseEntity>) },
                recordTypeName = "BloodGlucoseRecord",
                avroTypeClass = AvroBloodGlucoseRecord::class.java
            )
        }
        processors[BloodPressureRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> (avro as AvroBloodPressureRecord).toBloodPressureRecordEntity() },
                daoInsertFunction = { entities -> bloodPressureRecordDao.insertAll(entities as List<BloodPressureRecordEntity>) },
                recordTypeName = "BloodPressureRecord",
                avroTypeClass = AvroBloodPressureRecord::class.java
            )
        }
        processors[BodyFatRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> (avro as AvroBodyFatRecord).toBodyFatRecordEntity() },
                daoInsertFunction = { entities -> bodyFatRecordDao.insertAll(entities as List<BodyFatRecordEntity>) },
                recordTypeName = "BodyFatRecord",
                avroTypeClass = AvroBodyFatRecord::class.java
            )
        }
        processors[BodyTemperatureRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> (avro as AvroBodyTemperatureRecord).toBodyTemperatureRecordEntity() },
                daoInsertFunction = { entities -> bodyTemperatureRecordDao.insertAll(entities as List<BodyTemperatureRecordEntity>) },
                recordTypeName = "BodyTemperatureRecord",
                avroTypeClass = AvroBodyTemperatureRecord::class.java
            )
        }
        processors[BodyWaterMassRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> (avro as AvroBodyWaterMassRecord).toBodyWaterMassRecordEntity() },
                daoInsertFunction = { entities -> bodyWaterMassRecordDao.insertAll(entities as List<BodyWaterMassRecordEntity>) },
                recordTypeName = "BodyWaterMassRecord",
                avroTypeClass = AvroBodyWaterMassRecord::class.java
            )
        }
        processors[BoneMassRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> (avro as AvroBoneMassRecord).toBoneMassRecordEntity() },
                daoInsertFunction = { entities -> boneMassRecordDao.insertAll(entities as List<BoneMassRecordEntity>) },
                recordTypeName = "BoneMassRecord",
                avroTypeClass = AvroBoneMassRecord::class.java
            )
        }
        processors[DistanceRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> (avro as AvroDistanceRecord).toDistanceRecordEntity() },
                daoInsertFunction = { entities -> distanceRecordDao.insertAll(entities as List<DistanceRecordEntity>) },
                recordTypeName = "DistanceRecord",
                avroTypeClass = AvroDistanceRecord::class.java
            )
        }
        processors[ElevationGainedRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> (avro as AvroElevationGainedRecord).toElevationGainedRecordEntity() },
                daoInsertFunction = { entities -> elevationGainedRecordDao.insertAll(entities as List<ElevationGainedRecordEntity>) },
                recordTypeName = "ElevationGainedRecord",
                avroTypeClass = AvroElevationGainedRecord::class.java
            )
        }
        processors[ExerciseSessionRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> (avro as AvroExerciseSessionRecord).toExerciseSessionRecordEntity() },
                daoInsertFunction = { entities -> exerciseSessionRecordDao.insertAll(entities as List<ExerciseSessionRecordEntity>) },
                recordTypeName = "ExerciseSessionRecord",
                avroTypeClass = AvroExerciseSessionRecord::class.java
            )
        }
        processors[FloorsClimbedRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> (avro as AvroFloorsClimbedRecord).toFloorsClimbedRecordEntity() },
                daoInsertFunction = { entities -> floorsClimbedRecordDao.insertAll(entities as List<FloorsClimbedRecordEntity>) },
                recordTypeName = "FloorsClimbedRecord",
                avroTypeClass = AvroFloorsClimbedRecord::class.java
            )
        }
        processors[HeartRateVariabilityRmssdRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> (avro as AvroHeartRateVariabilityRmssdRecord).toHeartRateVariabilityRmssdRecordEntity() },
                daoInsertFunction = { entities -> heartRateVariabilityRmssdRecordDao.insertAll(entities as List<HeartRateVariabilityRmssdRecordEntity>) },
                recordTypeName = "HeartRateVariabilityRmssdRecord",
                avroTypeClass = AvroHeartRateVariabilityRmssdRecord::class.java
            )
        }
        processors[HeightRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> (avro as AvroHeightRecord).toHeightRecordEntity() },
                daoInsertFunction = { entities -> heightRecordDao.insertAll(entities as List<HeightRecordEntity>) },
                recordTypeName = "HeightRecord",
                avroTypeClass = AvroHeightRecord::class.java
            )
        }
        processors[HydrationRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> (avro as AvroHydrationRecord).toHydrationRecordEntity() },
                daoInsertFunction = { entities -> hydrationRecordDao.insertAll(entities as List<HydrationRecordEntity>) },
                recordTypeName = "HydrationRecord",
                avroTypeClass = AvroHydrationRecord::class.java
            )
        }
        processors[LeanBodyMassRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> (avro as AvroLeanBodyMassRecord).toLeanBodyMassRecordEntity() },
                daoInsertFunction = { entities -> leanBodyMassRecordDao.insertAll(entities as List<LeanBodyMassRecordEntity>) },
                recordTypeName = "LeanBodyMassRecord",
                avroTypeClass = AvroLeanBodyMassRecord::class.java
            )
        }
        processors[NutritionRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> (avro as AvroNutritionRecord).toNutritionRecordEntity() },
                daoInsertFunction = { entities -> nutritionRecordDao.insertAll(entities as List<NutritionRecordEntity>) },
                recordTypeName = "NutritionRecord",
                avroTypeClass = AvroNutritionRecord::class.java
            )
        }
        processors[OxygenSaturationRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> (avro as AvroOxygenSaturationRecord).toOxygenSaturationRecordEntity() },
                daoInsertFunction = { entities -> oxygenSaturationRecordDao.insertAll(entities as List<OxygenSaturationRecordEntity>) },
                recordTypeName = "OxygenSaturationRecord",
                avroTypeClass = AvroOxygenSaturationRecord::class.java
            )
        }
        processors[RespiratoryRateRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> (avro as AvroRespiratoryRateRecord).toRespiratoryRateRecordEntity() },
                daoInsertFunction = { entities -> respiratoryRateRecordDao.insertAll(entities as List<RespiratoryRateRecordEntity>) },
                recordTypeName = "RespiratoryRateRecord",
                avroTypeClass = AvroRespiratoryRateRecord::class.java
            )
        }
        processors[RestingHeartRateRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> (avro as AvroRestingHeartRateRecord).toRestingHeartRateRecordEntity() },
                daoInsertFunction = { entities -> restingHeartRateRecordDao.insertAll(entities as List<RestingHeartRateRecordEntity>) },
                recordTypeName = "RestingHeartRateRecord",
                avroTypeClass = AvroRestingHeartRateRecord::class.java
            )
        }
        processors[TotalCaloriesBurnedRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> (avro as AvroTotalCaloriesBurnedRecord).toTotalCaloriesBurnedRecordEntity() },
                daoInsertFunction = { entities -> totalCaloriesBurnedRecordDao.insertAll(entities as List<TotalCaloriesBurnedRecordEntity>) },
                recordTypeName = "TotalCaloriesBurnedRecord",
                avroTypeClass = AvroTotalCaloriesBurnedRecord::class.java
            )
        }
        processors[Vo2MaxRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> (avro as AvroVo2MaxRecord).toVo2MaxRecordEntity() },
                daoInsertFunction = { entities -> vo2MaxRecordDao.insertAll(entities as List<Vo2MaxRecordEntity>) },
                recordTypeName = "Vo2MaxRecord",
                avroTypeClass = AvroVo2MaxRecord::class.java
            )
        }
        processors[WeightRecord::class] = Provider {
            SingleRecordTypeProcessor(
                toEntityMapper = { avro -> (avro as AvroWeightRecord).toWeightRecordEntity() },
                daoInsertFunction = { entities -> weightRecordDao.insertAll(entities as List<WeightRecordEntity>) },
                recordTypeName = "WeightRecord",
                avroTypeClass = AvroWeightRecord::class.java
            )
        }
        return processors
    }
}
