package io.github.hitoshura25.healthsyncapp

import android.app.Application
import io.github.hitoshura25.healthsyncapp.data.local.database.AppDatabase

class HealthSyncAppApplication : Application() {

    // Lazy initialization of the database
    // The database instance will be created when it's first accessed.
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        // You can pre-warm the database instance here if needed, but lazy initialization is often fine.
        // For example: database.isOpen // This would trigger the lazy initialization
    }
}
