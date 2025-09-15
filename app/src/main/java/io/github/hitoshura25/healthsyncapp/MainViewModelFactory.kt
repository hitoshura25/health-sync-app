package io.github.hitoshura25.healthsyncapp

import android.app.Application
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BloodGlucoseDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.HeartRateSampleDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.SleepSessionDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.SleepStageDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.StepsRecordDao

// Define an EntryPoint to access Hilt dependencies from non-Hilt-aware classes
@EntryPoint
@InstallIn(SingletonComponent::class) // DAOs are Singleton scoped as per AppModule
interface ViewModelFactoryEntryPoint {
    fun stepsRecordDao(): StepsRecordDao
    fun heartRateSampleDao(): HeartRateSampleDao
    fun sleepSessionDao(): SleepSessionDao
    fun sleepStageDao(): SleepStageDao // Added SleepStageDao
    fun bloodGlucoseDao(): BloodGlucoseDao
}

class MainViewModelFactory(
    private val application: Application,
    private val healthConnectClient: HealthConnectClient
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            // Access DAOs via the Hilt EntryPoint
            val entryPoint = EntryPointAccessors.fromApplication(
                application.applicationContext, // Use application context for SingletonComponent
                ViewModelFactoryEntryPoint::class.java
            )

            val stepsDao = entryPoint.stepsRecordDao()
            val heartRateDao = entryPoint.heartRateSampleDao()
            val sleepSessionDao = entryPoint.sleepSessionDao()
            val sleepStageDao = entryPoint.sleepStageDao() // Get SleepStageDao
            val bloodGlucoseDao = entryPoint.bloodGlucoseDao()

            // Pass Application, DAOs (including SleepStageDao), and HealthConnectClient to MainViewModel
            return MainViewModel(
                application,
                stepsDao,
                heartRateDao,
                sleepSessionDao,
                bloodGlucoseDao,
                sleepStageDao, // Added SleepStageDao
                healthConnectClient
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }
}
