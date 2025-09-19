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
import io.github.hitoshura25.healthsyncapp.avro.AvroWeightRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroActiveCaloriesBurnedRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroBasalBodyTemperatureRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroBasalMetabolicRateRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroDistanceRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroElevationGainedRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroExerciseSessionRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroFloorsClimbedRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroHeartRateVariabilityRmssdRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroPowerRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroRestingHeartRateRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroSpeedRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroStepsCadenceRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroTotalCaloriesBurnedRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroVo2MaxRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroBodyFatRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroBodyTemperatureRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroBodyWaterMassRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroBoneMassRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroHeightRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroLeanBodyMassRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroHydrationRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroNutritionRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroBloodPressureRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroOxygenSaturationRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroRespiratoryRateRecord

// Explicit DAO imports
import io.github.hitoshura25.healthsyncapp.data.local.database.AppDatabase // Added
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BloodGlucoseDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.HeartRateSampleDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.SleepSessionDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.SleepStageDao // Added
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.StepsRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.WeightRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.ActiveCaloriesBurnedRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BasalBodyTemperatureRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BasalMetabolicRateRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BloodPressureRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BodyFatRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BodyTemperatureRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BodyWaterMassRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BoneMassRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.DistanceRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.ElevationGainedRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.ExerciseSessionRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.FloorsClimbedRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.HeartRateVariabilityRmssdRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.HeightRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.HydrationRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.LeanBodyMassRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.NutritionRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.OxygenSaturationRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.PowerRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.RespiratoryRateRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.RestingHeartRateRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.SpeedRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.StepsCadenceRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.TotalCaloriesBurnedRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.Vo2MaxRecordDao

