package io.github.hitoshura25.healthsyncapp

import android.app.Application
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import io.github.hitoshura25.healthsyncapp.data.local.database.AppDatabase
import io.github.hitoshura25.healthsyncapp.worker.SyncWorker
import java.util.concurrent.TimeUnit

class HealthSyncAppApplication : Application() {

    private val TAG = "HealthSyncApp"
    // SharedPreferences constants for initial data operations will be moved to ViewModel or a shared const file.

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Application onCreate - Initializing database and scheduling periodic sync.")
        setupPeriodicSync()
    }

    // Removed scheduleInitialSync() - this logic will move to MainViewModel

    private fun setupPeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Periodic sync will run as scheduled, but its first successful data processing
        // will depend on permissions being granted and data being available.
        val periodicSyncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES // Minimum 15 minutes for periodic
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            SyncWorker::class.java.name, // Unique name for the periodic work
            ExistingPeriodicWorkPolicy.KEEP, // Keep existing if already scheduled
            periodicSyncRequest
        )
        Log.i(TAG, "Periodic sync worker (15 min interval) scheduled with KEEP policy.")
    }
}
