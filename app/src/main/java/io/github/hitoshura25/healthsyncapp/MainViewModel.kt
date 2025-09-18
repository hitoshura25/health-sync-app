package io.github.hitoshura25.healthsyncapp

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
// Explicit Health Connect Record type imports
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
import androidx.health.connect.client.records.RespiratoryRateRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.SpeedRecord
import androidx.health.connect.client.records.StepsCadenceRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.Vo2MaxRecord
import androidx.health.connect.client.records.WeightRecord
// Lifecycle and coroutines imports
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
// Import DAOs
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BloodGlucoseDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.HeartRateSampleDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.SleepSessionDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.SleepStageDao // Added import
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.StepsRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.WeightRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.ActiveCaloriesBurnedRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BasalBodyTemperatureRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BasalMetabolicRateRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BloodPressureRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.StepsCadenceRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.TotalCaloriesBurnedRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.Vo2MaxRecordDao
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
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.PowerRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.RespiratoryRateRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.RestingHeartRateRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.SpeedRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BloodPressureRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.HeightRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.HydrationRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.LeanBodyMassRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.NutritionRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.OxygenSaturationRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.RespiratoryRateRecordEntity
import io.github.hitoshura25.healthsyncapp.worker.HealthDataFetcherWorker // Changed import
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
// Java time imports
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MainViewModel(
    private val application: Application,
    private val stepsDao: StepsRecordDao,
    private val heartRateDao: HeartRateSampleDao,
    private val sleepDao: SleepSessionDao,
    private val bloodGlucoseDao: BloodGlucoseDao,
    private val sleepStageDao: SleepStageDao, // Added SleepStageDao
    private val weightRecordDao: WeightRecordDao,
    private val activeCaloriesBurnedRecordDao: ActiveCaloriesBurnedRecordDao,
    private val basalBodyTemperatureRecordDao: BasalBodyTemperatureRecordDao,
    private val basalMetabolicRateRecordDao: BasalMetabolicRateRecordDao,
    private val distanceRecordDao: DistanceRecordDao,
    private val elevationGainedRecordDao: ElevationGainedRecordDao,
    private val exerciseSessionRecordDao: ExerciseSessionRecordDao,
    private val floorsClimbedRecordDao: FloorsClimbedRecordDao,
    private val heartRateVariabilityRmssdRecordDao: HeartRateVariabilityRmssdRecordDao,
    private val powerRecordDao: PowerRecordDao,
    private val restingHeartRateRecordDao: RestingHeartRateRecordDao,
    private val speedRecordDao: SpeedRecordDao,
    private val stepsCadenceRecordDao: StepsCadenceRecordDao,
    private val totalCaloriesBurnedRecordDao: TotalCaloriesBurnedRecordDao,
    private val vo2MaxRecordDao: Vo2MaxRecordDao,
    private val bodyFatRecordDao: BodyFatRecordDao,
    private val bodyTemperatureRecordDao: BodyTemperatureRecordDao,
    private val bodyWaterMassRecordDao: BodyWaterMassRecordDao,
    private val boneMassRecordDao: BoneMassRecordDao,
    private val heightRecordDao: HeightRecordDao,
    private val leanBodyMassRecordDao: LeanBodyMassRecordDao,
    private val hydrationRecordDao: HydrationRecordDao,
    private val nutritionRecordDao: NutritionRecordDao,
    private val bloodPressureRecordDao: BloodPressureRecordDao,
    private val oxygenSaturationRecordDao: OxygenSaturationRecordDao,
    private val respiratoryRateRecordDao: RespiratoryRateRecordDao,
    private val healthConnectClient: HealthConnectClient // Still needed for permission checks
) : ViewModel() {


    companion object {
        private const val TAG = "MainViewModel"
        private const val PREFS_NAME = "HealthSyncAppPrefs"
        private const val KEY_INITIAL_WORKER_SCHEDULED = "initialWorkerScheduled"

        private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault())

        val PERMISSIONS = setOf(
            HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
            HealthPermission.getReadPermission(BasalBodyTemperatureRecord::class),
            HealthPermission.getReadPermission(BasalMetabolicRateRecord::class),
            HealthPermission.getReadPermission(BloodGlucoseRecord::class),
            HealthPermission.getReadPermission(BloodPressureRecord::class),
            HealthPermission.getReadPermission(BodyFatRecord::class),
            HealthPermission.getReadPermission(BodyTemperatureRecord::class),
            HealthPermission.getReadPermission(BodyWaterMassRecord::class),
            HealthPermission.getReadPermission(BoneMassRecord::class),
            HealthPermission.getReadPermission(CyclingPedalingCadenceRecord::class),
            HealthPermission.getReadPermission(DistanceRecord::class),
            HealthPermission.getReadPermission(ElevationGainedRecord::class),
            HealthPermission.getReadPermission(ExerciseSessionRecord::class),
            HealthPermission.getReadPermission(FloorsClimbedRecord::class),
            HealthPermission.getReadPermission(HeartRateRecord::class),
            HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class),
            HealthPermission.getReadPermission(HeightRecord::class),
            HealthPermission.getReadPermission(HydrationRecord::class),
            HealthPermission.getReadPermission(LeanBodyMassRecord::class),
            HealthPermission.getReadPermission(NutritionRecord::class),
            HealthPermission.getReadPermission(OxygenSaturationRecord::class),
            HealthPermission.getReadPermission(PowerRecord::class),
            HealthPermission.getReadPermission(RespiratoryRateRecord::class),
            HealthPermission.getReadPermission(RestingHeartRateRecord::class),
            HealthPermission.getReadPermission(SleepSessionRecord::class),
            HealthPermission.getReadPermission(SpeedRecord::class),
            HealthPermission.getReadPermission(StepsCadenceRecord::class),
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
            HealthPermission.getReadPermission(Vo2MaxRecord::class),
            HealthPermission.getReadPermission(WeightRecord::class)
        )
    }


    val stepsCadenceData: StateFlow<String> = stepsCadenceRecordDao.getAllObservable()
        .map { recordsWithSamples ->
            if (recordsWithSamples.isNotEmpty()) {
                recordsWithSamples.maxByOrNull { it.record.endTimeEpochMillis }?.let { latestRecordWithSamples ->
                    val latestSample = latestRecordWithSamples.samples.maxByOrNull { it.timeEpochMillis }
                    if (latestSample != null) {
                        "Latest Steps Cadence (DB): ${String.format("%.2f", latestSample.rateInStepsPerMinute)} steps/min (ends ${formatter.format(Instant.ofEpochMilli(latestRecordWithSamples.record.endTimeEpochMillis))})"
                    } else {
                        "Steps Cadence: No samples in latest record"
                    }
                } ?: "Steps Cadence: No data in DB"
            } else "Steps Cadence: No data in DB"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Steps Cadence: Loading...")



    // Observe data from DAOs using Flow and convert to StateFlow for UI
    val stepsData: StateFlow<String> = stepsDao.getAllObservable()
        .map { records ->
            if (records.isNotEmpty()) "Steps (from DB): ${records.sumOf { it.count }}" 
            else "Steps: No data in DB"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Steps: Loading...")

    val heartRateData: StateFlow<String> = heartRateDao.getAllObservable()
        .map { samples ->
            if (samples.isNotEmpty()) {
                samples.maxByOrNull { it.sampleTimeEpochMillis }?.let {
                    "Latest HR (DB): ${it.beatsPerMinute} BPM at ${formatter.format(Instant.ofEpochMilli(it.sampleTimeEpochMillis))}"
                } ?: "Heart Rate: No samples in DB"
            } else "Heart Rate: No data in DB"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Heart Rate: Loading...")

    val sleepData: StateFlow<String> = sleepDao.getAllObservable()
        .map { sessions ->
            if (sessions.isNotEmpty()) {
                sessions.maxByOrNull { it.endTimeEpochMillis }?.let {
                    val duration = it.durationMillis?.let { d -> (d / (1000 * 60)).toLong().toString() } ?: "N/A"
                    val stageCount = sleepStageDao.getStagesBySessionHcUid(it.hcUid).size
                    "Last Sleep (DB): $duration mins, $stageCount stages (ends ${formatter.format(Instant.ofEpochMilli(it.endTimeEpochMillis))})"
                } ?: "Sleep: No session data in DB"
            } else "Sleep: No data in DB"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Sleep: Loading...")

    val bloodGlucoseData: StateFlow<String> = bloodGlucoseDao.getAllObservable()
        .map { records ->
            if (records.isNotEmpty()) {
                records.maxByOrNull { it.timeEpochMillis }?.let {
                    "Latest Glucose (DB): ${it.levelInMilligramsPerDeciliter} mg/dL at ${formatter.format(Instant.ofEpochMilli(it.timeEpochMillis))}"
                } ?: "Blood Glucose: No level data in DB"
            } else "Blood Glucose: No data in DB"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Blood Glucose: Loading...")

    val weightData: StateFlow<String> = weightRecordDao.getAllObservable()
        .map { records ->
            if (records.isNotEmpty()) {
                records.maxByOrNull { it.timeEpochMillis }?.let {
                    "Latest Weight (DB): ${it.weightInKilograms} kg at ${formatter.format(Instant.ofEpochMilli(it.timeEpochMillis))}"
                } ?: "Weight: No data in DB"
            } else "Weight: No data in DB"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Weight: Loading...")

    val activeCaloriesBurnedData: StateFlow<String> = activeCaloriesBurnedRecordDao.getAllObservable()
        .map { records ->
            if (records.isNotEmpty()) {
                val totalCalories = records.sumOf { it.energyInKilocalories }
                "Total Active Calories Burned (DB): $totalCalories kcal"
            } else "Active Calories Burned: No data in DB"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Active Calories Burned: Loading...")

    val basalBodyTemperatureData: StateFlow<String> = basalBodyTemperatureRecordDao.getAllObservable()
        .map { records ->
            if (records.isNotEmpty()) {
                records.maxByOrNull { it.timeEpochMillis }?.let {
                    "Latest Basal Body Temperature (DB): ${it.temperatureInCelsius} °C at ${formatter.format(Instant.ofEpochMilli(it.timeEpochMillis))}"
                } ?: "Basal Body Temperature: No data in DB"
            } else "Basal Body Temperature: No data in DB"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Basal Body Temperature: Loading...")

    val basalMetabolicRateData: StateFlow<String> = basalMetabolicRateRecordDao.getAllObservable()
        .map { records ->
            if (records.isNotEmpty()) {
                records.maxByOrNull { it.timeEpochMillis }?.let {
                    "Latest Basal Metabolic Rate (DB): ${it.basalMetabolicRateInKilocaloriesPerDay} kcal/day at ${formatter.format(Instant.ofEpochMilli(it.timeEpochMillis))}"
                } ?: "Basal Metabolic Rate: No data in DB"
            } else "Basal Metabolic Rate: No data in DB"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Basal Metabolic Rate: Loading...")

    val distanceData: StateFlow<String> = distanceRecordDao.getAllObservable()
        .map { records ->
            if (records.isNotEmpty()) {
                records.maxByOrNull { it.endTimeEpochMillis }?.let {
                    "Latest Distance (DB): ${String.format("%.2f", it.distanceInMeters / 1000)} km (ends ${formatter.format(Instant.ofEpochMilli(it.endTimeEpochMillis))})"
                } ?: "Distance: No data in DB"
            } else "Distance: No data in DB"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Distance: Loading...")

    val elevationGainedData: StateFlow<String> = elevationGainedRecordDao.getAllObservable()
        .map { records ->
            if (records.isNotEmpty()) {
                records.maxByOrNull { it.endTimeEpochMillis }?.let {
                    "Latest Elevation Gained (DB): ${String.format("%.2f", it.elevationInMeters)} m (ends ${formatter.format(Instant.ofEpochMilli(it.endTimeEpochMillis))})"
                } ?: "Elevation Gained: No data in DB"
            } else "Elevation Gained: No data in DB"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Elevation Gained: Loading...")

    val exerciseSessionData: StateFlow<String> = exerciseSessionRecordDao.getAllObservable()
        .map { records ->
            if (records.isNotEmpty()) {
                records.maxByOrNull { it.endTimeEpochMillis }?.let {
                    val duration = (it.endTimeEpochMillis - it.startTimeEpochMillis).let { d -> d / (1000 * 60) }.toString()
                    "Last Exercise Session (DB): ${it.exerciseType} for $duration mins (ends ${formatter.format(Instant.ofEpochMilli(it.endTimeEpochMillis))})"
                } ?: "Exercise Session: No data in DB"
            } else "Exercise Session: No data in DB"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Exercise Session: Loading...")

    val floorsClimbedData: StateFlow<String> = floorsClimbedRecordDao.getAllObservable()
        .map { records ->
            if (records.isNotEmpty()) {
                records.maxByOrNull { it.endTimeEpochMillis }?.let {
                    "Latest Floors Climbed (DB): ${it.floors} floors (ends ${formatter.format(Instant.ofEpochMilli(it.endTimeEpochMillis))})"
                } ?: "Floors Climbed: No data in DB"
            } else "Floors Climbed: No data in DB"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Floors Climbed: Loading...")

    val heartRateVariabilityRmssdData: StateFlow<String> = heartRateVariabilityRmssdRecordDao.getAllObservable()
        .map { records ->
            if (records.isNotEmpty()) {
                records.maxByOrNull { it.timeEpochMillis }?.let {
                    "Latest HRV RMSSD (DB): ${String.format("%.2f", it.heartRateVariabilityRmssd)} ms (at ${formatter.format(Instant.ofEpochMilli(it.timeEpochMillis))})"
                } ?: "HRV RMSSD: No data in DB"
            } else "HRV RMSSD: No data in DB"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "HRV RMSSD: Loading...")

    val powerData: StateFlow<String> = powerRecordDao.getAllObservable()
        .map { recordsWithSamples ->
            if (recordsWithSamples.isNotEmpty()) {
                recordsWithSamples.maxByOrNull { it.record.endTimeEpochMillis }?.let { latestRecordWithSamples ->
                    val latestSample = latestRecordWithSamples.samples.maxByOrNull { it.timeEpochMillis }
                    if (latestSample != null) {
                        "Latest Power (DB): ${String.format("%.2f", latestSample.powerInWatts)} W (ends ${formatter.format(Instant.ofEpochMilli(latestRecordWithSamples.record.endTimeEpochMillis))})"
                    } else {
                        "Power: No samples in latest record"
                    }
                } ?: "Power: No data in DB"
            } else "Power: No data in DB"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Power: Loading...")

    val restingHeartRateData: StateFlow<String> = restingHeartRateRecordDao.getAllObservable()
        .map { records ->
            if (records.isNotEmpty()) {
                records.maxByOrNull { it.timeEpochMillis }?.let {
                    "Latest Resting Heart Rate (DB): ${it.beatsPerMinute} BPM (at ${formatter.format(Instant.ofEpochMilli(it.timeEpochMillis))})"
                } ?: "Resting Heart Rate: No data in DB"
            } else "Resting Heart Rate: No data in DB"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Resting Heart Rate: Loading...")

    val speedData: StateFlow<String> = speedRecordDao.getAllObservable()
        .map { recordsWithSamples ->
            if (recordsWithSamples.isNotEmpty()) {
                recordsWithSamples.maxByOrNull { it.record.endTimeEpochMillis }?.let { latestRecordWithSamples ->
                    val latestSample = latestRecordWithSamples.samples.maxByOrNull { it.timeEpochMillis }
                    if (latestSample != null) {
                        "Latest Speed (DB): ${String.format("%.2f", latestSample.speedInMetersPerSecond)} m/s (ends ${formatter.format(Instant.ofEpochMilli(latestRecordWithSamples.record.endTimeEpochMillis))})"
                    } else {
                        "Speed: No samples in latest record"
                    }
                } ?: "Speed: No data in DB"
            } else "Speed: No data in DB"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Speed: Loading...")

    val totalCaloriesBurnedData: StateFlow<String> = totalCaloriesBurnedRecordDao.getAllObservable()
        .map { records ->
            if (records.isNotEmpty()) {
                val totalCalories = records.sumOf { it.energyInKilocalories }
                "Total Calories Burned (DB): $totalCalories kcal"
            } else "Total Calories Burned: No data in DB"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Total Calories Burned: Loading...")

    val vo2MaxData: StateFlow<String> = vo2MaxRecordDao.getAllObservable()
        .map { records ->
            if (records.isNotEmpty()) {
                records.maxByOrNull { it.timeEpochMillis }?.let {
                    "Latest VO2 Max (DB): ${String.format("%.2f", it.vo2Max)} ml/(min·kg) (at ${formatter.format(Instant.ofEpochMilli(it.timeEpochMillis))})"
                } ?: "VO2 Max: No data in DB"
            } else "VO2 Max: No data in DB"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "VO2 Max: Loading...")

    val bodyFatData: StateFlow<String> = bodyFatRecordDao.getAllObservable()
        .map { records ->
            if (records.isNotEmpty()) {
                records.maxByOrNull { it.timeEpochMillis }?.let {
                    "Latest Body Fat (DB): ${String.format("%.2f", it.percentage)}% (at ${formatter.format(Instant.ofEpochMilli(it.timeEpochMillis))})"
                } ?: "Body Fat: No data in DB"
            } else "Body Fat: No data in DB"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Body Fat: Loading...")

    val bodyTemperatureData: StateFlow<String> = bodyTemperatureRecordDao.getAllObservable()
        .map { records ->
            if (records.isNotEmpty()) {
                records.maxByOrNull { it.timeEpochMillis }?.let {
                    "Latest Body Temperature (DB): ${String.format("%.2f", it.temperatureInCelsius)} °C (at ${formatter.format(Instant.ofEpochMilli(it.timeEpochMillis))})"
                } ?: "Body Temperature: No data in DB"
            } else "Body Temperature: No data in DB"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Body Temperature: Loading...")

    val bodyWaterMassData: StateFlow<String> = bodyWaterMassRecordDao.getAllObservable()
        .map { records ->
            if (records.isNotEmpty()) {
                records.maxByOrNull { it.timeEpochMillis }?.let {
                    "Latest Body Water Mass (DB): ${String.format("%.2f", it.massInKilograms)} kg (at ${formatter.format(Instant.ofEpochMilli(it.timeEpochMillis))})"
                } ?: "Body Water Mass: No data in DB"
            } else "Body Water Mass: No data in DB"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Body Water Mass: Loading...")

    val boneMassData: StateFlow<String> = boneMassRecordDao.getAllObservable()
        .map { records ->
            if (records.isNotEmpty()) {
                records.maxByOrNull { it.timeEpochMillis }?.let {
                    "Latest Bone Mass (DB): ${String.format("%.2f", it.massInKilograms)} kg (at ${formatter.format(Instant.ofEpochMilli(it.timeEpochMillis))})"
                } ?: "Bone Mass: No data in DB"
            } else "Bone Mass: No data in DB"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Bone Mass: Loading...")

    val heightData: StateFlow<String> = heightRecordDao.getAllObservable()
        .map { records: List<HeightRecordEntity> ->
            if (records.isNotEmpty()) {
                records.maxByOrNull { it.timeEpochMillis }?.let { latestRecord: HeightRecordEntity ->
                    "Latest Height (DB): ${String.format("%.2f", latestRecord.heightInMeters * 100)} cm (at ${formatter.format(Instant.ofEpochMilli(latestRecord.timeEpochMillis))})"
                } ?: "Height: No data in DB"
            } else "Height: No data in DB"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Height: Loading...")

    val leanBodyMassData: StateFlow<String> = leanBodyMassRecordDao.getAllObservable()
        .map { records: List<LeanBodyMassRecordEntity> ->
            if (records.isNotEmpty()) {
                records.maxByOrNull { it.timeEpochMillis }?.let { latestRecord: LeanBodyMassRecordEntity ->
                    "Latest Lean Body Mass (DB): ${String.format("%.2f", latestRecord.massInKilograms)} kg (at ${formatter.format(Instant.ofEpochMilli(latestRecord.timeEpochMillis))})"
                } ?: "Lean Body Mass: No data in DB"
            } else "Lean Body Mass: No data in DB"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Lean Body Mass: Loading...")

    val hydrationData: StateFlow<String> = hydrationRecordDao.getAllObservable()
        .map { records: List<HydrationRecordEntity> ->
            if (records.isNotEmpty()) {
                val totalVolume = records.sumOf { it.volumeInMilliliters / 1000.0 }
                "Total Hydration (DB): ${String.format("%.2f", totalVolume)} L"
            } else "Hydration: No data in DB"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Hydration: Loading...")

    val nutritionData: StateFlow<String> = nutritionRecordDao.getAllObservable()
        .map { records: List<NutritionRecordEntity> ->
            if (records.isNotEmpty()) {
                val totalCalories = records.sumOf { it.calories ?: 0.0 }
                "Total Nutrition Calories (DB): ${String.format("%.2f", totalCalories)} kcal"
            } else "Nutrition: No data in DB"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Nutrition: Loading...")

    val bloodPressureData: StateFlow<String> = bloodPressureRecordDao.getAllObservable()
        .map { records: List<BloodPressureRecordEntity> ->
            if (records.isNotEmpty()) {
                records.maxByOrNull { it.timeEpochMillis }?.let { latestRecord: BloodPressureRecordEntity ->
                    "Latest Blood Pressure (DB): ${latestRecord.systolic}/${latestRecord.diastolic} mmHg (at ${formatter.format(Instant.ofEpochMilli(latestRecord.timeEpochMillis))})"
                } ?: "Blood Pressure: No data in DB"
            } else "Blood Pressure: No data in DB"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Blood Pressure: Loading...")

    val oxygenSaturationData: StateFlow<String> = oxygenSaturationRecordDao.getAllObservable()
        .map { records: List<OxygenSaturationRecordEntity> ->
            if (records.isNotEmpty()) {
                records.maxByOrNull { it.timeEpochMillis }?.let { latestRecord: OxygenSaturationRecordEntity ->
                    "Latest Oxygen Saturation (DB): ${String.format("%.2f", latestRecord.percentage)}% (at ${formatter.format(Instant.ofEpochMilli(latestRecord.timeEpochMillis))})"
                } ?: "Oxygen Saturation: No data in DB"
            } else "Oxygen Saturation: No data in DB"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Oxygen Saturation: Loading...")

    val respiratoryRateData: StateFlow<String> = respiratoryRateRecordDao.getAllObservable()
        .map { records: List<RespiratoryRateRecordEntity> ->
            if (records.isNotEmpty()) {
                records.maxByOrNull { it.timeEpochMillis }?.let { latestRecord: RespiratoryRateRecordEntity ->
                    "Latest Respiratory Rate (DB): ${String.format("%.2f", latestRecord.rate)} breaths/min (at ${formatter.format(Instant.ofEpochMilli(latestRecord.timeEpochMillis))})"
                } ?: "Respiratory Rate: No data in DB"
            } else "Respiratory Rate: No data in DB"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Respiratory Rate: Loading...")

    private val _requestPermissionsLauncherEvent = MutableLiveData<Set<String>>()
    val requestPermissionsLauncherEvent: LiveData<Set<String>> get() = _requestPermissionsLauncherEvent

    private val _allPermissionsGranted = MutableLiveData<Boolean>(false)
    val allPermissionsGranted: LiveData<Boolean> get() = _allPermissionsGranted

    fun checkOrRequestPermissions() {
        viewModelScope.launch {
            try {
                val currentlyGrantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()
                if (currentlyGrantedPermissions.containsAll(PERMISSIONS)) {
                    Log.d(TAG, "All permissions already granted.")
                    _allPermissionsGranted.postValue(true)
                    handleInitialWorkerScheduling() 
                } else {
                    Log.d(TAG, "Not all permissions granted; triggering request.")
                    _allPermissionsGranted.postValue(false)
                    val permissionsToRequest = PERMISSIONS.filterNot { currentlyGrantedPermissions.contains(it) }.toSet()
                    if (permissionsToRequest.isNotEmpty()){
                        _requestPermissionsLauncherEvent.postValue(permissionsToRequest)
                    } else { // Should not happen if previous check failed
                         _allPermissionsGranted.postValue(true) 
                         handleInitialWorkerScheduling()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking permissions: ${e.message}", e)
                _allPermissionsGranted.postValue(false)
            }
        }
    }

    fun onPermissionsResult(grantedPermissions: Set<String>) {
        if (grantedPermissions.containsAll(PERMISSIONS)) {
            Log.d(TAG, "All required permissions granted from launcher result.")
            _allPermissionsGranted.postValue(true)
            handleInitialWorkerScheduling()
        } else {
            _allPermissionsGranted.postValue(false)
            val deniedPermissions = PERMISSIONS.filterNot { grantedPermissions.contains(it) }
            Log.w(TAG, "Some permissions denied: $deniedPermissions. Background sync may be affected.")
        }
    }

    private fun handleInitialWorkerScheduling() {
        viewModelScope.launch {
            val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val initialWorkerScheduled = prefs.getBoolean(KEY_INITIAL_WORKER_SCHEDULED, false)

            if (!initialWorkerScheduled) {
                Log.i(TAG, "Initial HealthDataFetcherWorker not yet scheduled. Enqueuing OneTimeWorkRequest.")
                // Changed to enqueue HealthDataFetcherWorker directly
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED) 
                    .build()
                val oneTimeFetchRequest = OneTimeWorkRequestBuilder<HealthDataFetcherWorker>()
                    .setConstraints(constraints)
                    .addTag("InitialPermissionHealthDataFetcherWorker") 
                    .build()
                WorkManager.getInstance(application).enqueue(oneTimeFetchRequest)
                Log.d(TAG, "Enqueued OneTimeWorkRequest for HealthDataFetcherWorker with tag: InitialPermissionHealthDataFetcherWorker")
                
                prefs.edit().putBoolean(KEY_INITIAL_WORKER_SCHEDULED, true).apply()
                Log.i(TAG, "Initial HealthDataFetcherWorker OneTimeWorkRequest enqueued and preference updated.")
            } else {
                Log.d(TAG, "Initial HealthDataFetcherWorker has already been scheduled previously.")
            }
        }
    }

    fun triggerDataRefresh() {
         if (allPermissionsGranted.value == true) {
            Log.i(TAG, "User triggered data refresh. Enqueuing OneTimeWorkRequest for HealthDataFetcherWorker.")
            // Changed to call the modified enqueueHealthDataFetcherWorker method
            enqueueHealthDataFetcherWorker("UserTriggeredHealthDataFetcherWorker")
        } else {
            Log.w(TAG, "User triggered data refresh but permissions not granted. Requesting permissions.")
            checkOrRequestPermissions()
        }
    }

    // Renamed from triggerDataRefreshWorker and changed to enqueue HealthDataFetcherWorker
    private fun enqueueHealthDataFetcherWorker(tag: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) 
            .build()
        val oneTimeFetchRequest = OneTimeWorkRequestBuilder<HealthDataFetcherWorker>() // Changed to HealthDataFetcherWorker
            .setConstraints(constraints)
            .addTag(tag) 
            .build()
        WorkManager.getInstance(application).enqueue(oneTimeFetchRequest)
        Log.d(TAG, "Enqueued OneTimeWorkRequest for HealthDataFetcherWorker with tag: $tag")
    }
}
