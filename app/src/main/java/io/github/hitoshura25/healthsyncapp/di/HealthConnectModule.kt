package io.github.hitoshura25.healthsyncapp.di

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class HealthConnectModule { // Changed from object to class

    @Provides
    @Singleton
    fun provideHealthConnectClient(@ApplicationContext appContext: Context): HealthConnectClient {
        return HealthConnectClient.getOrCreate(appContext)
    }
}