// Explicit Mapper imports
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toBloodGlucoseEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toHeartRateSampleEntities
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toSleepSessionEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toSleepStageEntity // Added
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toStepsRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toWeightRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toActiveCaloriesBurnedRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toBasalBodyTemperatureRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toBasalMetabolicRateRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toDistanceRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toElevationGainedRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toExerciseSessionRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toFloorsClimbedRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toHeartRateVariabilityRmssdRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toPowerRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toRestingHeartRateRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toSpeedRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toStepsCadenceRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toTotalCaloriesBurnedRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toVo2MaxRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toBodyFatRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toBodyTemperatureRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toBodyWaterMassRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toBoneMassRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toHeightRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toLeanBodyMassRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toHydrationRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toNutritionRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toBloodPressureRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toOxygenSaturationRecordEntity

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
    private val sleepStageDao: SleepStageDao,
    private val bloodGlucoseDao: BloodGlucoseDao,
    private val weightRecordDao: WeightRecordDao,
    private val activeCaloriesBurnedRecordDao: ActiveCaloriesBurnedRecordDao,
    private val basalBodyTemperatureRecordDao: BasalBodyTemperatureRecordDao,
    private val basalMetabolicRateRecordDao: BasalMetabolicRateRecordDao,
    private val distanceRecordDao: DistanceRecordDao,
    private val elevationGainedRecordDao: ElevationGainedRecordDao,
    private val exerciseSessionRecordDao: ExerciseSessionRecordDao,
    private val floorsClimbedRecordDao: FloorsClimbedRecordDao,
    private val heartRateVariabilityRmssdRecordDao: HeartRateVariabilityRmssdRecordDao,
    private val powerRecordDao: PowerRecordDao,
    private val restingHeartRateRecordDao: RestingHeartRateRecordDao,
    private val speedRecordDao: SpeedRecordDao,
    private val stepsCadenceRecordDao: StepsCadenceRecordDao,
    private val totalCaloriesBurnedRecordDao: TotalCaloriesBurnedRecordDao,
    private val vo2MaxRecordDao: Vo2MaxRecordDao,
    private val bodyFatRecordDao: BodyFatRecordDao,
    private val bodyTemperatureRecordDao: BodyTemperatureRecordDao,
    private val bodyWaterMassRecordDao: BodyWaterMassRecordDao,
    private val boneMassRecordDao: BoneMassRecordDao,
    private val heightRecordDao: HeightRecordDao,
    private val leanBodyMassRecordDao: LeanBodyMassRecordDao,
    private val hydrationRecordDao: HydrationRecordDao,
    private val nutritionRecordDao: NutritionRecordDao,
    private val bloodPressureRecordDao: BloodPressureRecordDao,
    private val oxygenSaturationRecordDao: OxygenSaturationRecordDao,
    private val respiratoryRateRecordDao: RespiratoryRateRecordDao
) {
    private val TAG = "AvroFileProcService" // Added comment to force re-compilation

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
                    file.name.startsWith("WeightRecord_") -> {
                        val avroRecords = Files.newInputStream(file.toPath()).buffered().use {
                            AvroObjectContainer.decodeFromStream<AvroWeightRecord>(it).toList()
                        }
                        if (avroRecords.isNotEmpty()) {
                            val entities = avroRecords.map { it.toWeightRecordEntity() }
                            appDatabase.weightRecordDao().insertAll(entities)
                            Log.i(TAG, "Inserted ${entities.size} WeightRecord entities from ${file.name}")
                        }
                        fileProcessedSuccessfully = true
                        attemptMove = true
                    }
                    file.name.startsWith("ActiveCaloriesBurnedRecord_") -> {
                        val avroRecords = Files.newInputStream(file.toPath()).buffered().use {
                            AvroObjectContainer.decodeFromStream<AvroActiveCaloriesBurnedRecord>(it).toList()
                        }
                        if (avroRecords.isNotEmpty()) {
                            val entities = avroRecords.map { it.toActiveCaloriesBurnedRecordEntity() }
                            activeCaloriesBurnedRecordDao.insertAll(entities)
                            Log.i(TAG, "Inserted ${entities.size} ActiveCaloriesBurnedRecord entities from ${file.name}")
                        }
                        fileProcessedSuccessfully = true
                        attemptMove = true
                    }
                    file.name.startsWith("BasalBodyTemperatureRecord_") -> {
                        val avroRecords = Files.newInputStream(file.toPath()).buffered().use {
                            AvroObjectContainer.decodeFromStream<AvroBasalBodyTemperatureRecord>(it).toList()
                        }
                        if (avroRecords.isNotEmpty()) {
                            val entities = avroRecords.map { it.toBasalBodyTemperatureRecordEntity() }
                            basalBodyTemperatureRecordDao.insertAll(entities)
                            Log.i(TAG, "Inserted ${entities.size} BasalBodyTemperatureRecord entities from ${file.name}")
                        }
                        fileProcessedSuccessfully = true
                        attemptMove = true
                    }
                    file.name.startsWith("RespiratoryRateRecord_") -> {
                        val avroRecords = Files.newInputStream(file.toPath()).buffered().use {
                            AvroObjectContainer.decodeFromStream<AvroRespiratoryRateRecord>(it).toList()
                        }

                        fileProcessedSuccessfully = true
                        attemptMove = true
                    }
                    file.name.startsWith("OxygenSaturationRecord_") -> {
                        val avroRecords = Files.newInputStream(file.toPath()).buffered().use {
                            AvroObjectContainer.decodeFromStream<AvroOxygenSaturationRecord>(it).toList()
                        }
                        if (avroRecords.isNotEmpty()) {
                            val entities = avroRecords.map { it.toOxygenSaturationRecordEntity() }
                            oxygenSaturationRecordDao.insertAll(entities)
                            Log.i(TAG, "Inserted ${entities.size} OxygenSaturationRecord entities from ${file.name}")
                        }
                        fileProcessedSuccessfully = true
                        attemptMove = true
                    }
                    file.name.startsWith("BloodPressureRecord_") -> {
                        val avroRecords = Files.newInputStream(file.toPath()).buffered().use {
                            AvroObjectContainer.decodeFromStream<AvroBloodPressureRecord>(it).toList()
                        }
                        if (avroRecords.isNotEmpty()) {
                            val entities = avroRecords.map { it.toBloodPressureRecordEntity() }
                            bloodPressureRecordDao.insertAll(entities)
                            Log.i(TAG, "Inserted ${entities.size} BloodPressureRecord entities from ${file.name}")
                        }
                        fileProcessedSuccessfully = true
                        attemptMove = true
                    }
                    file.name.startsWith("NutritionRecord_") -> {
                        val avroRecords = Files.newInputStream(file.toPath()).buffered().use {
                            AvroObjectContainer.decodeFromStream<AvroNutritionRecord>(it).toList()
                        }
                        if (avroRecords.isNotEmpty()) {
                            val entities = avroRecords.map { it.toNutritionRecordEntity() }
                            nutritionRecordDao.insertAll(entities)
                            Log.i(TAG, "Inserted ${entities.size} NutritionRecord entities from ${file.name}")
                        }
                        fileProcessedSuccessfully = true
                        attemptMove = true
                    }
                    file.name.startsWith("HydrationRecord_") -> {
                        val avroRecords = Files.newInputStream(file.toPath()).buffered().use {
                            AvroObjectContainer.decodeFromStream<AvroHydrationRecord>(it).toList()
                        }
                        if (avroRecords.isNotEmpty()) {
                            val entities = avroRecords.map { it.toHydrationRecordEntity() }
                            hydrationRecordDao.insertAll(entities)
                            Log.i(TAG, "Inserted ${entities.size} HydrationRecord entities from ${file.name}")
                        }
                        fileProcessedSuccessfully = true
                        attemptMove = true
                    }
                    file.name.startsWith("LeanBodyMassRecord_") -> {
                        val avroRecords = Files.newInputStream(file.toPath()).buffered().use {
                            AvroObjectContainer.decodeFromStream<AvroLeanBodyMassRecord>(it).toList()
                        }
                        if (avroRecords.isNotEmpty()) {
                            val entities = avroRecords.map { it.toLeanBodyMassRecordEntity() }
                            leanBodyMassRecordDao.insertAll(entities)
                            Log.i(TAG, "Inserted ${entities.size} LeanBodyMassRecord entities from ${file.name}")
                        }
                        fileProcessedSuccessfully = true
                        attemptMove = true
                    }
                    file.name.startsWith("HeightRecord_") -> {
                        val avroRecords = Files.newInputStream(file.toPath()).buffered().use {
                            AvroObjectContainer.decodeFromStream<AvroHeightRecord>(it).toList()
                        }
                        if (avroRecords.isNotEmpty()) {
                            val entities = avroRecords.map { it.toHeightRecordEntity() }
                            heightRecordDao.insertAll(entities)
                            Log.i(TAG, "Inserted ${entities.size} HeightRecord entities from ${file.name}")
                        }
                        fileProcessedSuccessfully = true
                        attemptMove = true
                    }
                    file.name.startsWith("BoneMassRecord_") -> {
                        val avroRecords = Files.newInputStream(file.toPath()).buffered().use {
                            AvroObjectContainer.decodeFromStream<AvroBoneMassRecord>(it).toList()
                        }
                        if (avroRecords.isNotEmpty()) {
                            val entities = avroRecords.map { it.toBoneMassRecordEntity() }
                            boneMassRecordDao.insertAll(entities)
                            Log.i(TAG, "Inserted ${entities.size} BoneMassRecord entities from ${file.name}")
                        }
                        fileProcessedSuccessfully = true
                        attemptMove = true
                    }
                    file.name.startsWith("BodyWaterMassRecord_") -> {
                        val avroRecords = Files.newInputStream(file.toPath()).buffered().use {
                            AvroObjectContainer.decodeFromStream<AvroBodyWaterMassRecord>(it).toList()
                        }
                        if (avroRecords.isNotEmpty()) {
                            val entities = avroRecords.map { it.toBodyWaterMassRecordEntity() }
                            bodyWaterMassRecordDao.insertAll(entities)
                            Log.i(TAG, "Inserted ${entities.size} BodyWaterMassRecord entities from ${file.name}")
                        }
                        fileProcessedSuccessfully = true
                        attemptMove = true
                    }
                    file.name.startsWith("BodyTemperatureRecord_") -> {
                        val avroRecords = Files.newInputStream(file.toPath()).buffered().use {
                            AvroObjectContainer.decodeFromStream<AvroBodyTemperatureRecord>(it).toList()
                        }
                        if (avroRecords.isNotEmpty()) {
                            val entities = avroRecords.map { it.toBodyTemperatureRecordEntity() }
                            bodyTemperatureRecordDao.insertAll(entities)
                            Log.i(TAG, "Inserted ${entities.size} BodyTemperatureRecord entities from ${file.name}")
                        }
                        fileProcessedSuccessfully = true
                        attemptMove = true
                    }
                    file.name.startsWith("BodyFatRecord_") -> {
                        val avroRecords = Files.newInputStream(file.toPath()).buffered().use {
                            AvroObjectContainer.decodeFromStream<AvroBodyFatRecord>(it).toList()
                        }
                        if (avroRecords.isNotEmpty()) {
                            val entities = avroRecords.map { it.toBodyFatRecordEntity() }
                            bodyFatRecordDao.insertAll(entities)
                            Log.i(TAG, "Inserted ${entities.size} BodyFatRecord entities from ${file.name}")
                        }
                        fileProcessedSuccessfully = true
                        attemptMove = true
                    }
                    file.name.startsWith("Vo2MaxRecord_") -> {
                        val avroRecords = Files.newInputStream(file.toPath()).buffered().use {
                            AvroObjectContainer.decodeFromStream<AvroVo2MaxRecord>(it).toList()
                        }
                        if (avroRecords.isNotEmpty()) {
                            val entities = avroRecords.map { it.toVo2MaxRecordEntity() }
                            vo2MaxRecordDao.insertAll(entities)
                            Log.i(TAG, "Inserted ${entities.size} Vo2MaxRecord entities from ${file.name}")
                        }
                        fileProcessedSuccessfully = true
                        attemptMove = true
                    }
                    file.name.startsWith("TotalCaloriesBurnedRecord_") -> {
                        val avroRecords = Files.newInputStream(file.toPath()).buffered().use {
                            AvroObjectContainer.decodeFromStream<AvroTotalCaloriesBurnedRecord>(it).toList()
                        }
                        if (avroRecords.isNotEmpty()) {
                            val entities = avroRecords.map { it.toTotalCaloriesBurnedRecordEntity() }
                            totalCaloriesBurnedRecordDao.insertAll(entities)
                            Log.i(TAG, "Inserted ${entities.size} TotalCaloriesBurnedRecord entities from ${file.name}")
                        }
                        fileProcessedSuccessfully = true
                        attemptMove = true
                    }
                    file.name.startsWith("StepsCadenceRecord_") -> {
                        val avroRecords = Files.newInputStream(file.toPath()).buffered().use {
                            AvroObjectContainer.decodeFromStream<AvroStepsCadenceRecord>(it).toList()
                        }
                        if (avroRecords.isNotEmpty()) {
                            var allTransactionsInFileSucceeded = true
                            for (avroRecord in avroRecords) {
                                try {
                                    val (recordEntity, sampleEntities) = avroRecord.toStepsCadenceRecordEntity()
                                    stepsCadenceRecordDao.insertRecordWithSamples(recordEntity, sampleEntities)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Failed to process and insert StepsCadenceRecord ${avroRecord.metadata.id} from file ${file.name}", e)
                                    allTransactionsInFileSucceeded = false
                                    break
                                }
                            }
                            if (allTransactionsInFileSucceeded) {
                                Log.i(TAG, "Successfully processed ${avroRecords.size} StepsCadenceRecord(s) from ${file.name}")
                                fileProcessedSuccessfully = true
                            } else {
                                Log.w(TAG, "One or more StepsCadenceRecord(s) in ${file.name} failed to process fully.")
                                overallSuccess = false
                            }
                        } else {
                            Log.i(TAG, "No StepsCadenceRecord objects found in ${file.name}, file is empty.")
                            fileProcessedSuccessfully = true
                        }
                        attemptMove = true
                    }
                    file.name.startsWith("SpeedRecord_") -> {
                        val avroRecords = Files.newInputStream(file.toPath()).buffered().use {
                            AvroObjectContainer.decodeFromStream<AvroSpeedRecord>(it).toList()
                        }
                        if (avroRecords.isNotEmpty()) {
                            var allTransactionsInFileSucceeded = true
                            for (avroRecord in avroRecords) {
                                try {
                                    val (recordEntity, sampleEntities) = avroRecord.toSpeedRecordEntity()
                                    speedRecordDao.insertRecordWithSamples(recordEntity, sampleEntities)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Failed to process and insert SpeedRecord ${avroRecord.metadata.id} from file ${file.name}", e)
                                    allTransactionsInFileSucceeded = false
                                    break
                                }
                            }
                            if (allTransactionsInFileSucceeded) {
                                Log.i(TAG, "Successfully processed ${avroRecords.size} SpeedRecord(s) from ${file.name}")
                                fileProcessedSuccessfully = true
                            } else {
                                Log.w(TAG, "One or more SpeedRecord(s) in ${file.name} failed to process fully.")
                                overallSuccess = false
                            }
                        } else {
                            Log.i(TAG, "No SpeedRecord objects found in ${file.name}, file is empty.")
                            fileProcessedSuccessfully = true
                        }
                        attemptMove = true
                    }
                    file.name.startsWith("RestingHeartRateRecord_") -> {
                        val avroRecords = Files.newInputStream(file.toPath()).buffered().use {
                            AvroObjectContainer.decodeFromStream<AvroRestingHeartRateRecord>(it).toList()
                        }
                        if (avroRecords.isNotEmpty()) {
                            val entities = avroRecords.map { it.toRestingHeartRateRecordEntity() }
                            restingHeartRateRecordDao.insertAll(entities)
                            Log.i(TAG, "Inserted ${entities.size} RestingHeartRateRecord entities from ${file.name}")
                        }
                        fileProcessedSuccessfully = true
                        attemptMove = true
                    }
                    file.name.startsWith("PowerRecord_") -> {
                        val avroRecords = Files.newInputStream(file.toPath()).buffered().use {
                            AvroObjectContainer.decodeFromStream<AvroPowerRecord>(it).toList()
                        }
                        if (avroRecords.isNotEmpty()) {
                            var allTransactionsInFileSucceeded = true
                            for (avroRecord in avroRecords) {
                                try {
                                    val (recordEntity, sampleEntities) = avroRecord.toPowerRecordEntity()
                                    powerRecordDao.insertRecordWithSamples(recordEntity, sampleEntities)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Failed to process and insert PowerRecord ${avroRecord.metadata.id} from file ${file.name}", e)
                                    allTransactionsInFileSucceeded = false
                                    break
                                }
                            }
                            if (allTransactionsInFileSucceeded) {
                                Log.i(TAG, "Successfully processed ${avroRecords.size} PowerRecord(s) from ${file.name}")
                                fileProcessedSuccessfully = true
                            } else {
                                Log.w(TAG, "One or more PowerRecord(s) in ${file.name} failed to process fully.")
                                overallSuccess = false
                            }
                        } else {
                            Log.i(TAG, "No PowerRecord objects found in ${file.name}, file is empty.")
                            fileProcessedSuccessfully = true
                        }
                        attemptMove = true
                    }
                    file.name.startsWith("HeartRateVariabilityRmssdRecord_") -> {
                        val avroRecords = Files.newInputStream(file.toPath()).buffered().use {
                            AvroObjectContainer.decodeFromStream<AvroHeartRateVariabilityRmssdRecord>(it).toList()
                        }
                        if (avroRecords.isNotEmpty()) {
                            val entities = avroRecords.map { it.toHeartRateVariabilityRmssdRecordEntity() }
                            heartRateVariabilityRmssdRecordDao.insertAll(entities)
                            Log.i(TAG, "Inserted ${entities.size} HeartRateVariabilityRmssdRecord entities from ${file.name}")
                        }
                        fileProcessedSuccessfully = true
                        attemptMove = true
                    }
                    file.name.startsWith("FloorsClimbedRecord_") -> {
                        val avroRecords = Files.newInputStream(file.toPath()).buffered().use {
                            AvroObjectContainer.decodeFromStream<AvroFloorsClimbedRecord>(it).toList()
                        }
                        if (avroRecords.isNotEmpty()) {
                            val entities = avroRecords.map { it.toFloorsClimbedRecordEntity() }
                            floorsClimbedRecordDao.insertAll(entities)
                            Log.i(TAG, "Inserted ${entities.size} FloorsClimbedRecord entities from ${file.name}")
                        }
                        fileProcessedSuccessfully = true
                        attemptMove = true
                    }
                    file.name.startsWith("ExerciseSessionRecord_") -> {
                        val avroRecords = Files.newInputStream(file.toPath()).buffered().use {
                            AvroObjectContainer.decodeFromStream<AvroExerciseSessionRecord>(it).toList()
                        }
                        if (avroRecords.isNotEmpty()) {
                            val entities = avroRecords.map { it.toExerciseSessionRecordEntity() }
                            exerciseSessionRecordDao.insertAll(entities)
                            Log.i(TAG, "Inserted ${entities.size} ExerciseSessionRecord entities from ${file.name}")
                        }
                        fileProcessedSuccessfully = true
                        attemptMove = true
                    }
                    file.name.startsWith("ElevationGainedRecord_") -> {
                        val avroRecords = Files.newInputStream(file.toPath()).buffered().use {
                            AvroObjectContainer.decodeFromStream<AvroElevationGainedRecord>(it).toList()
                        }
                        if (avroRecords.isNotEmpty()) {
                            val entities = avroRecords.map { it.toElevationGainedRecordEntity() }
                            elevationGainedRecordDao.insertAll(entities)
                            Log.i(TAG, "Inserted ${entities.size} ElevationGainedRecord entities from ${file.name}")
                        }
                        fileProcessedSuccessfully = true
                        attemptMove = true
                    }
                    file.name.startsWith("DistanceRecord_") -> {
                        val avroRecords = Files.newInputStream(file.toPath()).buffered().use {
                            AvroObjectContainer.decodeFromStream<AvroDistanceRecord>(it).toList()
                        }
                        if (avroRecords.isNotEmpty()) {
                            val entities = avroRecords.map { it.toDistanceRecordEntity() }
                            distanceRecordDao.insertAll(entities)
                            Log.i(TAG, "Inserted ${entities.size} DistanceRecord entities from ${file.name}")
                        }
                        fileProcessedSuccessfully = true
                        attemptMove = true
                    }
                    file.name.startsWith("BasalMetabolicRateRecord_") -> {
                        val avroRecords = Files.newInputStream(file.toPath()).buffered().use {
                            AvroObjectContainer.decodeFromStream<AvroBasalMetabolicRateRecord>(it).toList()
                        }
                        if (avroRecords.isNotEmpty()) {
                            val entities = avroRecords.map { it.toBasalMetabolicRateRecordEntity() }
                            basalMetabolicRateRecordDao.insertAll(entities)
                            Log.i(TAG, "Inserted ${entities.size} BasalMetabolicRateRecord entities from ${file.name}")
                        }
                        fileProcessedSuccessfully = true
                        attemptMove = true
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
