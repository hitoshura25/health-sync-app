package io.github.hitoshura25.healthsyncapp.worker

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted // Added import
import dagger.assisted.AssistedInject // Added import
import androidx.hilt.work.HiltWorker // Added import
import io.github.hitoshura25.healthsyncapp.data.repository.HealthDataRepository // Added import
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.temporal.ChronoUnit

@HiltWorker // Added annotation
class SyncWorker @AssistedInject constructor(
    @Assisted private val appContext: Context, // appContext is still fine for HealthConnectClient
    @Assisted workerParams: WorkerParameters,
    private val healthDataRepository: HealthDataRepository // Injected repository
) : CoroutineWorker(appContext, workerParams) {

    private val TAG = "SyncWorker"
    private val DATA_FETCH_LOOKBACK_HOURS = 48L 

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.i(TAG, "SyncWorker started its work cycle.")

        try {
            val healthConnectClient: HealthConnectClient? = try {
                HealthConnectClient.getOrCreate(applicationContext)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get HealthConnectClient: ${e.message}")
                null
            }

            // --- Phase 1: Fetch new data from Health Connect and save to local DB ---
            Log.d(TAG, "Phase 1: Fetching new data from Health Connect.")
            val endTime = Instant.now()
            val startTime = endTime.minus(DATA_FETCH_LOOKBACK_HOURS, ChronoUnit.HOURS)
            var fetchSuccess = true // Default to true if SDK not available or no permissions

            if (healthConnectClient != null && HealthConnectClient.getSdkStatus(applicationContext) == HealthConnectClient.SDK_AVAILABLE) {
                try {
                    val permissions = healthConnectClient.permissionController.getGrantedPermissions()
                    if (permissions.isNotEmpty()) { 
                        // Pass healthConnectClient to the repository method
                        fetchSuccess = healthDataRepository.fetchAllDataTypesFromHealthConnectAndSave(healthConnectClient, startTime, endTime)
                        Log.i(TAG, "New data fetch attempt result: ${if(fetchSuccess) "Success" else "Partial/Failed"}")
                    } else {
                        Log.w(TAG, "No Health Connect permissions granted. Skipping fetch phase.")
                        // fetchSuccess remains true (not a worker failure)
                    }
                } catch (e: SecurityException) {
                    Log.e(TAG, "SecurityException during permission check or data fetch: ${e.message}. This likely means permissions are missing.")
                    // fetchSuccess remains true (not a worker failure, considered 'successful' in terms of worker execution)
                } catch (e: Exception) {
                    Log.e(TAG, "Exception during data fetch: ${e.message}")
                    fetchSuccess = false // Indicate fetch phase had issues
                }
            } else if (healthConnectClient == null) {
                 Log.w(TAG, "Health Connect Client is null. Skipping fetch phase.")
                 // fetchSuccess remains true
            } else {
                Log.i(TAG, "Health Connect SDK not available, skipping data fetch phase.")
                // fetchSuccess remains true
            }

            // --- Phase 2: Process unsynced data from local DB ---
            // This part uses the injected healthDataRepository and remains largely the same
            Log.d(TAG, "Phase 2: Processing unsynced data from local database.")
            var totalProcessedInWorker = 0
            val processingMessage = StringBuilder("SyncWorker processed data:\n")

            val unsyncedSteps = healthDataRepository.getUnsyncedStepsRecords()
            if (unsyncedSteps.isNotEmpty()) {
                val ids = unsyncedSteps.map { it.id }
                val updatedRows = healthDataRepository.markStepsRecordsAsSynced(ids)
                processingMessage.append("- Steps: Found ${unsyncedSteps.size}, Marked $updatedRows as synced.\n")
                totalProcessedInWorker += unsyncedSteps.size
                Log.i(TAG, "Processed ${unsyncedSteps.size} steps records, marked $updatedRows as synced.")
            }

            val unsyncedHeartRate = healthDataRepository.getUnsyncedHeartRateSamples()
            if (unsyncedHeartRate.isNotEmpty()) {
                val ids = unsyncedHeartRate.map { it.id }
                val updatedRows = healthDataRepository.markHeartRateSamplesAsSynced(ids)
                processingMessage.append("- Heart Rate: Found ${unsyncedHeartRate.size}, Marked $updatedRows as synced.\n")
                totalProcessedInWorker += unsyncedHeartRate.size
                Log.i(TAG, "Processed ${unsyncedHeartRate.size} heart rate samples, marked $updatedRows as synced.")
            }

            val unsyncedSleep = healthDataRepository.getUnsyncedSleepSessions()
            if (unsyncedSleep.isNotEmpty()) {
                val ids = unsyncedSleep.map { it.id }
                val updatedRows = healthDataRepository.markSleepSessionsAsSynced(ids)
                processingMessage.append("- Sleep: Found ${unsyncedSleep.size}, Marked $updatedRows as synced.\n")
                totalProcessedInWorker += unsyncedSleep.size
                Log.i(TAG, "Processed ${unsyncedSleep.size} sleep sessions, marked $updatedRows as synced.")
            }

            val unsyncedBloodGlucose = healthDataRepository.getUnsyncedBloodGlucoseRecords()
            if (unsyncedBloodGlucose.isNotEmpty()) {
                val ids = unsyncedBloodGlucose.map { it.id }
                val updatedRows = healthDataRepository.markBloodGlucoseRecordsAsSynced(ids)
                processingMessage.append("- Blood Glucose: Found ${unsyncedBloodGlucose.size}, Marked $updatedRows as synced.\n")
                totalProcessedInWorker += unsyncedBloodGlucose.size
                Log.i(TAG, "Processed ${unsyncedBloodGlucose.size} blood glucose records, marked $updatedRows as synced.")
            }

            if (totalProcessedInWorker > 0) {
                Log.i(TAG, processingMessage.toString())
            } else {
                Log.i(TAG, "No unsynced data found to process during this cycle.")
            }

            Log.i(TAG, "SyncWorker finished its work cycle.")
            
            // Worker result: if fetchSuccess is false (meaning an actual error during fetch attempt),
            // then the worker should probably retry or fail. Otherwise, success.
            return@withContext if (fetchSuccess) Result.success() else Result.retry() // Or Result.failure() if fetch errors are not retryable

        } catch (e: Exception) {
            Log.e(TAG, "Critical error in SyncWorker doWork(): ${e.message}", e)
            return@withContext Result.failure()
        }
    }
}
