package io.github.hitoshura25.healthsyncapp.service

import android.util.Log
import androidx.room.withTransaction // Added for Room transactions
import com.github.avrokotlin.avro4k.AvroObjectContainer
import com.github.avrokotlin.avro4k.ExperimentalAvro4kApi
import com.github.avrokotlin.avro4k.decodeFromStream

// Explicit Avro DTO imports
import io.github.hitoshura25.healthsyncapp.avro.AvroBloodGlucoseRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroHeartRateRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroSleepSessionRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroStepsRecord

// Explicit DAO imports
import io.github.hitoshura25.healthsyncapp.data.local.database.AppDatabase // Added
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BloodGlucoseDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.HeartRateSampleDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.SleepSessionDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.SleepStageDao // Added
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.StepsRecordDao

// Explicit Mapper imports
import io.github.hitoshura25.healthsyncapp.data.mapper.toBloodGlucoseEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.toHeartRateSampleEntities
import io.github.hitoshura25.healthsyncapp.data.mapper.toSleepSessionEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.toSleepStageEntity // Added
import io.github.hitoshura25.healthsyncapp.data.mapper.toStepsRecordEntity

import io.github.hitoshura25.healthsyncapp.file.FileHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AvroFileProcessingService @Inject constructor(
    private val fileHandler: FileHandler,
    private val appDatabase: AppDatabase, // Added
    private val stepsRecordDao: StepsRecordDao,
    private val heartRateSampleDao: HeartRateSampleDao,
    private val sleepSessionDao: SleepSessionDao,
    private val sleepStageDao: SleepStageDao, // Added
    private val bloodGlucoseDao: BloodGlucoseDao
) {

    private val TAG = "AvroFileProcService"

    @OptIn(ExperimentalAvro4kApi::class)
    suspend fun processStagedAvroFiles(): Boolean = withContext(Dispatchers.IO) {
        val stagingDir = fileHandler.getStagingDirectory()
        val completedDir = fileHandler.getCompletedDirectory()
        var overallSuccess = true

        Log.d(TAG, "Starting to process files in staging directory: ${stagingDir.absolutePath}")

        val filesToProcess = fileHandler.listFiles(stagingDir)
        if (filesToProcess.isEmpty()) {
            Log.i(TAG, "No files found in staging directory.")
            return@withContext true
        }

        Log.i(TAG, "Found ${filesToProcess.size} files to process.")

        for (file in filesToProcess) {
            if (!file.isFile || !file.name.endsWith(".avro")) {
                Log.w(TAG, "Skipping non-avro file or directory: ${file.name}")
                continue
            }

            Log.d(TAG, "Processing file: ${file.name}")
            var fileProcessedSuccessfully = false
            var attemptMove = false

            try {
                when {
                    file.name.startsWith("StepsRecord_") -> {
                        val avroRecords = Files.newInputStream(file.toPath()).buffered().use {
                            AvroObjectContainer.decodeFromStream<AvroStepsRecord>(it).toList()
                        }
                        if (avroRecords.isNotEmpty()) {
                            val entities = avroRecords.map { it.toStepsRecordEntity() }
                            stepsRecordDao.insertAll(entities)
                            Log.i(TAG, "Inserted ${entities.size} StepsRecord entities from ${file.name}")
                        }
                        fileProcessedSuccessfully = true 
                        attemptMove = true
                    }
                    file.name.startsWith("HeartRateRecord_") -> {
                        val avroHeartRateRecords = Files.newInputStream(file.toPath()).buffered().use {
                            AvroObjectContainer.decodeFromStream<AvroHeartRateRecord>(it).toList()
                        }
                        if (avroHeartRateRecords.isNotEmpty()) {
                            val allSampleEntities = avroHeartRateRecords.flatMap { avroRecord ->
                                avroRecord.toHeartRateSampleEntities()
                            }
                            if (allSampleEntities.isNotEmpty()){
                                heartRateSampleDao.insertAll(allSampleEntities)
                                Log.i(TAG, "Inserted ${allSampleEntities.size} HeartRateSample entities from ${file.name}")
                            } else {
                                Log.i(TAG, "No actual samples found within HeartRateRecord(s) in ${file.name}.")
                            }
                        }
                        fileProcessedSuccessfully = true 
                        attemptMove = true
                    }
                    file.name.startsWith("SleepSessionRecord_") -> {
                        val avroRecords = Files.newInputStream(file.toPath()).buffered().use {
                            AvroObjectContainer.decodeFromStream<AvroSleepSessionRecord>(it).toList()
                        }
                        if (avroRecords.isNotEmpty()) {
                            var allTransactionsInFileSucceeded = true
                            for (avroRecord in avroRecords) {
                                try {
                                    val sessionEntity = avroRecord.toSleepSessionEntity()
                                    val stageEntities = avroRecord.stages.map { stageAvro ->
                                        stageAvro.toSleepStageEntity(sessionHcUidParam = sessionEntity.hcUid)
                                    }

                                    appDatabase.withTransaction {
                                        sleepSessionDao.insertAll(listOf(sessionEntity)) 
                                        if (stageEntities.isNotEmpty()) {
                                            sleepStageDao.insertAll(stageEntities)
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Failed to process and insert session ${avroRecord.metadata.id} from file ${file.name}", e)
                                    allTransactionsInFileSucceeded = false
                                    break 
                                }
                            }
                            if (allTransactionsInFileSucceeded) {
                                Log.i(TAG, "Successfully processed ${avroRecords.size} sleep sessions and their stages from ${file.name}")
                                fileProcessedSuccessfully = true
                            } else {
                                Log.w(TAG, "One or more sleep sessions in ${file.name} failed to process fully.")
                                overallSuccess = false 
                            }
                        } else {
                            Log.i(TAG, "No SleepSessionRecord objects found in ${file.name}, file is empty.")
                            fileProcessedSuccessfully = true
                        }
                        attemptMove = true
                    }
                    file.name.startsWith("BloodGlucoseRecord_") -> {
                        val avroRecords = Files.newInputStream(file.toPath()).buffered().use {
                            AvroObjectContainer.decodeFromStream<AvroBloodGlucoseRecord>(it).toList()
                        }
                        if (avroRecords.isNotEmpty()) {
                            val entities = avroRecords.map { it.toBloodGlucoseEntity() }
                            bloodGlucoseDao.insertAll(entities)
                            Log.i(TAG, "Inserted ${entities.size} BloodGlucose entities from ${file.name}")
                        }
                        fileProcessedSuccessfully = true 
                        attemptMove = true
                    }
                    else -> {
                        Log.w(TAG, "Unknown record type for file: ${file.name}. Will not process or move.")
                        // overallSuccess = false // Removed this line
                    }
                }

                if (attemptMove && fileProcessedSuccessfully) {
                    val destinationFile = File(completedDir, file.name)
                    if (fileHandler.moveFile(file, destinationFile)) {
                        Log.i(TAG, "Successfully moved ${file.name} to completed directory.")
                    } else {
                        Log.e(TAG, "Failed to move ${file.name} to completed directory. It remains in staging.")
                        overallSuccess = false
                    }
                } else if (attemptMove && !fileProcessedSuccessfully) {
                    Log.e(TAG, "File ${file.name} was marked for move but not processed successfully. Not moving.")
                    overallSuccess = false 
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error processing file ${file.name}. It remains in staging.", e)
                overallSuccess = false
            }
        }
        return@withContext overallSuccess
    }
}
