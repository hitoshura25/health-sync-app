package io.github.hitoshura25.healthsyncapp.data.repository

import androidx.health.connect.client.HealthConnectClient // Added import
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BloodGlucoseEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.HeartRateSampleEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.SleepSessionEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.StepsRecordEntity
import java.time.Instant

/**
 * Repository interface for handling health data operations,
 * abstracting the data sources (Health Connect, Local DB).
 */
interface HealthDataRepository {

    // --- Combined Data Fetching from Health Connect (and saving to local DB) ---
    /**
     * Fetches all relevant data types (steps, heart rate, sleep, blood glucose)
     * from Health Connect for the given time range and saves them to the local database.
     * New records will be marked as not synced.
     * @return true if all fetch and save operations were attempted successfully (individual operations might still fail silently based on their implementation),
     *         false if a major, unrecoverable error occurred during orchestration.
     */
    suspend fun fetchAllDataTypesFromHealthConnectAndSave(
        healthConnectClient: HealthConnectClient, // Added parameter
        startTime: Instant, 
        endTime: Instant
    ): Boolean

    // --- Individual Data Fetching (can be kept for granularity or internal use) ---
    suspend fun fetchAndSaveStepsData(
        healthConnectClient: HealthConnectClient, // Added parameter
        startTime: Instant, 
        endTime: Instant
    ): List<StepsRecordEntity>
    
suspend fun fetchAndSaveHeartRateData(
        healthConnectClient: HealthConnectClient, // Added parameter
        startTime: Instant, 
        endTime: Instant
    ): List<HeartRateSampleEntity>
    
suspend fun fetchAndSaveSleepSessions(
        healthConnectClient: HealthConnectClient, // Added parameter
        startTime: Instant, 
        endTime: Instant
    ): List<SleepSessionEntity>
    
suspend fun fetchAndSaveBloodGlucoseData(
        healthConnectClient: HealthConnectClient, // Added parameter
        startTime: Instant, 
        endTime: Instant
    ): List<BloodGlucoseEntity>

    // --- Data Retrieval for Upload (from Local DB) ---
    suspend fun getUnsyncedStepsRecords(): List<StepsRecordEntity>
    suspend fun getUnsyncedHeartRateSamples(): List<HeartRateSampleEntity>
    suspend fun getUnsyncedSleepSessions(): List<SleepSessionEntity>
    suspend fun getUnsyncedBloodGlucoseRecords(): List<BloodGlucoseEntity>

    // --- Data Sync Status Updates (to Local DB) ---
    suspend fun markStepsRecordsAsSynced(ids: List<Long>): Int
    suspend fun markHeartRateSamplesAsSynced(ids: List<Long>): Int
    suspend fun markSleepSessionsAsSynced(ids: List<Long>): Int
    suspend fun markBloodGlucoseRecordsAsSynced(ids: List<Long>): Int
}
