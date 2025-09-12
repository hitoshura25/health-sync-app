package io.github.hitoshura25.healthsyncapp

import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
// Explicit imports for Health Connect Record types
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
// Other Health Connect imports
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
// Lifecycle and coroutines imports
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
// Java time imports
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class MainViewModel : ViewModel() {

    private val TAG = "MainViewModel"

    val PERMISSIONS =
        setOf(
            // Activity
            HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
            HealthPermission.getReadPermission(BasalBodyTemperatureRecord::class),
            HealthPermission.getReadPermission(BasalMetabolicRateRecord::class),
            HealthPermission.getReadPermission(CyclingPedalingCadenceRecord::class),
            HealthPermission.getReadPermission(DistanceRecord::class),
            HealthPermission.getReadPermission(ElevationGainedRecord::class),
            HealthPermission.getReadPermission(ExerciseSessionRecord::class),
            HealthPermission.getReadPermission(FloorsClimbedRecord::class),
            HealthPermission.getReadPermission(HeartRateRecord::class),
            HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class),
            HealthPermission.getReadPermission(PowerRecord::class),
            HealthPermission.getReadPermission(RestingHeartRateRecord::class),
            HealthPermission.getReadPermission(SpeedRecord::class),
            HealthPermission.getReadPermission(StepsCadenceRecord::class),
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
            HealthPermission.getReadPermission(Vo2MaxRecord::class),
            // Body Measurement
            HealthPermission.getReadPermission(BodyFatRecord::class),
            HealthPermission.getReadPermission(BodyTemperatureRecord::class),
            HealthPermission.getReadPermission(BodyWaterMassRecord::class),
            HealthPermission.getReadPermission(BoneMassRecord::class),
            HealthPermission.getReadPermission(HeightRecord::class),
            HealthPermission.getReadPermission(LeanBodyMassRecord::class),
            HealthPermission.getReadPermission(WeightRecord::class),
            // Nutrition
            HealthPermission.getReadPermission(HydrationRecord::class),
            HealthPermission.getReadPermission(NutritionRecord::class),
            // Sleep
            HealthPermission.getReadPermission(SleepSessionRecord::class),
            // Vitals
            HealthPermission.getReadPermission(BloodGlucoseRecord::class),
            HealthPermission.getReadPermission(BloodPressureRecord::class),
            HealthPermission.getReadPermission(OxygenSaturationRecord::class),
            HealthPermission.getReadPermission(RespiratoryRateRecord::class)
        )

    private lateinit var healthConnectClient: HealthConnectClient

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

    private val _allPermissionsGranted = MutableLiveData<Boolean>()
    val allPermissionsGranted: LiveData<Boolean> get() = _allPermissionsGranted

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault())

    fun setHealthConnectClient(client: HealthConnectClient) {
        healthConnectClient = client
    }

    fun checkOrRequestPermissions() {
        if (!::healthConnectClient.isInitialized) {
            Log.e(TAG, "HealthConnectClient has not been initialized in ViewModel.")
            _allPermissionsGranted.postValue(false)
            return
        }
        viewModelScope.launch {
            try {
                val currentlyGrantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()
                if (currentlyGrantedPermissions.containsAll(PERMISSIONS)) {
                    Log.d(TAG, "All permissions already granted (checked via ViewModel).")
                    _allPermissionsGranted.postValue(true)
                } else {
                    Log.d(TAG, "Not all permissions granted; triggering request (via ViewModel).")
                    val permissionsToRequest = PERMISSIONS.filterNot { currentlyGrantedPermissions.contains(it) }.toSet()
                    if (permissionsToRequest.isNotEmpty()){
                        _requestPermissionsLauncherEvent.postValue(permissionsToRequest)
                    } else {
                        // All PERMISSIONS are already in currentlyGrantedPermissions,
                        // but containsAll was false. This implies currentlyGrantedPermissions might contain
                        // PERMISSIONS plus others, or PERMISSIONS is empty. If PERMISSIONS is not empty,
                        // and all its elements are in currentlyGrantedPermissions, it should be true.
                        Log.d(TAG, "All requested permissions are already granted, no new permissions to request.")
                        _allPermissionsGranted.postValue(true) // Should be true if all PERMISSIONS are indeed covered.
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking permissions: ${e.message}", e)
                _allPermissionsGranted.postValue(false)
            }
        }
    }

    fun onPermissionsResult(grantedPermissions: Set<String>) { // Parameter type changed
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
        if (!::healthConnectClient.isInitialized || _allPermissionsGranted.value != true) {
            Log.w(TAG, "Cannot fetch data: Client not ready or permissions not granted.")
            _stepsData.postValue("Steps: Permissions not granted or client not ready.")
            _heartRateData.postValue("Heart Rate: Permissions not granted or client not ready.")
            _sleepData.postValue("Sleep: Permissions not granted or client not ready.")
            _bloodGlucoseData.postValue("Blood Glucose: Permissions not granted or client not ready.")
            return
        }
        Log.d(TAG, "Fetching health data for UI.")
        viewModelScope.launch {
            readStepsDataInternal()
            readHeartRateDataInternal()
            readSleepSessionDataInternal()
            readBloodGlucoseDataInternal()
        }
    }

    private suspend fun readStepsDataInternal() {
        try {
            val now = Instant.now()
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(StepsRecord::class, TimeRangeFilter.between(now.minus(1, ChronoUnit.DAYS), now))
            )
            if (response.records.isNotEmpty()) {
                val totalSteps = response.records.sumOf { it.count }
                _stepsData.postValue("Steps (last 24h): $totalSteps")
            } else {
                _stepsData.postValue("Steps: No data found for last 24h.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading Steps: ${e.message}", e)
            _stepsData.postValue("Steps: Error loading data.")
        }
    }

    private suspend fun readHeartRateDataInternal() {
        try {
            val now = Instant.now()
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(HeartRateRecord::class, TimeRangeFilter.between(now.minus(1, ChronoUnit.DAYS), now))
            )
            if (response.records.isNotEmpty()) {
                val latestRecord = response.records.maxByOrNull { it.startTime }?.samples?.maxByOrNull { it.time }
                if (latestRecord != null) {
                    _heartRateData.postValue("Latest Heart Rate: ${latestRecord.beatsPerMinute} BPM at ${formatter.format(latestRecord.time)}")
                } else {
                    _heartRateData.postValue("Heart Rate: No samples found.")
                }
            } else {
                _heartRateData.postValue("Heart Rate: No data found for last 24h.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading Heart Rate: ${e.message}", e)
            _heartRateData.postValue("Heart Rate: Error loading data.")
        }
    }

    private suspend fun readSleepSessionDataInternal() {
        try {
            val now = Instant.now()
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(SleepSessionRecord::class, TimeRangeFilter.between(now.minus(2, ChronoUnit.DAYS), now))
            )
            if (response.records.isNotEmpty()) {
                val latestSession = response.records.maxByOrNull { it.endTime }
                if (latestSession != null) {
                    val duration = ChronoUnit.MINUTES.between(latestSession.startTime, latestSession.endTime)
                    _sleepData.postValue("Last Sleep: $duration mins (ends ${formatter.format(latestSession.endTime)})")
                } else {
                     _sleepData.postValue("Sleep: No session data.")
                }
            } else {
                _sleepData.postValue("Sleep: No data found for last 48h.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading Sleep: ${e.message}", e)
            _sleepData.postValue("Sleep: Error loading data.")
        }
    }

    private suspend fun readBloodGlucoseDataInternal() {
        try {
            val now = Instant.now()
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(BloodGlucoseRecord::class, TimeRangeFilter.between(now.minus(1, ChronoUnit.DAYS), now))
            )
            if (response.records.isNotEmpty()) {
                val latestRecord = response.records.maxByOrNull { it.time }
                if (latestRecord != null) {
                    _bloodGlucoseData.postValue("Latest Glucose: ${latestRecord.level.inMilligramsPerDeciliter} mg/dL at ${formatter.format(latestRecord.time)}")
                } else {
                    _bloodGlucoseData.postValue("Blood Glucose: No level data.")
                }
            } else {
                _bloodGlucoseData.postValue("Blood Glucose: No data found for last 24h.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading Blood Glucose: ${e.message}", e)
            _bloodGlucoseData.postValue("Blood Glucose: Error loading data.")
        }
    }
}
