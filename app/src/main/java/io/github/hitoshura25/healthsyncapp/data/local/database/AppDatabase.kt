package io.github.hitoshura25.healthsyncapp.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BloodGlucoseDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.HeartRateSampleDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.SleepSessionDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.StepsRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BloodGlucoseEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.HeartRateSampleEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.SleepSessionEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.StepsRecordEntity

@Database(
    entities = [
        StepsRecordEntity::class,
        HeartRateSampleEntity::class,
        SleepSessionEntity::class,
        BloodGlucoseEntity::class
    ],
    version = 1,
    exportSchema = false // Set to true if you plan to export schema to version control
)
// @TypeConverters(YourTypeConverters::class) // We'll add this later if needed
abstract class AppDatabase : RoomDatabase() {

    abstract fun stepsRecordDao(): StepsRecordDao
    abstract fun heartRateSampleDao(): HeartRateSampleDao
    abstract fun sleepSessionDao(): SleepSessionDao
    abstract fun bloodGlucoseDao(): BloodGlucoseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "health_sync_app_database"
                )
                // .addMigrations(MIGRATION_X_Y) // Add migrations if you change schema later
                // .fallbackToDestructiveMigration() // Use this only during development if you don't want to write migrations
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
