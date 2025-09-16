package io.github.hitoshura25.healthsyncapp.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BloodGlucoseDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.HeartRateSampleDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.SleepSessionDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.SleepStageDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.StepsRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BloodGlucoseEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.HeartRateSampleEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.SleepSessionEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.SleepStageEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.StepsRecordEntity

@Database(
    entities = [
        StepsRecordEntity::class,
        HeartRateSampleEntity::class,
        SleepSessionEntity::class,
        SleepStageEntity::class,
        BloodGlucoseEntity::class
    ],
    version = 3, // Incremented version to 3 for isSynced column removal
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun stepsRecordDao(): StepsRecordDao
    abstract fun heartRateSampleDao(): HeartRateSampleDao
    abstract fun sleepSessionDao(): SleepSessionDao
    abstract fun sleepStageDao(): SleepStageDao
    abstract fun bloodGlucoseDao(): BloodGlucoseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. BloodGlucoseEntity: Remove isSynced
                db.execSQL("CREATE TABLE blood_glucose_records_new (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "health_connect_uid TEXT NOT NULL, " +
                        "time_epoch_millis INTEGER NOT NULL, " +
                        "zone_offset_id TEXT, " +
                        "level_in_milligrams_per_deciliter REAL NOT NULL, " +
                        "specimen_source INTEGER NOT NULL, " +
                        "meal_type INTEGER NOT NULL, " +
                        "relation_to_meal INTEGER NOT NULL, " +
                        "data_origin_package_name TEXT NOT NULL, " +
                        "hc_last_modified_time_epoch_millis INTEGER NOT NULL, " +
                        "client_record_id TEXT, " +
                        "client_record_version INTEGER NOT NULL DEFAULT 0, " +
                        "app_record_fetch_time_epoch_millis INTEGER NOT NULL)")
                db.execSQL("INSERT INTO blood_glucose_records_new (id, health_connect_uid, time_epoch_millis, zone_offset_id, level_in_milligrams_per_deciliter, specimen_source, meal_type, relation_to_meal, data_origin_package_name, hc_last_modified_time_epoch_millis, client_record_id, client_record_version, app_record_fetch_time_epoch_millis) " +
                        "SELECT id, health_connect_uid, time_epoch_millis, zone_offset_id, level_in_milligrams_per_deciliter, specimen_source, meal_type, relation_to_meal, data_origin_package_name, hc_last_modified_time_epoch_millis, client_record_id, client_record_version, app_record_fetch_time_epoch_millis FROM blood_glucose_records")
                db.execSQL("DROP TABLE blood_glucose_records")
                db.execSQL("ALTER TABLE blood_glucose_records_new RENAME TO blood_glucose_records")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_blood_glucose_records_health_connect_uid ON blood_glucose_records (health_connect_uid)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_blood_glucose_records_time_epoch_millis ON blood_glucose_records (time_epoch_millis)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_blood_glucose_records_hc_last_modified_time_epoch_millis ON blood_glucose_records (hc_last_modified_time_epoch_millis)")

                // 2. HeartRateSampleEntity: Remove isSynced
                db.execSQL("CREATE TABLE heart_rate_samples_new (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "hc_record_uid TEXT NOT NULL, " +
                        "sample_time_epoch_millis INTEGER NOT NULL, " +
                        "beats_per_minute INTEGER NOT NULL, " +
                        "zone_offset_id TEXT, " +
                        "app_record_fetch_time_epoch_millis INTEGER NOT NULL)")
                db.execSQL("INSERT INTO heart_rate_samples_new (id, hc_record_uid, sample_time_epoch_millis, beats_per_minute, zone_offset_id, app_record_fetch_time_epoch_millis) " +
                        "SELECT id, hc_record_uid, sample_time_epoch_millis, beats_per_minute, zone_offset_id, app_record_fetch_time_epoch_millis FROM heart_rate_samples")
                db.execSQL("DROP TABLE heart_rate_samples")
                db.execSQL("ALTER TABLE heart_rate_samples_new RENAME TO heart_rate_samples")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_heart_rate_samples_sample_time_epoch_millis ON heart_rate_samples (sample_time_epoch_millis)")

                // 3. SleepSessionEntity: Remove isSynced
                db.execSQL("CREATE TABLE sleep_sessions_new (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "health_connect_uid TEXT NOT NULL, " +
                        "title TEXT, " +
                        "notes TEXT, " +
                        "start_time_epoch_millis INTEGER NOT NULL, " +
                        "start_zone_offset_id TEXT, " +
                        "end_time_epoch_millis INTEGER NOT NULL, " +
                        "end_zone_offset_id TEXT, " +
                        "duration_millis INTEGER, " +
                        "app_record_fetch_time_epoch_millis INTEGER NOT NULL)")
                db.execSQL("INSERT INTO sleep_sessions_new (id, health_connect_uid, title, notes, start_time_epoch_millis, start_zone_offset_id, end_time_epoch_millis, end_zone_offset_id, duration_millis, app_record_fetch_time_epoch_millis) " +
                        "SELECT id, health_connect_uid, title, notes, start_time_epoch_millis, start_zone_offset_id, end_time_epoch_millis, end_zone_offset_id, duration_millis, app_record_fetch_time_epoch_millis FROM sleep_sessions")
                db.execSQL("DROP TABLE sleep_sessions")
                db.execSQL("ALTER TABLE sleep_sessions_new RENAME TO sleep_sessions")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_sleep_sessions_health_connect_uid ON sleep_sessions (health_connect_uid)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_sleep_sessions_start_time_epoch_millis ON sleep_sessions (start_time_epoch_millis)")

                // 4. StepsRecordEntity: Remove isSynced
                db.execSQL("CREATE TABLE steps_records_new (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "health_connect_uid TEXT NOT NULL, " +
                        "count INTEGER NOT NULL, " +
                        "start_time_epoch_millis INTEGER NOT NULL, " +
                        "end_time_epoch_millis INTEGER NOT NULL, " +
                        "zone_offset_id TEXT, " +
                        "app_record_fetch_time_epoch_millis INTEGER NOT NULL)")
                db.execSQL("INSERT INTO steps_records_new (id, health_connect_uid, count, start_time_epoch_millis, end_time_epoch_millis, zone_offset_id, app_record_fetch_time_epoch_millis) " +
                        "SELECT id, health_connect_uid, count, start_time_epoch_millis, end_time_epoch_millis, zone_offset_id, app_record_fetch_time_epoch_millis FROM steps_records")
                db.execSQL("DROP TABLE steps_records")
                db.execSQL("ALTER TABLE steps_records_new RENAME TO steps_records")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_steps_records_health_connect_uid ON steps_records (health_connect_uid)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "health_sync_app_database"
                )
                .addMigrations(MIGRATION_2_3) // Added migration
                // Removed .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
