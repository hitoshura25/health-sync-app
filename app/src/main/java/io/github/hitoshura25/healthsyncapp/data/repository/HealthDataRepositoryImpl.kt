package io.github.hitoshura25.healthsyncapp.data.repository

import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
// Removed: import androidx.health.connect.client.units.BloodGlucose - No longer needed
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BloodGlucoseDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.HeartRateSampleDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.SleepSessionDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.StepsRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BloodGlucoseEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.HeartRateSampleEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.SleepSessionEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.StepsRecordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject 
import javax.inject.Singleton 

@Singleton 
class HealthDataRepositoryImpl @Inject constructor(
    private val stepsRecordDao: StepsRecordDao,
    private val heartRateSampleDao: HeartRateSampleDao,
    private val sleepSessionDao: SleepSessionDao,
    private val bloodGlucoseDao: BloodGlucoseDao
) : HealthDataRepository {

    private val TAG = "HealthDataRepoImpl"

    override suspend fun fetchAllDataTypesFromHealthConnectAndSave(
        healthConnectClient: HealthConnectClient, 
        startTime: Instant, 
        endTime: Instant
    ): Boolean {
        return withContext(Dispatchers.IO) { 
            var overallSuccess = true
            Log.d(TAG, "Starting fetchAllDataTypesFromHealthConnectAndSave for range: $startTime to $endTime")
            try {
                fetchAndSaveStepsData(healthConnectClient, startTime, endTime)
                fetchAndSaveHeartRateData(healthConnectClient, startTime, endTime)
                fetchAndSaveSleepSessions(healthConnectClient, startTime, endTime)
                fetchAndSaveBloodGlucoseData(healthConnectClient, startTime, endTime)
                
                Log.i(TAG, "fetchAllDataTypesFromHealthConnectAndSave completed.")
            } catch (e: Exception) {
                Log.e(TAG, "Critical error during fetchAllDataTypesFromHealthConnectAndSave orchestration: ${e.message}", e)
                overallSuccess = false
            }
            overallSuccess
        }
    }

    override suspend fun fetchAndSaveStepsData(
        healthConnectClient: HealthConnectClient, 
        startTime: Instant, 
        endTime: Instant
    ): List<StepsRecordEntity> {
        val entitiesToSave = mutableListOf<StepsRecordEntity>()
        try {
            val request = ReadRecordsRequest(StepsRecord::class, TimeRangeFilter.between(startTime, endTime))
            val response = healthConnectClient.readRecords(request) 
            val appFetchTime = Instant.now().toEpochMilli()

            for (record in response.records) {
                entitiesToSave.add(
                    StepsRecordEntity(
                        hcUid = record.metadata.id,
                        count = record.count,
                        startTimeEpochMillis = record.startTime.toEpochMilli(),
                        endTimeEpochMillis = record.endTime.toEpochMilli(),
                        zoneOffsetId = record.startZoneOffset?.id,
                        appRecordFetchTimeEpochMillis = appFetchTime,
                        isSynced = false
                    )
                )
            }
            if (entitiesToSave.isNotEmpty()) {
                stepsRecordDao.insertAll(entitiesToSave)
                Log.d(TAG, "Saved ${entitiesToSave.size} steps records to local DB.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching or saving steps data: ${e.message}", e)
        }
        return entitiesToSave
    }

    override suspend fun getUnsyncedStepsRecords(): List<StepsRecordEntity> {
        return stepsRecordDao.getUnsyncedSteps()
    }

    override suspend fun markStepsRecordsAsSynced(ids: List<Long>): Int {
        return stepsRecordDao.markAsSynced(ids)
    }

    override suspend fun fetchAndSaveHeartRateData(
        healthConnectClient: HealthConnectClient, 
        startTime: Instant, 
        endTime: Instant
    ): List<HeartRateSampleEntity> {
        val entitiesToSave = mutableListOf<HeartRateSampleEntity>()
        try {
            val request = ReadRecordsRequest(HeartRateRecord::class, TimeRangeFilter.between(startTime, endTime))
            val response = healthConnectClient.readRecords(request) 
            val appFetchTime = Instant.now().toEpochMilli()

            for (record in response.records) {
                record.samples.forEach { sample ->
                    entitiesToSave.add(
                        HeartRateSampleEntity(
                            hcRecordUid = record.metadata.id, 
                            sampleTimeEpochMillis = sample.time.toEpochMilli(),
                            beatsPerMinute = sample.beatsPerMinute,
                            zoneOffsetId = record.startZoneOffset?.id ?: record.endZoneOffset?.id,
                            appRecordFetchTimeEpochMillis = appFetchTime,
                            isSynced = false
                        )
                    )
                }
            }
            if (entitiesToSave.isNotEmpty()) {
                heartRateSampleDao.insertAll(entitiesToSave)
                Log.d(TAG, "Saved ${entitiesToSave.size} heart rate samples to local DB.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching or saving heart rate data: ${e.message}", e)
        }
        return entitiesToSave
    }

    override suspend fun getUnsyncedHeartRateSamples(): List<HeartRateSampleEntity> {
        return heartRateSampleDao.getUnsyncedSamples()
    }

    override suspend fun markHeartRateSamplesAsSynced(ids: List<Long>): Int {
        return heartRateSampleDao.markAsSynced(ids)
    }

    override suspend fun fetchAndSaveSleepSessions(
        healthConnectClient: HealthConnectClient, 
        startTime: Instant, 
        endTime: Instant
    ): List<SleepSessionEntity> {
        val entitiesToSave = mutableListOf<SleepSessionEntity>()
        try {
            val request = ReadRecordsRequest(SleepSessionRecord::class, TimeRangeFilter.between(startTime, endTime))
            val response = healthConnectClient.readRecords(request) 
            val appFetchTime = Instant.now().toEpochMilli()

            for (record in response.records) {
                entitiesToSave.add(
                    SleepSessionEntity(
                        hcUid = record.metadata.id,
                        title = record.title,
                        notes = record.notes,
                        startTimeEpochMillis = record.startTime.toEpochMilli(),
                        startZoneOffsetId = record.startZoneOffset?.id,
                        endTimeEpochMillis = record.endTime.toEpochMilli(),
                        endZoneOffsetId = record.endZoneOffset?.id,
                        durationMillis = record.endTime.toEpochMilli() - record.startTime.toEpochMilli(),
                        appRecordFetchTimeEpochMillis = appFetchTime,
                        isSynced = false
                    )
                )
            }
            if (entitiesToSave.isNotEmpty()) {
                sleepSessionDao.insertAll(entitiesToSave)
                Log.d(TAG, "Saved ${entitiesToSave.size} sleep sessions to local DB.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching or saving sleep session data: ${e.message}", e)
        }
        return entitiesToSave
    }

    override suspend fun getUnsyncedSleepSessions(): List<SleepSessionEntity> {
        return sleepSessionDao.getUnsyncedSessions()
    }

    override suspend fun markSleepSessionsAsSynced(ids: List<Long>): Int {
        return sleepSessionDao.markAsSynced(ids)
    }

    override suspend fun fetchAndSaveBloodGlucoseData(
        healthConnectClient: HealthConnectClient, 
        startTime: Instant, 
        endTime: Instant
    ): List<BloodGlucoseEntity> {
        val entitiesToSave = mutableListOf<BloodGlucoseEntity>()
        try {
            val request = ReadRecordsRequest(BloodGlucoseRecord::class, TimeRangeFilter.between(startTime, endTime))
            val response = healthConnectClient.readRecords(request) 
            val appFetchTime = Instant.now().toEpochMilli()

            for (record in response.records) {
                entitiesToSave.add(
                    BloodGlucoseEntity(
                        hcUid = record.metadata.id,
                        timeEpochMillis = record.time.toEpochMilli(),
                        zoneOffsetId = record.zoneOffset?.id,
                        levelInMilligramsPerDeciliter = record.level.inMilligramsPerDeciliter, // Renamed field, value is mg/dL
                        // levelUnit field removed from Entity
                        specimenSource = record.specimenSource,
                        mealType = record.mealType,
                        relationToMeal = record.relationToMeal,
                        dataOriginPackageName = record.metadata.dataOrigin.packageName, 
                        hcLastModifiedTimeEpochMillis = record.metadata.lastModifiedTime.toEpochMilli(), 
                        clientRecordId = record.metadata.clientRecordId, 
                        clientRecordVersion = record.metadata.clientRecordVersion, 
                        appRecordFetchTimeEpochMillis = appFetchTime,
                        isSynced = false
                    )
                )
            }
            if (entitiesToSave.isNotEmpty()) {
                bloodGlucoseDao.insertAll(entitiesToSave)
                Log.d(TAG, "Saved ${entitiesToSave.size} blood glucose records to local DB.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching or saving blood glucose data: ${e.message}", e)
        }
        return entitiesToSave
    }

    override suspend fun getUnsyncedBloodGlucoseRecords(): List<BloodGlucoseEntity> {
        return bloodGlucoseDao.getUnsyncedRecords()
    }

    override suspend fun markBloodGlucoseRecordsAsSynced(ids: List<Long>): Int {
        return bloodGlucoseDao.markAsSynced(ids)
    }
}
