package io.github.hitoshura25.healthsyncapp

import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
// Health Connect Record types (used for permission definitions)
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
import androidx.health.connect.client.records.WheelchairPushesRecord
// Lifecycle and coroutines imports
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.hitoshura25.healthsyncapp.data.repository.HealthDataRepository
import kotlinx.coroutines.launch
// Java time imports
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class MainViewModel(
    private val healthDataRepository: HealthDataRepository,
    private val healthConnectClient: HealthConnectClient // Still needed for permission controller
) : ViewModel() {

    private val TAG = "MainViewModel"

    val PERMISSIONS =
        setOf(
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getReadPermission(HeartRateRecord::class),
            HealthPermission.getReadPermission(SleepSessionRecord::class),
            HealthPermission.getReadPermission(BloodGlucoseRecord::class),
            // Add other permissions as needed from the original list
            HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
            HealthPermission.getReadPermission(BasalBodyTemperatureRecord::class),
            HealthPermission.getReadPermission(BasalMetabolicRateRecord::class),
            HealthPermission.getReadPermission(CyclingPedalingCadenceRecord::class),
            HealthPermission.getReadPermission(DistanceRecord::class),
            HealthPermission.getReadPermission(ElevationGainedRecord::class),
            HealthPermission.getReadPermission(ExerciseSessionRecord::class),
            HealthPermission.getReadPermission(FloorsClimbedRecord::class),
            HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class),
            HealthPermission.getReadPermission(PowerRecord::class),
            HealthPermission.getReadPermission(RestingHeartRateRecord::class),
            HealthPermission.getReadPermission(SpeedRecord::class),
            HealthPermission.getReadPermission(StepsCadenceRecord::class),
            HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
            HealthPermission.getReadPermission(Vo2MaxRecord::class),
            HealthPermission.getReadPermission(BodyFatRecord::class),
            HealthPermission.getReadPermission(BodyTemperatureRecord::class),
            HealthPermission.getReadPermission(BodyWaterMassRecord::class),
            HealthPermission.getReadPermission(BoneMassRecord::class),
            HealthPermission.getReadPermission(HeightRecord::class),
            HealthPermission.getReadPermission(LeanBodyMassRecord::class),
            HealthPermission.getReadPermission(WeightRecord::class),
            HealthPermission.getReadPermission(HydrationRecord::class),
            HealthPermission.getReadPermission(NutritionRecord::class),
            HealthPermission.getReadPermission(BloodPressureRecord::class),
            HealthPermission.getReadPermission(OxygenSaturationRecord::class),
            HealthPermission.getReadPermission(RespiratoryRateRecord::class)
        )

    private val _stepsData = MutableLiveData<String>("Steps: Not loaded")
    val stepsData: LiveData<String> get() = _stepsData

    private val _heartRateData = MutableLiveData<String>("Heart Rate: Not loaded")
    val heartRateData: LiveData<String> get() = _heartRateData

    private val _sleepData = MutableLiveData<String>("Sleep: Not loaded")
    val sleepData: LiveData<String> get() = _sleepData

    private val _bloodGlucoseData = MutableLiveData<String>("Blood Glucose: Not loaded")
    val bloodGlucoseData: LiveData<String> get() = _bloodGlucoseData

    private val _requestPermissionsLauncherEvent = MutableLiveData<Set<String>>()
    val requestPermissionsLauncherEvent: LiveData<Set<String>> get() = _requestPermissionsLauncherEvent

    private val _allPermissionsGranted = MutableLiveData<Boolean>(false)
    val allPermissionsGranted: LiveData<Boolean> get() = _allPermissionsGranted

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault())

    // Removed setHealthConnectClient method

    fun checkOrRequestPermissions() {
        // healthConnectClient is now injected via constructor
        viewModelScope.launch {
            try {
                val currentlyGrantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()
                if (currentlyGrantedPermissions.containsAll(PERMISSIONS)) {
                    Log.d(TAG, "All permissions already granted (checked via ViewModel).")
                    _allPermissionsGranted.postValue(true)
                } else {
                    Log.d(TAG, "Not all permissions granted; triggering request (via ViewModel).")
                    _allPermissionsGranted.postValue(false)
                    val permissionsToRequest = PERMISSIONS.filterNot { currentlyGrantedPermissions.contains(it) }.toSet()
                    if (permissionsToRequest.isNotEmpty()) {
                        _requestPermissionsLauncherEvent.postValue(permissionsToRequest)
                    } else {
                        if (PERMISSIONS.isEmpty()) {
                            Log.d(TAG, "PERMISSIONS set is empty, considering all permissions granted.")
                            _allPermissionsGranted.postValue(true)
                        } else {
                            Log.w(TAG, "No specific permissions left to request, but not all were considered granted. Check PERMISSIONS set.")
                            _allPermissionsGranted.postValue(false)
                        }
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
            Log.d(TAG, "All required permissions granted from launcher result (processed by ViewModel).")
            _allPermissionsGranted.postValue(true)
        } else {
            _allPermissionsGranted.postValue(false)
            val deniedPermissions = PERMISSIONS.filterNot { grantedPermissions.contains(it) }
            Log.w(TAG, "Some or all required permissions denied from launcher result (processed by ViewModel). Denied: $deniedPermissions")
        }
    }

    fun fetchHealthData() {
        if (allPermissionsGranted.value != true) {
            Log.w(TAG, "Cannot fetch data: Permissions not granted. allPermissionsGranted.value: ${allPermissionsGranted.value}")
            return
        }
        Log.d(TAG, "Fetching and saving health data via repository because permissions are granted.")
        viewModelScope.launch {
            val now = Instant.now()
            val startTime24h = now.minus(1, ChronoUnit.DAYS)
            val startTime48h = now.minus(2, ChronoUnit.DAYS) // For sleep data

            // Fetch and Save Steps
            val stepsEntities = healthDataRepository.fetchAndSaveStepsData(startTime24h, now)
            if (stepsEntities.isNotEmpty()) {
                val totalSteps = stepsEntities.sumOf { it.count }
                _stepsData.postValue("Steps (last 24h): $totalSteps")
            } else {
                _stepsData.postValue("Steps: No data found for last 24h.")
            }

            // Fetch and Save Heart Rate
            val heartRateEntities = healthDataRepository.fetchAndSaveHeartRateData(startTime24h, now)
            if (heartRateEntities.isNotEmpty()) {
                val latestSample = heartRateEntities.maxByOrNull { it.sampleTimeEpochMillis }
                if (latestSample != null) {
                    val time = Instant.ofEpochMilli(latestSample.sampleTimeEpochMillis)
                    _heartRateData.postValue("Latest Heart Rate: ${latestSample.beatsPerMinute} BPM at ${formatter.format(time)}")
                } else {
                    _heartRateData.postValue("Heart Rate: No samples found.")
                }
            } else {
                _heartRateData.postValue("Heart Rate: No data found for last 24h.")
            }

            // Fetch and Save Sleep Sessions
            val sleepEntities = healthDataRepository.fetchAndSaveSleepSessions(startTime48h, now) // Using 48h for sleep
            if (sleepEntities.isNotEmpty()) {
                val latestSession = sleepEntities.maxByOrNull { it.endTimeEpochMillis }
                if (latestSession != null && latestSession.durationMillis != null) {
                    val durationMinutes = latestSession.durationMillis / (1000 * 60)
                    val endTime = Instant.ofEpochMilli(latestSession.endTimeEpochMillis)
                    _sleepData.postValue("Last Sleep: $durationMinutes mins (ends ${formatter.format(endTime)})")
                } else {
                    _sleepData.postValue("Sleep: No session data.")
                }
            } else {
                _sleepData.postValue("Sleep: No data found for last 48h.")
            }

            // Fetch and Save Blood Glucose
            val bloodGlucoseEntities = healthDataRepository.fetchAndSaveBloodGlucoseData(startTime24h, now)
            if (bloodGlucoseEntities.isNotEmpty()) {
                val latestRecord = bloodGlucoseEntities.maxByOrNull { it.timeEpochMillis }
                if (latestRecord != null) {
                    val time = Instant.ofEpochMilli(latestRecord.timeEpochMillis)
                    _bloodGlucoseData.postValue("Latest Glucose: ${latestRecord.levelMgdL} mg/dL at ${formatter.format(time)}")
                } else {
                    _bloodGlucoseData.postValue("Blood Glucose: No level data.")
                }
            } else {
                _bloodGlucoseData.postValue("Blood Glucose: No data found for last 24h.")
            }
        }
    }
    // Removed internal data reading methods as their logic is now in HealthDataRepositoryImpl
}
