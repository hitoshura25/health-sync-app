package io.github.hitoshura25.healthsyncapp.service

import android.util.Log
import androidx.room.withTransaction
import com.github.avrokotlin.avro4k.AvroObjectContainer
import com.github.avrokotlin.avro4k.ExperimentalAvro4kApi
import com.github.avrokotlin.avro4k.decodeFromStream
import io.github.hitoshura25.healthsyncapp.avro.AvroActiveCaloriesBurnedRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroBasalBodyTemperatureRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroBasalMetabolicRateRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroBloodGlucoseRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroBloodPressureRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroBodyFatRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroBodyTemperatureRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroBodyWaterMassRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroBoneMassRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroCyclingPedalingCadenceRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroDistanceRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroElevationGainedRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroExerciseSessionRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroFloorsClimbedRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroHeartRateVariabilityRmssdRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroHeightRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroHydrationRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroLeanBodyMassRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroNutritionRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroOxygenSaturationRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroPowerRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroRespiratoryRateRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroRestingHeartRateRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroSleepSessionRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroStepsRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroTotalCaloriesBurnedRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroVo2MaxRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroWeightRecord
import io.github.hitoshura25.healthsyncapp.data.HealthConnectConstants.RECORD_TYPES_SUPPORTED
import io.github.hitoshura25.healthsyncapp.data.SupportedHealthRecordType
import io.github.hitoshura25.healthsyncapp.data.local.database.AppDatabase
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.ActiveCaloriesBurnedRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BasalBodyTemperatureRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BasalMetabolicRateRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BloodGlucoseDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BloodPressureRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BodyFatRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BodyTemperatureRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BodyWaterMassRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BoneMassRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.CyclingPedalingCadenceRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.DistanceRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.ElevationGainedRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.ExerciseSessionRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.FloorsClimbedRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.HeartRateSampleDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.HeartRateVariabilityRmssdRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.HeightRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.HydrationRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.LeanBodyMassRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.NutritionRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.OxygenSaturationRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.PowerRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.RespiratoryRateRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.RestingHeartRateRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.SleepSessionDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.SleepStageDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.SpeedRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.StepsCadenceRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.StepsRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.TotalCaloriesBurnedRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.Vo2MaxRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.WeightRecordDao
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toActiveCaloriesBurnedRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toBasalBodyTemperatureRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toBasalMetabolicRateRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toBloodGlucoseEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toBloodPressureRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toBodyFatRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toBodyTemperatureRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toBodyWaterMassRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toBoneMassRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toCyclingPedalingCadenceRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toDistanceRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toElevationGainedRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toExerciseSessionRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toFloorsClimbedRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toHeartRateSampleEntities
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toHeartRateVariabilityRmssdRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toHeightRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toHydrationRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toLeanBodyMassRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toNutritionRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toOxygenSaturationRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toPowerRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toRespiratoryRateRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toRestingHeartRateRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toSleepSessionEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toSleepStageEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toSpeedRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toStepsCadenceRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toStepsRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toTotalCaloriesBurnedRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toVo2MaxRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toWeightRecordEntity
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
    private val appDatabase: AppDatabase,
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
    private val respiratoryRateRecordDao: RespiratoryRateRecordDao,
    private val cyclingPedalingCadenceRecordDao: CyclingPedalingCadenceRecordDao
) {
    private val TAG = "AvroFileProcService"

    @OptIn(ExperimentalAvro4kApi::class)
    suspend fun processStagedAvroFiles(): Boolean = withContext(Dispatchers.IO) {
        val stagingDir = fileHandler.getStagingDirectory()
        val completedDir = fileHandler.getCompletedDirectory()
        var overallSuccess = true

        Log.d(TAG, "Starting to process files in staging directory: ${stagingDir.absolutePath}")

        val allStagedFiles = fileHandler.listFiles(stagingDir).filter { it.isFile && it.name.endsWith(".avro") }
        if (allStagedFiles.isEmpty()) {
            Log.i(TAG, "No Avro files found in staging directory.")
            return@withContext true
        }

        Log.i(TAG, "Found ${allStagedFiles.size} Avro files to potentially process.")

        // Iterate over supported record types to ensure all are handled
        RECORD_TYPES_SUPPORTED.forEach { supportedType ->
            val recordTypeName = supportedType.recordKClass.simpleName
            val filesForThisType = allStagedFiles.filter { it.name.startsWith("${recordTypeName}_") }

            if (filesForThisType.isEmpty()) {
                Log.d(TAG, "No Avro files found for ${recordTypeName}. Skipping.")
                return@forEach // Continue to the next supportedType
            }

            Log.i(TAG, "Processing ${filesForThisType.size} files for ${recordTypeName}.")

            for (file in filesForThisType) {
                Log.d(TAG, "Processing file: ${file.name}")
                var fileProcessedSuccessfully = false

                try {
                    fileProcessedSuccessfully = processAvroFileForType(file, supportedType)

                    if (fileProcessedSuccessfully) {
                        val destinationFile = File(completedDir, file.name)
                        if (fileHandler.moveFile(file, destinationFile)) {
                            Log.i(TAG, "Successfully moved ${file.name} to completed directory.")
                        } else {
                            Log.e(TAG, "Failed to move ${file.name} to completed directory. It remains in staging.")
                            overallSuccess = false
                        }
                    } else {
                        Log.e(TAG, "File ${file.name} was not processed successfully. Not moving.")
                        overallSuccess = false
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Error processing file ${file.name}. It remains in staging.", e)
                    overallSuccess = false
                }
            }
        }
        return@withContext overallSuccess
    }

    @OptIn(ExperimentalAvro4kApi::class)
    private suspend fun processAvroFileForType(file: File, supportedType: SupportedHealthRecordType<*>): Boolean {
        var processedSuccessfully = false
        try {
            processedSuccessfully = when (supportedType) {
                SupportedHealthRecordType.Steps ->
                    processAndInsertSingleRecordType(file,  AvroStepsRecord::toStepsRecordEntity, stepsRecordDao::insertAll, "StepsRecord")
                SupportedHealthRecordType.HeartRate ->
                    processHeartRateRecords(file)
                SupportedHealthRecordType.SleepSession ->
                    processSleepSessionRecords(file)
                SupportedHealthRecordType.BloodGlucose ->
                    processAndInsertSingleRecordType(file,  AvroBloodGlucoseRecord::toBloodGlucoseEntity, bloodGlucoseDao::insertAll, "BloodGlucoseRecord")
                SupportedHealthRecordType.Weight ->
                    processAndInsertSingleRecordType(file,  AvroWeightRecord::toWeightRecordEntity, weightRecordDao::insertAll, "WeightRecord")
                SupportedHealthRecordType.ActiveCaloriesBurned ->
                    processAndInsertSingleRecordType(file,  AvroActiveCaloriesBurnedRecord::toActiveCaloriesBurnedRecordEntity, activeCaloriesBurnedRecordDao::insertAll, "ActiveCaloriesBurnedRecord")
                SupportedHealthRecordType.BasalBodyTemperature ->
                    processAndInsertSingleRecordType(file,  AvroBasalBodyTemperatureRecord::toBasalBodyTemperatureRecordEntity, basalBodyTemperatureRecordDao::insertAll, "BasalBodyTemperatureRecord")
                SupportedHealthRecordType.RespiratoryRate ->
                    processAndInsertSingleRecordType(file,  AvroRespiratoryRateRecord::toRespiratoryRateRecordEntity, respiratoryRateRecordDao::insertAll, "RespiratoryRateRecord")
                SupportedHealthRecordType.OxygenSaturation ->
                    processAndInsertSingleRecordType(file,  AvroOxygenSaturationRecord::toOxygenSaturationRecordEntity, oxygenSaturationRecordDao::insertAll, "OxygenSaturationRecord")
                SupportedHealthRecordType.BloodPressure ->
                    processAndInsertSingleRecordType(file,  AvroBloodPressureRecord::toBloodPressureRecordEntity, bloodPressureRecordDao::insertAll, "BloodPressureRecord")
                SupportedHealthRecordType.Nutrition ->
                    processAndInsertSingleRecordType(file, AvroNutritionRecord::toNutritionRecordEntity, nutritionRecordDao::insertAll, "NutritionRecord")
                SupportedHealthRecordType.Hydration ->
                    processAndInsertSingleRecordType(file,  AvroHydrationRecord::toHydrationRecordEntity, hydrationRecordDao::insertAll, "HydrationRecord")
                SupportedHealthRecordType.LeanBodyMass ->
                    processAndInsertSingleRecordType(file,  AvroLeanBodyMassRecord::toLeanBodyMassRecordEntity, leanBodyMassRecordDao::insertAll, "LeanBodyMassRecord")
                SupportedHealthRecordType.Height ->
                    processAndInsertSingleRecordType(file,  AvroHeightRecord::toHeightRecordEntity, heightRecordDao::insertAll, "HeightRecord")
                SupportedHealthRecordType.BoneMass ->
                    processAndInsertSingleRecordType(file,  AvroBoneMassRecord::toBoneMassRecordEntity, boneMassRecordDao::insertAll, "BoneMassRecord")
                SupportedHealthRecordType.BodyWaterMass ->
                    processAndInsertSingleRecordType(file,  AvroBodyWaterMassRecord::toBodyWaterMassRecordEntity, bodyWaterMassRecordDao::insertAll, "BodyWaterMassRecord")
                SupportedHealthRecordType.BodyTemperature ->
                    processAndInsertSingleRecordType(file,  AvroBodyTemperatureRecord::toBodyTemperatureRecordEntity, bodyTemperatureRecordDao::insertAll, "BodyTemperatureRecord")
                SupportedHealthRecordType.BodyFat ->
                    processAndInsertSingleRecordType(file,  AvroBodyFatRecord::toBodyFatRecordEntity, bodyFatRecordDao::insertAll, "BodyFatRecord")
                SupportedHealthRecordType.Vo2Max ->
                    processAndInsertSingleRecordType(file,  AvroVo2MaxRecord::toVo2MaxRecordEntity, vo2MaxRecordDao::insertAll, "Vo2MaxRecord")
                SupportedHealthRecordType.TotalCaloriesBurned ->
                    processAndInsertSingleRecordType(file, AvroTotalCaloriesBurnedRecord::toTotalCaloriesBurnedRecordEntity, totalCaloriesBurnedRecordDao::insertAll, "TotalCaloriesBurnedRecord")
                SupportedHealthRecordType.StepsCadence ->
                    processStepsCadenceRecords(file)
                SupportedHealthRecordType.Speed ->
                    processSpeedRecords(file)
                SupportedHealthRecordType.RestingHeartRate ->
                    processAndInsertSingleRecordType(file,  AvroRestingHeartRateRecord::toRestingHeartRateRecordEntity, restingHeartRateRecordDao::insertAll, "RestingHeartRateRecord")
                SupportedHealthRecordType.Power ->
                    processPowerRecords(file)
                SupportedHealthRecordType.HeartRateVariabilityRmssd ->
                    processAndInsertSingleRecordType(file,  AvroHeartRateVariabilityRmssdRecord::toHeartRateVariabilityRmssdRecordEntity, heartRateVariabilityRmssdRecordDao::insertAll, "HeartRateVariabilityRmssdRecord")
                SupportedHealthRecordType.FloorsClimbed ->
                    processAndInsertSingleRecordType(file, AvroFloorsClimbedRecord::toFloorsClimbedRecordEntity, floorsClimbedRecordDao::insertAll, "FloorsClimbedRecord")
                SupportedHealthRecordType.ExerciseSession ->
                    processAndInsertSingleRecordType(file,  AvroExerciseSessionRecord::toExerciseSessionRecordEntity, exerciseSessionRecordDao::insertAll, "ExerciseSessionRecord")
                SupportedHealthRecordType.ElevationGained ->
                    processAndInsertSingleRecordType(file, AvroElevationGainedRecord::toElevationGainedRecordEntity, elevationGainedRecordDao::insertAll, "ElevationGainedRecord")
                SupportedHealthRecordType.Distance ->
                    processAndInsertSingleRecordType(file,  AvroDistanceRecord::toDistanceRecordEntity, distanceRecordDao::insertAll, "DistanceRecord")
                SupportedHealthRecordType.BasalMetabolicRate ->
                    processAndInsertSingleRecordType(file,  AvroBasalMetabolicRateRecord::toBasalMetabolicRateRecordEntity, basalMetabolicRateRecordDao::insertAll, "BasalMetabolicRateRecord")
                SupportedHealthRecordType.CyclingPedalingCadence ->
                    processCyclingPedalingCadenceRecords(file)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing Avro file ${file.name} for type ${supportedType.recordKClass.simpleName}", e)
            processedSuccessfully = false
        }
        return processedSuccessfully
    }

    @OptIn(ExperimentalAvro4kApi::class)
    private suspend inline fun <reified AvroType : Any, EntityType : Any> processAndInsertSingleRecordType(
        file: File,
        toEntityMapper: (AvroType) -> EntityType,
        daoInsertFunction: suspend (List<EntityType>) -> Unit,
        recordTypeName: String
    ): Boolean {
        var processedSuccessfully = false
        try {
            val avroRecords = Files.newInputStream(file.toPath()).buffered().use {
                AvroObjectContainer.decodeFromStream<AvroType>(it).toList()
            }
            if (avroRecords.isNotEmpty()) {
                val entities = avroRecords.map { toEntityMapper(it) }
                daoInsertFunction(entities)
                Log.i(TAG, "Inserted ${entities.size} $recordTypeName entities from ${file.name}")
            } else {
                Log.i(TAG, "No $recordTypeName objects found in ${file.name}, file is empty.")
            }
            processedSuccessfully = true
        } catch (e: Exception) {
            Log.e(TAG, "Error processing Avro file ${file.name} for type $recordTypeName", e)
            processedSuccessfully = false
        }
        return processedSuccessfully
    }

    @OptIn(ExperimentalAvro4kApi::class)
    private suspend fun processSleepSessionRecords(file: File): Boolean {
        var processedSuccessfully = false
        try {
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
                    processedSuccessfully = true
                } else {
                    Log.w(TAG, "One or more sleep sessions in ${file.name} failed to process fully.")
                }
            } else {
                Log.i(TAG, "No SleepSessionRecord objects found in ${file.name}, file is empty.")
                processedSuccessfully = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing Avro file ${file.name} for type SleepSession", e)
            processedSuccessfully = false
        }
        return processedSuccessfully
    }

    @OptIn(ExperimentalAvro4kApi::class)
    private suspend fun processHeartRateRecords(file: File): Boolean {
        var processedSuccessfully = false
        try {
            val avroHeartRateRecords = Files.newInputStream(file.toPath()).buffered().use {
                AvroObjectContainer.decodeFromStream<io.github.hitoshura25.healthsyncapp.avro.AvroHeartRateRecord>(it).toList()
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
            } else {
                Log.i(TAG, "No HeartRateRecord objects found in ${file.name}, file is empty.")
            }
            processedSuccessfully = true
        } catch (e: Exception) {
            Log.e(TAG, "Error processing Avro file ${file.name} for type HeartRate", e)
            processedSuccessfully = false
        }
        return processedSuccessfully
    }

    @OptIn(ExperimentalAvro4kApi::class)
    private suspend fun processStepsCadenceRecords(file: File): Boolean {
        var processedSuccessfully = false
        try {
            val avroRecords = Files.newInputStream(file.toPath()).buffered().use {
                AvroObjectContainer.decodeFromStream<io.github.hitoshura25.healthsyncapp.avro.AvroStepsCadenceRecord>(it).toList()
            }
            if (avroRecords.isNotEmpty()) {
                var allTransactionsInFileSucceeded = true
                for (avroRecord in avroRecords) {
                    try {
                        val (recordEntity, sampleEntities) = avroRecord.toStepsCadenceRecordEntity()
                        appDatabase.withTransaction {
                            stepsCadenceRecordDao.insertRecordWithSamples(recordEntity, sampleEntities)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to process and insert StepsCadenceRecord ${avroRecord.metadata.id} from file ${file.name}", e)
                        allTransactionsInFileSucceeded = false
                        break
                    }
                }
                if (allTransactionsInFileSucceeded) {
                    Log.i(TAG, "Successfully processed ${avroRecords.size} StepsCadenceRecord(s) from ${file.name}")
                    processedSuccessfully = true
                } else {
                    Log.w(TAG, "One or more StepsCadenceRecord(s) in ${file.name} failed to process fully.")
                }
            } else {
                Log.i(TAG, "No StepsCadenceRecord objects found in ${file.name}, file is empty.")
                processedSuccessfully = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing Avro file ${file.name} for type StepsCadence", e)
            processedSuccessfully = false
        }
        return processedSuccessfully
    }

    @OptIn(ExperimentalAvro4kApi::class)
    private suspend fun processSpeedRecords(file: File): Boolean {
        var processedSuccessfully = false
        try {
            val avroRecords = Files.newInputStream(file.toPath()).buffered().use {
                AvroObjectContainer.decodeFromStream<io.github.hitoshura25.healthsyncapp.avro.AvroSpeedRecord>(it).toList()
            }
            if (avroRecords.isNotEmpty()) {
                var allTransactionsInFileSucceeded = true
                for (avroRecord in avroRecords) {
                    try {
                        val (recordEntity, sampleEntities) = avroRecord.toSpeedRecordEntity()
                        appDatabase.withTransaction {
                            speedRecordDao.insertRecordWithSamples(recordEntity, sampleEntities)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to process and insert SpeedRecord ${avroRecord.metadata.id} from file ${file.name}", e)
                        allTransactionsInFileSucceeded = false
                        break
                    }
                }
                if (allTransactionsInFileSucceeded) {
                    Log.i(TAG, "Successfully processed ${avroRecords.size} SpeedRecord(s) from ${file.name}")
                    processedSuccessfully = true
                } else {
                    Log.w(TAG, "One or more SpeedRecord(s) in ${file.name} failed to process fully.")
                }
            } else {
                Log.i(TAG, "No SpeedRecord objects found in ${file.name}, file is empty.")
                processedSuccessfully = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing Avro file ${file.name} for type Speed", e)
            processedSuccessfully = false
        }
        return processedSuccessfully
    }

    @OptIn(ExperimentalAvro4kApi::class)
    private suspend fun processPowerRecords(file: File): Boolean {
        var processedSuccessfully = false
        try {
            val avroRecords = Files.newInputStream(file.toPath()).buffered().use {
                AvroObjectContainer.decodeFromStream<AvroPowerRecord>(it).toList()
            }
            if (avroRecords.isNotEmpty()) {
                var allTransactionsInFileSucceeded = true
                for (avroRecord in avroRecords) {
                    try {
                        val (recordEntity, sampleEntities) = avroRecord.toPowerRecordEntity()
                        appDatabase.withTransaction {
                            powerRecordDao.insertRecordWithSamples(recordEntity, sampleEntities)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to process and insert PowerRecord ${avroRecord.metadata.id} from file ${file.name}", e)
                        allTransactionsInFileSucceeded = false
                        break
                    }
                }
                if (allTransactionsInFileSucceeded) {
                    Log.i(TAG, "Successfully processed ${avroRecords.size} PowerRecord(s) from ${file.name}")
                    processedSuccessfully = true
                } else {
                    Log.w(TAG, "One or more PowerRecord(s) in ${file.name} failed to process fully.")
                }
            } else {
                Log.i(TAG, "No PowerRecord objects found in ${file.name}, file is empty.")
                processedSuccessfully = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing Avro file ${file.name} for type Power", e)
            processedSuccessfully = false
        }
        return processedSuccessfully
    }

    @OptIn(ExperimentalAvro4kApi::class)
    private suspend fun processCyclingPedalingCadenceRecords(file: File): Boolean {
        var processedSuccessfully = false
        try {
            val avroRecords = Files.newInputStream(file.toPath()).buffered().use {
                AvroObjectContainer.decodeFromStream<AvroCyclingPedalingCadenceRecord>(it).toList()
            }
            if (avroRecords.isNotEmpty()) {
                var allTransactionsInFileSucceeded = true
                for (avroRecord in avroRecords) {
                    try {
                        val (recordEntity, sampleEntities) = avroRecord.toCyclingPedalingCadenceRecordEntity()
                        appDatabase.withTransaction {
                            cyclingPedalingCadenceRecordDao.insertRecordWithSamples(recordEntity, sampleEntities)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to process and insert CyclingPedalingCadenceRecord ${avroRecord.metadata.id} from file ${file.name}", e)
                        allTransactionsInFileSucceeded = false
                        break
                    }
                }
                if (allTransactionsInFileSucceeded) {
                    Log.i(TAG, "Successfully processed ${avroRecords.size} CyclingPedalingCadenceRecord(s) from ${file.name}")
                    processedSuccessfully = true
                } else {
                    Log.w(TAG, "One or more CyclingPedalingCadenceRecord(s) in ${file.name} failed to process fully.")
                }
            } else {
                Log.i(TAG, "No CyclingPedalingCadenceRecord objects found in ${file.name}, file is empty.")
                processedSuccessfully = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing Avro file ${file.name} for type CyclingPedalingCadence", e)
            processedSuccessfully = false
        }
        return processedSuccessfully
    }
}
