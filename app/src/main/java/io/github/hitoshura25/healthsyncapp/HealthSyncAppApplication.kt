package io.github.hitoshura25.healthsyncapp

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import io.github.hitoshura25.healthsyncapp.worker.HealthDataFetcherWorker // Import the worker
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class HealthSyncAppApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        setupHealthDataFetcherWorker()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.DEBUG) // Optional: for more logs
            .build()

    private fun setupHealthDataFetcherWorker() {
        val workManager = WorkManager.getInstance(this)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicFetchRequest = 
            PeriodicWorkRequestBuilder<HealthDataFetcherWorker>(24, TimeUnit.HOURS) // Repeat every 24 hours
                .setConstraints(constraints)
                .build()

        workManager.enqueueUniquePeriodicWork(
            HealthDataFetcherWorker.WORK_NAME, // Use the unique name from the worker companion object
            ExistingPeriodicWorkPolicy.KEEP, // Or REPLACE if you want new parameters to take effect
            periodicFetchRequest
        )
    }
}
