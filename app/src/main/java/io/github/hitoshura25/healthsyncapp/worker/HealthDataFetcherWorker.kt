package io.github.hitoshura25.healthsyncapp.worker

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.hitoshura25.healthsyncapp.data.HealthConnectConstants.RECORD_TYPES_SUPPORTED
import io.github.hitoshura25.healthsyncapp.file.FileHandler
import io.github.hitoshura25.healthsyncapp.worker.fetcher.RecordFetcherFactory
import java.io.File
import java.time.Instant
import java.time.temporal.ChronoUnit

@HiltWorker
class HealthDataFetcherWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val healthConnectClient: HealthConnectClient,
    private val fileHandler: FileHandler,
    private val recordFetcherFactory: RecordFetcherFactory
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "HealthDataFetcherWorker"
        private const val TAG = "HealthDataFetcherWorker"
    }

    private var anyFilesWrittenSuccessfully = false

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting $WORK_NAME execution.")
        anyFilesWrittenSuccessfully = false

        try {
            val grantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()
            val endTime = Instant.now()
            val startTime = endTime.minus(24, ChronoUnit.HOURS)
            val timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            val timestampForFilesAndMapping = endTime.toEpochMilli()

            var overallProcessingSuccess = true

            RECORD_TYPES_SUPPORTED.forEach { healthRecordType ->
                val readPermission = HealthPermission.getReadPermission(healthRecordType.recordKClass)
                if (grantedPermissions.contains(readPermission)) {
                    try {
                        val fetcher = recordFetcherFactory.create(healthRecordType)
                        val mappedAvroRecords = fetcher.fetchAndMap(timeRangeFilter, timestampForFilesAndMapping)

                        if (mappedAvroRecords.isNotEmpty()) {
                            val outputFileName = "${healthRecordType.recordKClass.simpleName}_${timestampForFilesAndMapping}.avro"
                            val outputFile = File(fileHandler.getStagingDirectory(), outputFileName)
                            val writeSuccess = fileHandler.writeAvroFile(mappedAvroRecords.asSequence(), outputFile)
                            if (writeSuccess) {
                                anyFilesWrittenSuccessfully = true
                                Log.i(TAG, "Successfully wrote Avro file: ${outputFile.path}")
                            } else {
                                Log.e(TAG, "Failed to write Avro file: ${outputFile.path}")
                                overallProcessingSuccess = false
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing ${healthRecordType.recordKClass.simpleName}: ${e.message}", e)
                        overallProcessingSuccess = false
                    }
                } else {
                    Log.w(TAG, "Read permission DENIED for ${healthRecordType.recordKClass.simpleName}. Skipping.")
                }
            }

            if (overallProcessingSuccess) {
                if (anyFilesWrittenSuccessfully) {
                    enqueueAvroFileProcessorWorker()
                }
                return Result.success()
            } else {
                return Result.failure()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Critical error in $WORK_NAME: ${e.message}", e)
            return Result.failure()
        }
    }

    private fun enqueueAvroFileProcessorWorker() {
        val workManager = WorkManager.getInstance(appContext)
        val processRequest = OneTimeWorkRequestBuilder<AvroFileProcessorWorker>().build()
        workManager.enqueueUniqueWork(
            AvroFileProcessorWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            processRequest
        )
        Log.i(TAG, "Enqueued $AvroFileProcessorWorker.WORK_NAME with REPLACE policy.")
    }
}