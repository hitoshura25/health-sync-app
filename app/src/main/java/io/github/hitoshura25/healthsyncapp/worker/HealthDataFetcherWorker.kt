package io.github.hitoshura25.healthsyncapp.worker


import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.BasalBodyTemperatureRecord
import androidx.health.connect.client.records.BasalMetabolicRateRecord
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.BodyTemperatureRecord
import androidx.health.connect.client.records.BodyWaterMassRecord
import androidx.health.connect.client.records.BoneMassRecord
import androidx.health.connect.client.records.CyclingPedalingCadenceRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ElevationGainedRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.FloorsClimbedRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.HeightRecord
import androidx.health.connect.client.records.HydrationRecord
import androidx.health.connect.client.records.LeanBodyMassRecord
import androidx.health.connect.client.records.NutritionRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.PowerRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.RespiratoryRateRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.SpeedRecord
import androidx.health.connect.client.records.StepsCadenceRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.Vo2MaxRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.ReadRecordsRequest
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
import io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro.mapActiveCaloriesBurnedRecord
import io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro.mapBasalBodyTemperatureRecord
import io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro.mapBasalMetabolicRateRecord
import io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro.mapBloodGlucoseRecord
import io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro.mapBloodPressureRecord
import io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro.mapBodyFatRecord
import io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro.mapBodyTemperatureRecord
import io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro.mapBodyWaterMassRecord
import io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro.mapBoneMassRecord
import io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro.mapCyclingPedalingCadenceRecord
import io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro.mapDistanceRecord
import io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro.mapElevationGainedRecord
import io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro.mapExerciseSessionRecord
import io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro.mapFloorsClimbedRecord
import io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro.mapHeartRateRecord
import io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro.mapHeartRateVariabilityRmssdRecord
import io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro.mapHeightRecord
import io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro.mapHydrationRecord
import io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro.mapLeanBodyMassRecord
import io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro.mapNutritionRecord
import io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro.mapOxygenSaturationRecord
import io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro.mapPowerRecord
import io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro.mapRespiratoryRateRecord
import io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro.mapRestingHeartRateRecord
import io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro.mapSleepSessionRecord
import io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro.mapSpeedRecord
import io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro.mapStepsCadenceRecord
import io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro.mapStepsRecord
import io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro.mapTotalCaloriesBurnedRecord
import io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro.mapVo2MaxRecord
import io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro.mapWeightRecord
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
    private val fileHandler: FileHandler,
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "HealthDataFetcherWorker"
        private const val TAG = "HealthDataFetcherWorker"
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

            RECORD_TYPES_SUPPORTED.forEach { recordType ->
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
                    mapStepsRecord(it, timestampForFileNameAndMapping)
                }
                HeartRateRecord::class -> (response.records as List<HeartRateRecord>).map {
                    mapHeartRateRecord(it, timestampForFileNameAndMapping)
                }
                SleepSessionRecord::class -> (response.records as List<SleepSessionRecord>).map {
                    mapSleepSessionRecord(it, timestampForFileNameAndMapping)
                }
                BloodGlucoseRecord::class -> (response.records as List<BloodGlucoseRecord>).map {
                    mapBloodGlucoseRecord(it, timestampForFileNameAndMapping)
                }
                WeightRecord::class -> (response.records as List<WeightRecord>).map {
                    mapWeightRecord(it, timestampForFileNameAndMapping)
                }
                ActiveCaloriesBurnedRecord::class -> (response.records as List<ActiveCaloriesBurnedRecord>).map {
                    mapActiveCaloriesBurnedRecord(it, timestampForFileNameAndMapping)
                }
                BasalBodyTemperatureRecord::class -> (response.records as List<BasalBodyTemperatureRecord>).map {
                    mapBasalBodyTemperatureRecord(it, timestampForFileNameAndMapping)
                }
                BasalMetabolicRateRecord::class -> (response.records as List<BasalMetabolicRateRecord>).map {
                    mapBasalMetabolicRateRecord(it, timestampForFileNameAndMapping)
                }
                CyclingPedalingCadenceRecord::class -> (response.records as List<CyclingPedalingCadenceRecord>).map {
                    mapCyclingPedalingCadenceRecord(it, timestampForFileNameAndMapping)
                }
                DistanceRecord::class -> (response.records as List<DistanceRecord>).map {
                    mapDistanceRecord(it, timestampForFileNameAndMapping)
                }
                ElevationGainedRecord::class -> (response.records as List<ElevationGainedRecord>).map {
                    mapElevationGainedRecord(it, timestampForFileNameAndMapping)
                }
                ExerciseSessionRecord::class -> (response.records as List<ExerciseSessionRecord>).map {
                    mapExerciseSessionRecord(it, timestampForFileNameAndMapping)
                }
                FloorsClimbedRecord::class -> (response.records as List<FloorsClimbedRecord>).map {
                    mapFloorsClimbedRecord(it, timestampForFileNameAndMapping)
                }
                HeartRateVariabilityRmssdRecord::class -> (response.records as List<HeartRateVariabilityRmssdRecord>).map {
                    mapHeartRateVariabilityRmssdRecord(it, timestampForFileNameAndMapping)
                }
                PowerRecord::class -> (response.records as List<PowerRecord>).map {
                    mapPowerRecord(it, timestampForFileNameAndMapping)
                }
                RestingHeartRateRecord::class -> (response.records as List<RestingHeartRateRecord>).map {
                    mapRestingHeartRateRecord(it, timestampForFileNameAndMapping)
                }
                SpeedRecord::class -> (response.records as List<SpeedRecord>).map {
                    mapSpeedRecord(it, timestampForFileNameAndMapping)
                }
                StepsCadenceRecord::class -> (response.records as List<StepsCadenceRecord>).map {
                    mapStepsCadenceRecord(it, timestampForFileNameAndMapping)
                }
                TotalCaloriesBurnedRecord::class -> (response.records as List<TotalCaloriesBurnedRecord>).map {
                    mapTotalCaloriesBurnedRecord(it, timestampForFileNameAndMapping)
                }
                Vo2MaxRecord::class -> (response.records as List<Vo2MaxRecord>).map {
                    mapVo2MaxRecord(it, timestampForFileNameAndMapping)
                }
                BodyFatRecord::class -> (response.records as List<BodyFatRecord>).map {
                    mapBodyFatRecord(it, timestampForFileNameAndMapping)
                }
                BodyTemperatureRecord::class -> (response.records as List<BodyTemperatureRecord>).map {
                    mapBodyTemperatureRecord(it, timestampForFileNameAndMapping)
                }
                BodyWaterMassRecord::class -> (response.records as List<BodyWaterMassRecord>).map {
                    mapBodyWaterMassRecord(it, timestampForFileNameAndMapping)
                }
                BoneMassRecord::class -> (response.records as List<BoneMassRecord>).map {
                    mapBoneMassRecord(it, timestampForFileNameAndMapping)
                }
                HeightRecord::class -> (response.records as List<HeightRecord>).map {
                    mapHeightRecord(it, timestampForFileNameAndMapping)
                }
                LeanBodyMassRecord::class -> (response.records as List<LeanBodyMassRecord>).map {
                    mapLeanBodyMassRecord(it, timestampForFileNameAndMapping)
                }
                HydrationRecord::class -> (response.records as List<HydrationRecord>).map {
                    mapHydrationRecord(it, timestampForFileNameAndMapping)
                }
                NutritionRecord::class -> (response.records as List<NutritionRecord>).map {
                    mapNutritionRecord(it, timestampForFileNameAndMapping)
                }
                BloodPressureRecord::class -> (response.records as List<BloodPressureRecord>).map {
                    mapBloodPressureRecord(it, timestampForFileNameAndMapping)
                }
                OxygenSaturationRecord::class -> (response.records as List<OxygenSaturationRecord>).map {
                    mapOxygenSaturationRecord(it, timestampForFileNameAndMapping)
                }
                RespiratoryRateRecord::class -> (response.records as List<RespiratoryRateRecord>).map {
                    mapRespiratoryRateRecord(it, timestampForFileNameAndMapping)
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