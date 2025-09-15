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
import io.github.hitoshura25.healthsyncapp.worker.SyncWorker
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
    private val healthConnectClient: HealthConnectClient // Still needed for permission checks
) : ViewModel() {

    private val TAG = "MainViewModel"
    private val PREFS_NAME = "HealthSyncAppPrefs"
    private val KEY_INITIAL_WORKER_SCHEDULED = "initialWorkerScheduled"

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault())

    val PERMISSIONS = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(BloodGlucoseRecord::class),
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
        HealthPermission.getReadPermission(RespiratoryRateRecord::class),
        HealthPermission.PERMISSION_READ_HEALTH_DATA_IN_BACKGROUND
    )

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
                    val duration = it.durationMillis?.let { d -> d / (1000 * 60) } ?: "N/A"
                    // TODO: Could enhance this to query and show number of stages if sleepStageDao is used
                    "Last Sleep (DB): $duration mins (ends ${formatter.format(Instant.ofEpochMilli(it.endTimeEpochMillis))})"
                } ?: "Sleep: No session data in DB"
            } else "Sleep: No data in DB"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Sleep: Loading...")

    val bloodGlucoseData: StateFlow<String> = bloodGlucoseDao.getAllObservable()
        .map { records ->
            if (records.isNotEmpty()) {
                records.maxByOrNull { it.timeEpochMillis }?.let {
                    "Latest Glucose (DB): ${it.levelMgdL} mg/dL at ${formatter.format(Instant.ofEpochMilli(it.timeEpochMillis))}"
                } ?: "Blood Glucose: No level data in DB"
            } else "Blood Glucose: No data in DB"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Blood Glucose: Loading...")


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
                Log.i(TAG, "Initial SyncWorker not yet scheduled. Enqueuing OneTimeWorkRequest.")
                triggerDataRefreshWorker("InitialPermissionSyncWorker") 
                prefs.edit().putBoolean(KEY_INITIAL_WORKER_SCHEDULED, true).apply()
                Log.i(TAG, "Initial SyncWorker OneTimeWorkRequest enqueued and preference updated.")
            } else {
                Log.d(TAG, "Initial SyncWorker has already been scheduled previously.")
            }
        }
    }

    fun triggerDataRefresh() {
         if (allPermissionsGranted.value == true) {
            Log.i(TAG, "User triggered data refresh. Enqueuing OneTimeWorkRequest for SyncWorker.")
            triggerDataRefreshWorker("UserTriggeredSyncWorker")
        } else {
            Log.w(TAG, "User triggered data refresh but permissions not granted. Requesting permissions.")
            checkOrRequestPermissions()
        }
    }

    private fun triggerDataRefreshWorker(tag: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) 
            .build()
        val oneTimeSyncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .addTag(tag) 
            .build()
        WorkManager.getInstance(application).enqueue(oneTimeSyncRequest)
        Log.d(TAG, "Enqueued OneTimeWorkRequest for SyncWorker with tag: $tag")
    }
}
