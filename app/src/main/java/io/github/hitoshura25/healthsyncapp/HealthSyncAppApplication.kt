package io.github.hitoshura25.healthsyncapp

import android.app.Application
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
// import androidx.work.NetworkType // Not needed for AvroFileProcessorWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import io.github.hitoshura25.healthsyncapp.worker.AvroFileProcessorWorker // Import the worker
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class HealthSyncAppApplication : Application() {

    private val TAG = "HealthSyncApp"

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Application onCreate - Hilt is initializing.")
        // setupPeriodicSync() // Keep this commented or re-evaluate for SyncWorker separately
        setupAvroFileProcessorWorker() // Add call to schedule the new worker
    }

    private fun setupAvroFileProcessorWorker() {
        val constraints = Constraints.Builder()
            .setRequiresStorageNotLow(true) // Example: only run if storage isn't low
            .build()

        val avroProcessingRequest = PeriodicWorkRequestBuilder<AvroFileProcessorWorker>(
            1, TimeUnit.HOURS // Schedule to run approximately every 1 hour
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            AvroFileProcessorWorker::class.java.name, // Unique name for this periodic work
            ExistingPeriodicWorkPolicy.KEEP, // Keep existing if already scheduled
            avroProcessingRequest
        )
        Log.i(TAG, "Periodic AvroFileProcessorWorker (1-hour interval) scheduled with KEEP policy.")
    }

    /* // Commented out SyncWorker scheduling - can be re-added or managed elsewhere
    private fun setupPeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicSyncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            SyncWorker::class.java.name, 
            ExistingPeriodicWorkPolicy.KEEP, 
            periodicSyncRequest
        )
        Log.i(TAG, "Periodic sync worker (15 min interval) scheduled with KEEP policy.")
    }
    */
}
