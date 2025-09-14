package io.github.hitoshura25.healthsyncapp.worker

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.hitoshura25.healthsyncapp.data.HealthConnectToAvroMapper
import io.github.hitoshura25.healthsyncapp.file.FileHandler
import java.io.File
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.reflect.KClass

class HealthDataFetcherWorker(
    private val appContext: Context, 
    workerParams: WorkerParameters,
    private val healthConnectClient: HealthConnectClient,
    private val mapper: HealthConnectToAvroMapper, 
    private val fileHandler: FileHandler
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "HealthDataFetcherWorker"
        private const val TAG = "HealthDataFetcherWorker"
        private const val AVRO_STAGING_SUBDIR = "avro_staging"

        private val PROCESSED_RECORD_TYPES: List<KClass<out Record>> = listOf(
            StepsRecord::class,
            HeartRateRecord::class,
            SleepSessionRecord::class
        )
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting HealthDataFetcherWorker execution.")

        try {
            val grantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()
            Log.d(TAG, "Granted permissions: $grantedPermissions")

            val endTime = Instant.now()
            val startTime = endTime.minus(24, ChronoUnit.HOURS)
            val timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            val fetchedTimeForFileNameAndMapping = endTime.toEpochMilli()
            Log.d(TAG, "Fetching data for time range: $startTime to $endTime. Using $fetchedTimeForFileNameAndMapping for file names/mapping timestamp.")

            var overallSuccess = true

            PROCESSED_RECORD_TYPES.forEach { recordType ->
                val readPermission = HealthPermission.getReadPermission(recordType)
                if (grantedPermissions.contains(readPermission)) {
                    Log.d(TAG, "Read permission GRANTED for ${recordType.simpleName}")
                    try {
                        val processingSuccess = fetchAndProcessRecords(
                            recordType,
                            timeRangeFilter,
                            fetchedTimeForFileNameAndMapping
                        )
                        if (!processingSuccess) {
                            Log.w(TAG, "Failed to process or write data for ${recordType.simpleName}.")
                            overallSuccess = false 
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing ${recordType.simpleName}: ${e.message}", e)
                        overallSuccess = false 
                    }
                } else {
                    Log.w(TAG, "Read permission DENIED for ${recordType.simpleName}. Skipping.")
                }
            }
            
            return if (overallSuccess) {
                Log.d(TAG, "HealthDataFetcherWorker completed successfully.")
                Result.success()
            } else {
                Log.w(TAG, "HealthDataFetcherWorker completed with some errors or processing failures.")
                Result.failure()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Critical error in HealthDataFetcherWorker: ${e.message}", e)
            return Result.failure()
        }
    }

    private suspend fun <T : Record> fetchAndProcessRecords(
        recordType: KClass<T>,
        timeRangeFilter: TimeRangeFilter,
        timestampForFileNameAndMapping: Long
    ): Boolean {
        val request = ReadRecordsRequest(recordType, timeRangeFilter)
        val response = healthConnectClient.readRecords(request)
        Log.d(TAG, "Fetched ${response.records.size} records for ${recordType.simpleName}")

        if (response.records.isNotEmpty()) {
            // Construct stagingDir from string paths for robustness with File constructor
            val baseFilesDirPath = appContext.filesDir.path // Uses mocked getPath()
            val stagingDir = File(baseFilesDirPath, AVRO_STAGING_SUBDIR)
            
            if (!stagingDir.exists()) {
                if (!stagingDir.mkdirs()) {
                    Log.e(TAG, "Failed to create staging directory: ${stagingDir.path}")
                    return false
                }
                Log.d(TAG, "Created staging directory: ${stagingDir.path}")
            }

            val mappedAvroRecords: List<Any> = when (recordType) {
                StepsRecord::class -> (response.records as List<StepsRecord>).map {
                    mapper.mapStepsRecord(it, timestampForFileNameAndMapping)
                }
                HeartRateRecord::class -> (response.records as List<HeartRateRecord>).map {
                    mapper.mapHeartRateRecord(it, timestampForFileNameAndMapping)
                }
                SleepSessionRecord::class -> (response.records as List<SleepSessionRecord>).map {
                    mapper.mapSleepSessionRecord(it, timestampForFileNameAndMapping)
                }
                else -> {
                    Log.w(TAG, "Unsupported record type for mapping: ${recordType.simpleName}")
                    return false
                }
            }

            Log.d(TAG, "For ${recordType.simpleName}: HC records count = ${response.records.size}, Mapped Avro records count = ${mappedAvroRecords.size}")

            if (mappedAvroRecords.isEmpty() && response.records.isNotEmpty()) {
                 Log.w(TAG, "Mapping resulted in empty list for ${recordType.simpleName}, though HC records were present. This indicates an issue with the when-clause or casting for mapping.")
                 return false
            }

            val outputFileName = "${recordType.simpleName}_${timestampForFileNameAndMapping}.avro"
            val outputFile = File(stagingDir, outputFileName)
            Log.d(TAG, "Attempting to write ${mappedAvroRecords.size} Avro records to: ${outputFile.path}")

            val writeSuccess = fileHandler.writeAvroFile(mappedAvroRecords.asSequence(), outputFile)
            if (!writeSuccess) {
                Log.e(TAG, "Failed to write Avro file: ${outputFile.path}")
                return false
            }
            Log.i(TAG, "Successfully wrote Avro file: ${outputFile.path}")
            return true
        } else {
            Log.d(TAG, "No new records to process for ${recordType.simpleName}.")
            return true 
        }
    }
}
