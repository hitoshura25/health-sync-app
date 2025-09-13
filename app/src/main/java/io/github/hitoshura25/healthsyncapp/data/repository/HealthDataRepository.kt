package io.github.hitoshura25.healthsyncapp.data.repository

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

    // --- Data Fetching from Health Connect (and saving to local DB) ---

    /**
     * Fetches steps data from Health Connect for the given time range
     * and saves it to the local database.
     * This function will encapsulate both fetching from HealthConnect and saving to Room.
     * The return type might evolve; for now, let's assume it signals success/failure or returns fetched data.
     * For simplicity, let's assume it will fetch, save, and then return the saved entities.
     */
    suspend fun fetchAndSaveStepsData(startTime: Instant, endTime: Instant): List<StepsRecordEntity>

    suspend fun fetchAndSaveHeartRateData(startTime: Instant, endTime: Instant): List<HeartRateSampleEntity>

    suspend fun fetchAndSaveSleepSessions(startTime: Instant, endTime: Instant): List<SleepSessionEntity>

    suspend fun fetchAndSaveBloodGlucoseData(startTime: Instant, endTime: Instant): List<BloodGlucoseEntity>

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
