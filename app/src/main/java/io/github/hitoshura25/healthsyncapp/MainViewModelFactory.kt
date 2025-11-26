package io.github.hitoshura25.healthsyncapp

import android.app.Application
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.ActiveCaloriesBurnedRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BasalBodyTemperatureRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BasalMetabolicRateRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BloodGlucoseDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BodyFatRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BodyTemperatureRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BodyWaterMassRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BoneMassRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.DistanceRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.ElevationGainedRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.ExerciseSessionRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.FloorsClimbedRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.HeartRateSampleDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.HeartRateVariabilityRmssdRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.PowerRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.RestingHeartRateRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.SleepSessionDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.SleepStageDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.SpeedRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.StepsCadenceRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.StepsRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.TotalCaloriesBurnedRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.Vo2MaxRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.WeightRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.HeightRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.LeanBodyMassRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.HydrationRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.NutritionRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BloodPressureRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.OxygenSaturationRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.RespiratoryRateRecordDao

// Define an EntryPoint to access Hilt dependencies from non-Hilt-aware classes
@EntryPoint
@InstallIn(SingletonComponent::class) // DAOs are Singleton scoped as per AppModule
interface ViewModelFactoryEntryPoint {
    fun stepsRecordDao(): StepsRecordDao
    fun heartRateSampleDao(): HeartRateSampleDao
    fun sleepSessionDao(): SleepSessionDao
    fun sleepStageDao(): SleepStageDao
    fun bloodGlucoseDao(): BloodGlucoseDao

    // Added DAOs for TODOs
    fun weightRecordDao(): WeightRecordDao
    fun activeCaloriesBurnedRecordDao(): ActiveCaloriesBurnedRecordDao
    fun basalBodyTemperatureRecordDao(): BasalBodyTemperatureRecordDao
    fun basalMetabolicRateRecordDao(): BasalMetabolicRateRecordDao
    fun distanceRecordDao(): DistanceRecordDao
    fun elevationGainedRecordDao(): ElevationGainedRecordDao
    fun exerciseSessionRecordDao(): ExerciseSessionRecordDao
    fun floorsClimbedRecordDao(): FloorsClimbedRecordDao
    fun heartRateVariabilityRmssdRecordDao(): HeartRateVariabilityRmssdRecordDao
    fun powerRecordDao(): PowerRecordDao
    fun restingHeartRateRecordDao(): RestingHeartRateRecordDao
    fun speedRecordDao(): SpeedRecordDao
    fun stepsCadenceRecordDao(): StepsCadenceRecordDao
    fun totalCaloriesBurnedRecordDao(): TotalCaloriesBurnedRecordDao
    fun vo2MaxRecordDao(): Vo2MaxRecordDao
    fun bodyFatRecordDao(): BodyFatRecordDao
    fun bodyTemperatureRecordDao(): BodyTemperatureRecordDao
    fun bodyWaterMassRecordDao(): BodyWaterMassRecordDao
    fun boneMassRecordDao(): BoneMassRecordDao
    fun heightRecordDao(): HeightRecordDao
    fun leanBodyMassRecordDao(): LeanBodyMassRecordDao
    fun hydrationRecordDao(): HydrationRecordDao
    fun nutritionRecordDao(): NutritionRecordDao
    fun bloodPressureRecordDao(): BloodPressureRecordDao
    fun oxygenSaturationRecordDao(): OxygenSaturationRecordDao
    fun respiratoryRateRecordDao(): RespiratoryRateRecordDao
}

