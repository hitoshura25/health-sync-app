package io.github.hitoshura25.healthsyncapp.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.hitoshura25.healthsyncapp.data.HealthConnectToAvroMapper
import io.github.hitoshura25.healthsyncapp.data.local.database.AppDatabase
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BloodGlucoseDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.HeartRateSampleDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.SleepSessionDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.SleepStageDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.StepsRecordDao
import io.github.hitoshura25.healthsyncapp.file.FileHandler
import io.github.hitoshura25.healthsyncapp.file.FileHandlerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "health_sync_app_database" // Consistent database name
        )
        // .fallbackToDestructiveMigration() // REMOVED fallbackToDestructiveMigration
        .build()
    }

    @Provides
    @Singleton
    fun provideStepsRecordDao(appDatabase: AppDatabase): StepsRecordDao {
        return appDatabase.stepsRecordDao()
    }

    @Provides
    @Singleton
    fun provideHeartRateSampleDao(appDatabase: AppDatabase): HeartRateSampleDao {
        return appDatabase.heartRateSampleDao()
    }

    @Provides
    @Singleton
    fun provideSleepSessionDao(appDatabase: AppDatabase): SleepSessionDao {
        return appDatabase.sleepSessionDao()
    }

    @Provides
    @Singleton
    fun provideSleepStageDao(appDatabase: AppDatabase): SleepStageDao {
        return appDatabase.sleepStageDao()
    }

    @Provides
    @Singleton
    fun provideBloodGlucoseDao(appDatabase: AppDatabase): BloodGlucoseDao {
        return appDatabase.bloodGlucoseDao()
    }

    @Provides
    @Singleton
    fun provideFileHandler(@ApplicationContext appContext: Context): FileHandler {
        return FileHandlerImpl(appContext)
    }

    @Provides
    @Singleton
    fun provideHealthConnectToAvroMapper(): HealthConnectToAvroMapper {
        return HealthConnectToAvroMapper
    }
}
