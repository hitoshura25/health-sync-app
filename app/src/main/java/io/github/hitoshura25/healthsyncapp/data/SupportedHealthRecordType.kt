package io.github.hitoshura25.healthsyncapp.data

import androidx.health.connect.client.records.*
import kotlin.reflect.KClass

sealed interface SupportedHealthRecordType<T : Record> {
    val recordKClass: KClass<T>

    object ActiveCaloriesBurned : SupportedHealthRecordType<ActiveCaloriesBurnedRecord> {
        override val recordKClass: KClass<ActiveCaloriesBurnedRecord> = ActiveCaloriesBurnedRecord::class
    }
    object BasalBodyTemperature : SupportedHealthRecordType<BasalBodyTemperatureRecord> {
        override val recordKClass: KClass<BasalBodyTemperatureRecord> = BasalBodyTemperatureRecord::class
    }
    object BasalMetabolicRate : SupportedHealthRecordType<BasalMetabolicRateRecord> {
        override val recordKClass: KClass<BasalMetabolicRateRecord> = BasalMetabolicRateRecord::class
    }
    object BloodGlucose : SupportedHealthRecordType<BloodGlucoseRecord> {
        override val recordKClass: KClass<BloodGlucoseRecord> = BloodGlucoseRecord::class
    }
    object BloodPressure : SupportedHealthRecordType<BloodPressureRecord> {
        override val recordKClass: KClass<BloodPressureRecord> = BloodPressureRecord::class
    }
    object BodyFat : SupportedHealthRecordType<BodyFatRecord> {
        override val recordKClass: KClass<BodyFatRecord> = BodyFatRecord::class
    }
    object BodyTemperature : SupportedHealthRecordType<BodyTemperatureRecord> {
        override val recordKClass: KClass<BodyTemperatureRecord> = BodyTemperatureRecord::class
    }
    object BodyWaterMass : SupportedHealthRecordType<BodyWaterMassRecord> {
        override val recordKClass: KClass<BodyWaterMassRecord> = BodyWaterMassRecord::class
    }
    object BoneMass : SupportedHealthRecordType<BoneMassRecord> {
        override val recordKClass: KClass<BoneMassRecord> = BoneMassRecord::class
    }
    object CyclingPedalingCadence : SupportedHealthRecordType<CyclingPedalingCadenceRecord> {
        override val recordKClass: KClass<CyclingPedalingCadenceRecord> = CyclingPedalingCadenceRecord::class
    }
    object Distance : SupportedHealthRecordType<DistanceRecord> {
        override val recordKClass: KClass<DistanceRecord> = DistanceRecord::class
    }
    object ElevationGained : SupportedHealthRecordType<ElevationGainedRecord> {
        override val recordKClass: KClass<ElevationGainedRecord> = ElevationGainedRecord::class
    }
    object ExerciseSession : SupportedHealthRecordType<ExerciseSessionRecord> {
        override val recordKClass: KClass<ExerciseSessionRecord> = ExerciseSessionRecord::class
    }
    object FloorsClimbed : SupportedHealthRecordType<FloorsClimbedRecord> {
        override val recordKClass: KClass<FloorsClimbedRecord> = FloorsClimbedRecord::class
    }
    object HeartRate : SupportedHealthRecordType<HeartRateRecord> {
        override val recordKClass: KClass<HeartRateRecord> = HeartRateRecord::class
    }
    object HeartRateVariabilityRmssd : SupportedHealthRecordType<HeartRateVariabilityRmssdRecord> {
        override val recordKClass: KClass<HeartRateVariabilityRmssdRecord> = HeartRateVariabilityRmssdRecord::class
    }
    object Height : SupportedHealthRecordType<HeightRecord> {
        override val recordKClass: KClass<HeightRecord> = HeightRecord::class
    }
    object Hydration : SupportedHealthRecordType<HydrationRecord> {
        override val recordKClass: KClass<HydrationRecord> = HydrationRecord::class
    }
    object LeanBodyMass : SupportedHealthRecordType<LeanBodyMassRecord> {
        override val recordKClass: KClass<LeanBodyMassRecord> = LeanBodyMassRecord::class
    }
    object Nutrition : SupportedHealthRecordType<NutritionRecord> {
        override val recordKClass: KClass<NutritionRecord> = NutritionRecord::class
    }
    object OxygenSaturation : SupportedHealthRecordType<OxygenSaturationRecord> {
        override val recordKClass: KClass<OxygenSaturationRecord> = OxygenSaturationRecord::class
    }
    object Power : SupportedHealthRecordType<PowerRecord> {
        override val recordKClass: KClass<PowerRecord> = PowerRecord::class
    }
    object RespiratoryRate : SupportedHealthRecordType<RespiratoryRateRecord> {
        override val recordKClass: KClass<RespiratoryRateRecord> = RespiratoryRateRecord::class
    }
    object RestingHeartRate : SupportedHealthRecordType<RestingHeartRateRecord> {
        override val recordKClass: KClass<RestingHeartRateRecord> = RestingHeartRateRecord::class
    }
    object SleepSession : SupportedHealthRecordType<SleepSessionRecord> {
        override val recordKClass: KClass<SleepSessionRecord> = SleepSessionRecord::class
    }
    object Speed : SupportedHealthRecordType<SpeedRecord> {
        override val recordKClass: KClass<SpeedRecord> = SpeedRecord::class
    }
    object StepsCadence : SupportedHealthRecordType<StepsCadenceRecord> {
        override val recordKClass: KClass<StepsCadenceRecord> = StepsCadenceRecord::class
    }
    object Steps : SupportedHealthRecordType<StepsRecord> {
        override val recordKClass: KClass<StepsRecord> = StepsRecord::class
    }
    object TotalCaloriesBurned : SupportedHealthRecordType<TotalCaloriesBurnedRecord> {
        override val recordKClass: KClass<TotalCaloriesBurnedRecord> = TotalCaloriesBurnedRecord::class
    }
    object Vo2Max : SupportedHealthRecordType<Vo2MaxRecord> {
        override val recordKClass: KClass<Vo2MaxRecord> = Vo2MaxRecord::class
    }
    object Weight : SupportedHealthRecordType<WeightRecord> {
        override val recordKClass: KClass<WeightRecord> = WeightRecord::class
    }
}