class MainViewModelFactory(
    private val application: Application,
    private val healthConnectClient: HealthConnectClient
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            // Access DAOs via the Hilt EntryPoint
            val entryPoint = EntryPointAccessors.fromApplication(
                application.applicationContext, // Use application context for SingletonComponent
                ViewModelFactoryEntryPoint::class.java
            )

            val stepsDao = entryPoint.stepsRecordDao()
            val heartRateDao = entryPoint.heartRateSampleDao()
            val sleepSessionDao = entryPoint.sleepSessionDao()
            val sleepStageDao = entryPoint.sleepStageDao()
            val bloodGlucoseDao = entryPoint.bloodGlucoseDao()

            // Resolve TODOs by getting DAOs from entryPoint
            val weightRecordDao = entryPoint.weightRecordDao()
            val activeCaloriesBurnedRecordDao = entryPoint.activeCaloriesBurnedRecordDao()
            val basalBodyTemperatureRecordDao = entryPoint.basalBodyTemperatureRecordDao()
            val basalMetabolicRateRecordDao = entryPoint.basalMetabolicRateRecordDao()
            val distanceRecordDao = entryPoint.distanceRecordDao()
            val elevationGainedRecordDao = entryPoint.elevationGainedRecordDao()
            val exerciseSessionRecordDao = entryPoint.exerciseSessionRecordDao()
            val floorsClimbedRecordDao = entryPoint.floorsClimbedRecordDao()
            val heartRateVariabilityRmssdRecordDao = entryPoint.heartRateVariabilityRmssdRecordDao()
            val powerRecordDao = entryPoint.powerRecordDao()
            val restingHeartRateRecordDao = entryPoint.restingHeartRateRecordDao()
            val speedRecordDao = entryPoint.speedRecordDao()
            val stepsCadenceRecordDao = entryPoint.stepsCadenceRecordDao()
            val totalCaloriesBurnedRecordDao = entryPoint.totalCaloriesBurnedRecordDao()
            val vo2MaxRecordDao = entryPoint.vo2MaxRecordDao()
            val bodyFatRecordDao = entryPoint.bodyFatRecordDao()
            val bodyTemperatureRecordDao = entryPoint.bodyTemperatureRecordDao()
            val bodyWaterMassRecordDao = entryPoint.bodyWaterMassRecordDao()
            val boneMassRecordDao = entryPoint.boneMassRecordDao()

            return MainViewModel(
                application = application,
                stepsDao = stepsDao,
                heartRateDao = heartRateDao,
                sleepDao = sleepSessionDao,
                bloodGlucoseDao = bloodGlucoseDao,
                sleepStageDao = sleepStageDao,
                healthConnectClient = healthConnectClient,
                weightRecordDao = weightRecordDao,
                activeCaloriesBurnedRecordDao = activeCaloriesBurnedRecordDao,
                basalBodyTemperatureRecordDao = basalBodyTemperatureRecordDao,
                basalMetabolicRateRecordDao = basalMetabolicRateRecordDao,
                distanceRecordDao = distanceRecordDao,
                elevationGainedRecordDao = elevationGainedRecordDao,
                exerciseSessionRecordDao = exerciseSessionRecordDao,
                floorsClimbedRecordDao = floorsClimbedRecordDao,
                heartRateVariabilityRmssdRecordDao = heartRateVariabilityRmssdRecordDao,
                powerRecordDao = powerRecordDao,
                restingHeartRateRecordDao = restingHeartRateRecordDao,
                speedRecordDao = speedRecordDao,
                stepsCadenceRecordDao = stepsCadenceRecordDao,
                totalCaloriesBurnedRecordDao = totalCaloriesBurnedRecordDao,
                vo2MaxRecordDao = vo2MaxRecordDao,
                bodyFatRecordDao = bodyFatRecordDao,
                bodyTemperatureRecordDao = bodyTemperatureRecordDao,
                bodyWaterMassRecordDao = bodyWaterMassRecordDao,
                boneMassRecordDao = boneMassRecordDao,
                heightRecordDao = entryPoint.heightRecordDao(),
                leanBodyMassRecordDao = entryPoint.leanBodyMassRecordDao(),
                hydrationRecordDao = entryPoint.hydrationRecordDao(),
                nutritionRecordDao = entryPoint.nutritionRecordDao(),
                bloodPressureRecordDao = entryPoint.bloodPressureRecordDao(),
                oxygenSaturationRecordDao = entryPoint.oxygenSaturationRecordDao(),
                respiratoryRateRecordDao = entryPoint.respiratoryRateRecordDao()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }
}
