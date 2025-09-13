package io.github.hitoshura25.healthsyncapp.worker

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.hitoshura25.healthsyncapp.HealthSyncAppApplication
import io.github.hitoshura25.healthsyncapp.data.repository.HealthDataRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.temporal.ChronoUnit

class SyncWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val TAG = "SyncWorker"
    // Define a lookback period for fetching data, e.g., last 48 hours.
    // This ensures we capture recent data without re-fetching everything always.
    private val DATA_FETCH_LOOKBACK_HOURS = 48L 

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.i(TAG, "SyncWorker started its work cycle.")

        try {
            val application = appContext as? HealthSyncAppApplication
                ?: return@withContext Result.failure().also {
                    Log.e(TAG, "Application context is not HealthSyncAppApplication")
                }

            if (HealthConnectClient.getSdkStatus(applicationContext) != HealthConnectClient.SDK_AVAILABLE) {
                Log.w(TAG, "Health Connect SDK not available. SyncWorker cannot fetch new data.")
                // We might still want to try processing previously fetched unsynced data if any.
                // For now, let's allow it to proceed to the processing phase.
            }
            
            val healthConnectClient = HealthConnectClient.getOrCreate(applicationContext)
            val db = application.database
            val healthDataRepository = HealthDataRepositoryImpl(
                healthConnectClient,
                db.stepsRecordDao(),
                db.heartRateSampleDao(),
                db.sleepSessionDao(),
                db.bloodGlucoseDao()
            )

            // --- Phase 1: Fetch new data from Health Connect and save to local DB ---
            Log.d(TAG, "Phase 1: Fetching new data from Health Connect.")
            val endTime = Instant.now()
            val startTime = endTime.minus(DATA_FETCH_LOOKBACK_HOURS, ChronoUnit.HOURS)
            var fetchSuccess = false
            if (HealthConnectClient.getSdkStatus(applicationContext) == HealthConnectClient.SDK_AVAILABLE) {
                // Only attempt to fetch if SDK is available and permissions are implicitly expected to be granted.
                // The repository method itself handles specific read errors.
                try {
                    val permissions = healthConnectClient.permissionController.getGrantedPermissions()
                    if (permissions.isNotEmpty()) { // Basic check if any permissions are granted
                        fetchSuccess = healthDataRepository.fetchAllDataTypesFromHealthConnectAndSave(startTime, endTime)
                        Log.i(TAG, "New data fetch attempt result: ${if(fetchSuccess) "Success" else "Partial/Failed"}")
                    } else {
                        Log.w(TAG, "No Health Connect permissions granted. Skipping fetch phase.")
                        fetchSuccess = true // Not a failure of the worker, just can't fetch.
                    }
                } catch (e: SecurityException) {
                    Log.e(TAG, "SecurityException during permission check or data fetch: ${e.message}. This likely means permissions are missing.")
                    // Do not mark as overall worker failure, as processing existing data might still be possible.
                    fetchSuccess = true // Considered 'successful' in terms of worker execution, but fetch failed.
                } catch (e: Exception) {
                    Log.e(TAG, "Exception during data fetch: ${e.message}")
                    fetchSuccess = false // Indicate fetch phase had issues
                }
            } else {
                Log.i(TAG, "Health Connect SDK not available, skipping data fetch phase.")
                fetchSuccess = true // Not a failure if SDK isn't there, just can't fetch.
            }

            // --- Phase 2: Process unsynced data from local DB ---
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
            // Worker result should depend on whether critical operations succeeded.
            // If fetch had an actual error (not just SDK unavailable/no perms), we might consider retrying.
            // For now, if processing phase is reached, consider it success for the worker's own execution.
            return@withContext if (fetchSuccess || totalProcessedInWorker > 0) Result.success() else Result.success() // Or Result.failure if fetch was critical and failed

        } catch (e: Exception) {
            Log.e(TAG, "Critical error in SyncWorker doWork(): ${e.message}", e)
            return@withContext Result.failure()
        }
    }
}
