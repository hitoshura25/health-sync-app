package io.github.hitoshura25.healthsyncapp.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.hitoshura25.healthsyncapp.data.HealthConnectToAvroMapper
import io.github.hitoshura25.healthsyncapp.file.FileHandler
import java.io.File
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.reflect.KClass

@HiltWorker
class HealthDataFetcherWorker @AssistedInject constructor(
    @Assisted private val appContext: Context, 
    @Assisted workerParams: WorkerParameters,
    private val healthConnectClient: HealthConnectClient,
    private val mapper: HealthConnectToAvroMapper, 
    private val fileHandler: FileHandler
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "HealthDataFetcherWorker"
        private const val TAG = "HealthDataFetcherWorker"
        // AVRO_STAGING_SUBDIR is not directly used here anymore, FileHandler handles paths

        private val PROCESSED_RECORD_TYPES: List<KClass<out Record>> = listOf(
            StepsRecord::class,
            HeartRateRecord::class,
            SleepSessionRecord::class,
            BloodGlucoseRecord::class
        )
    }

    private var anyFilesWrittenSuccessfully = false

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting $WORK_NAME execution.")
        anyFilesWrittenSuccessfully = false // Reset for this run

        try {
            val grantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()
            Log.d(TAG, "Granted permissions: $grantedPermissions")

            val endTime = Instant.now()
            // Fetch data for the last 24 hours up to the current moment.
            val startTime = endTime.minus(24, ChronoUnit.HOURS) 
            val timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            // Use a consistent timestamp for all files generated in this worker run.
            val timestampForFilesAndMapping = endTime.toEpochMilli()
            Log.d(TAG, "Fetching data for time range: $startTime to $endTime. Using $timestampForFilesAndMapping for file names/mapping timestamp.")

            var overallProcessingSuccess = true

            PROCESSED_RECORD_TYPES.forEach { recordType ->
                val readPermission = HealthPermission.getReadPermission(recordType)
                if (grantedPermissions.contains(readPermission)) {
                    Log.d(TAG, "Read permission GRANTED for ${recordType.simpleName}")
                    try {
                        val recordTypeProcessingSuccess = fetchAndMapRecords(
                            recordType,
                            timeRangeFilter,
                            timestampForFilesAndMapping
                        )
                        if (!recordTypeProcessingSuccess) {
                            Log.w(TAG, "Failed to process or write data for ${recordType.simpleName}.")
                            overallProcessingSuccess = false 
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing ${recordType.simpleName}: ${e.message}", e)
                        overallProcessingSuccess = false 
                    }
                } else {
                    Log.w(TAG, "Read permission DENIED for ${recordType.simpleName}. Skipping.")
                    // Denied permission for a type is not a worker failure, just skip that type.
                }
            }
            
            if (overallProcessingSuccess) {
                Log.i(TAG, "$WORK_NAME data fetching and mapping completed successfully.")
                if (anyFilesWrittenSuccessfully) {
                    Log.i(TAG, "New Avro files were written, enqueuing AvroFileProcessorWorker.")
                    enqueueAvroFileProcessorWorker()
                } else {
                    Log.i(TAG, "No new Avro files were written, AvroFileProcessorWorker will not be enqueued.")
                }
                return Result.success()
            } else {
                Log.w(TAG, "$WORK_NAME completed with some errors or processing failures.")
                return Result.failure()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Critical error in $WORK_NAME: ${e.message}", e)
            return Result.failure()
        }
    }

    private suspend fun <T : Record> fetchAndMapRecords(
        recordType: KClass<T>,
        timeRangeFilter: TimeRangeFilter,
        timestampForFileNameAndMapping: Long
    ): Boolean {
        val request = ReadRecordsRequest(recordType, timeRangeFilter)
        val response = healthConnectClient.readRecords(request)
        Log.d(TAG, "Fetched ${response.records.size} records for ${recordType.simpleName}")

        if (response.records.isNotEmpty()) {
            val stagingDir = fileHandler.getStagingDirectory()
            
            if (!stagingDir.exists()) {
                if (!stagingDir.mkdirs()) {
                    Log.e(TAG, "Failed to create staging directory: ${stagingDir.path}.")
                    return false // Error for this record type
                }
                Log.d(TAG, "Created staging directory via worker: ${stagingDir.path}")
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
                BloodGlucoseRecord::class -> (response.records as List<BloodGlucoseRecord>).map {
                    mapper.mapBloodGlucoseRecord(it, timestampForFileNameAndMapping)
                }
                else -> {
                    Log.w(TAG, "Unsupported record type for mapping: ${recordType.simpleName}")
                    return false // Error for this record type
                }
            }

            Log.d(TAG, "For ${recordType.simpleName}: HC records count = ${response.records.size}, Mapped Avro records count = ${mappedAvroRecords.size}")

            if (mappedAvroRecords.isEmpty() && response.records.isNotEmpty()) {
                 Log.w(TAG, "Mapping resulted in empty list for ${recordType.simpleName}, though HC records were present. This might indicate an issue.")
                 // Depending on requirements, this could be 'false' (an error for this type) or 'true' (not an error, just no output).
                 // For now, let's treat it as not an error for the overall processing of this type, but no file will be written.
                 return true 
            }
            
            if (mappedAvroRecords.isEmpty()) {
                Log.d(TAG, "No Avro records to write for ${recordType.simpleName} after mapping.")
                return true // Not an error, just nothing to write for this type
            }

            val outputFileName = "${recordType.simpleName}_${timestampForFileNameAndMapping}.avro"
            val outputFile = File(stagingDir, outputFileName)
            Log.d(TAG, "Attempting to write ${mappedAvroRecords.size} Avro records to: ${outputFile.path}")

            val writeSuccess = fileHandler.writeAvroFile(mappedAvroRecords.asSequence(), outputFile)
            if (!writeSuccess) {
                Log.e(TAG, "Failed to write Avro file: ${outputFile.path}")
                return false // Error for this record type
            }
            Log.i(TAG, "Successfully wrote Avro file: ${outputFile.path}")
            this.anyFilesWrittenSuccessfully = true // Set the flag here
            return true // Successfully processed and wrote this record type
        } else {
            Log.d(TAG, "No new records to process for ${recordType.simpleName}.")
            return true // Not an error, just no data for this type
        }
    }

    private fun enqueueAvroFileProcessorWorker() {
        val workManager = WorkManager.getInstance(appContext)
        val processRequest = OneTimeWorkRequestBuilder<AvroFileProcessorWorker>()
            // No specific constraints needed here, assuming it can run if triggered
            .build()

        workManager.enqueueUniqueWork(
            AvroFileProcessorWorker.WORK_NAME, // Unique name for the processor worker
            ExistingWorkPolicy.REPLACE, // If one is pending, replace it; if running, let it finish, then new one runs
            processRequest
        )
        Log.i(TAG, "Enqueued $AvroFileProcessorWorker.WORK_NAME with REPLACE policy.")
    }
}
