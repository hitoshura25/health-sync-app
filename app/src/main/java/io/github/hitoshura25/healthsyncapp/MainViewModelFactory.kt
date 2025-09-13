package io.github.hitoshura25.healthsyncapp

// Import DAOs directly
import android.app.Application
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MainViewModelFactory(
    private val application: Application,
    private val healthConnectClient: HealthConnectClient // Still needed for permission checks
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            val database = (application as HealthSyncAppApplication).database
            // Get instances of DAOs
            val stepsDao = database.stepsRecordDao()
            val heartRateDao = database.heartRateSampleDao()
            val sleepDao = database.sleepSessionDao()
            val bloodGlucoseDao = database.bloodGlucoseDao()

            // Pass Application, DAOs, and HealthConnectClient to MainViewModel
            return MainViewModel(
                application,
                stepsDao,
                heartRateDao,
                sleepDao,
                bloodGlucoseDao,
                healthConnectClient
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }
}
