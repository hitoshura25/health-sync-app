package io.github.hitoshura25.healthsyncapp

import android.app.Application
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.hitoshura25.healthsyncapp.data.local.database.AppDatabase
import io.github.hitoshura25.healthsyncapp.data.repository.HealthDataRepositoryImpl

class MainViewModelFactory(
    private val application: Application,
    private val healthConnectClient: HealthConnectClient
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            // Get DAOs from the AppDatabase instance (via Application context)
            val database = (application as HealthSyncAppApplication).database // Or AppDatabase.getDatabase(application)
            val stepsDao = database.stepsRecordDao()
            val heartRateDao = database.heartRateSampleDao()
            val sleepDao = database.sleepSessionDao()
            val bloodGlucoseDao = database.bloodGlucoseDao()

            // Create the repository instance
            val repository = HealthDataRepositoryImpl(
                healthConnectClient = healthConnectClient,
                stepsRecordDao = stepsDao,
                heartRateSampleDao = heartRateDao,
                sleepSessionDao = sleepDao,
                bloodGlucoseDao = bloodGlucoseDao
            )

            return MainViewModel(repository, healthConnectClient) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }
}
