package io.github.hitoshura25.healthsyncapp.worker

// Avro DTO imports

// Room Entity imports for verification
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.github.avrokotlin.avro4k.AvroObjectContainer
import com.github.avrokotlin.avro4k.ExperimentalAvro4kApi
import com.github.avrokotlin.avro4k.openWriter
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.github.hitoshura25.healthsyncapp.avro.AvroActiveCaloriesBurnedRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroBasalBodyTemperatureRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroBasalMetabolicRateRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroBloodGlucoseMealType
import io.github.hitoshura25.healthsyncapp.avro.AvroBloodGlucoseRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroBloodGlucoseRelationToMeal
import io.github.hitoshura25.healthsyncapp.avro.AvroBloodGlucoseSpecimenSource
import io.github.hitoshura25.healthsyncapp.avro.AvroBloodPressureBodyPosition
import io.github.hitoshura25.healthsyncapp.avro.AvroBloodPressureMeasurementLocation
import io.github.hitoshura25.healthsyncapp.avro.AvroBloodPressureRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroBodyFatRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroBodyTemperatureMeasurementLocation
import io.github.hitoshura25.healthsyncapp.avro.AvroBodyTemperatureRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroBodyWaterMassRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroBoneMassRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroCyclingPedalingCadenceRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroCyclingPedalingCadenceSample
import io.github.hitoshura25.healthsyncapp.avro.AvroDevice
import io.github.hitoshura25.healthsyncapp.avro.AvroDistanceRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroElevationGainedRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroExerciseSessionRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroExerciseType
import io.github.hitoshura25.healthsyncapp.avro.AvroFloorsClimbedRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroHeartRateRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroHeartRateSample
import io.github.hitoshura25.healthsyncapp.avro.AvroHeartRateVariabilityRmssdRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroHeightRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroHydrationRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroLeanBodyMassRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroMetadata
import io.github.hitoshura25.healthsyncapp.avro.AvroNutritionRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroOxygenSaturationRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroPowerRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroPowerSample
import io.github.hitoshura25.healthsyncapp.avro.AvroRespiratoryRateRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroRestingHeartRateRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroSleepSessionRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroSleepStageRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroSleepStageType
import io.github.hitoshura25.healthsyncapp.avro.AvroSpeedRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroSpeedSample
import io.github.hitoshura25.healthsyncapp.avro.AvroStepsCadenceRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroStepsCadenceSample
import io.github.hitoshura25.healthsyncapp.avro.AvroStepsRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroTotalCaloriesBurnedRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroVo2MaxMeasurementMethod
import io.github.hitoshura25.healthsyncapp.avro.AvroVo2MaxRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroWeightRecord
import io.github.hitoshura25.healthsyncapp.data.local.database.AppDatabase
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.ActiveCaloriesBurnedRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BasalBodyTemperatureRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BasalMetabolicRateRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BloodGlucoseEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BloodPressureRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BodyFatRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BodyTemperatureRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BodyWaterMassRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BoneMassRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.DistanceRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.ElevationGainedRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.ExerciseSessionRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.FloorsClimbedRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.HeartRateVariabilityRmssdRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.HeightRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.HydrationRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.LeanBodyMassRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.NutritionRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.OxygenSaturationRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.RespiratoryRateRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.RestingHeartRateRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.StepsRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.TotalCaloriesBurnedRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.Vo2MaxRecordEntity
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.WeightRecordEntity
import io.github.hitoshura25.healthsyncapp.file.FileHandler
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.util.UUID
import javax.inject.Inject

@HiltAndroidTest
@Config(application = HiltTestApplication::class, manifest = Config.NONE)
@RunWith(RobolectricTestRunner::class)
class AvroFileProcessorWorkerRobolectricTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var database: AppDatabase

    @Inject
    lateinit var fileHandler: FileHandler

    private lateinit var context: Context
    private lateinit var stagingDir: File
    private lateinit var completedDir: File

    @Before
    fun setUp() {
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext<HiltTestApplication>()
        stagingDir = fileHandler.getStagingDirectory()
        completedDir = fileHandler.getCompletedDirectory()
        stagingDir.mkdirs()
        completedDir.mkdirs()
    }

    @After
    fun tearDown() {
        database.close()
        stagingDir.deleteRecursively()
        completedDir.deleteRecursively()
    }

    @OptIn(ExperimentalAvro4kApi::class)
    private inline fun <reified T : Any> createAvroFileInDir(directory: File, fileName: String, records: List<T>) {
        val file = File(directory, fileName)
        file.outputStream().buffered().use { outputStream ->
            AvroObjectContainer.openWriter<T>(outputStream).use { writer ->
                records.forEach { record -> writer.writeValue(record) }
            }
        }
    }

    private suspend inline fun <reified AvroType : Any, EntityType : Any> testSingleRecordTypeProcessing(
        avroRecord: AvroType,
        hcUid: String,
        fileNamePrefix: String,
        daoQueryFunction: suspend (String) -> EntityType?,
        verificationFunction: (EntityType) -> Unit
    ) {
        val file = File(stagingDir, "${fileNamePrefix}_${hcUid}.avro")
        createAvroFileInDir<AvroType>(stagingDir, file.name, listOf(avroRecord))

        val worker = TestListenableWorkerBuilder<AvroFileProcessorWorker>(context)
            .setWorkerFactory(workerFactory)
            .build()

        val result = worker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        assertThat(stagingDir.listFiles()?.size).isEqualTo(0)
        assertThat(completedDir.listFiles()?.size).isEqualTo(1)

        val retrievedEntity = daoQueryFunction(hcUid)
        assertThat(retrievedEntity).isNotNull()
        verificationFunction(retrievedEntity!!)
    }

    private suspend fun testSleepSessionRecordProcessing(avroRecord: AvroSleepSessionRecord) {
        val hcUid = avroRecord.metadata.id
        val file = File(stagingDir, "SleepSessionRecord_${hcUid}.avro")
        createAvroFileInDir(stagingDir, file.name, listOf(avroRecord))

        val worker = TestListenableWorkerBuilder<AvroFileProcessorWorker>(context)
            .setWorkerFactory(workerFactory)
            .build()

        val result = worker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        assertThat(stagingDir.listFiles()?.size).isEqualTo(0)
        assertThat(completedDir.listFiles()?.size).isEqualTo(1)

        val retrievedSession = database.sleepSessionDao().getRecordByHcUid(hcUid)
        assertThat(retrievedSession).isNotNull()
        assertThat(retrievedSession?.title).isEqualTo(avroRecord.title)

        val retrievedStages = database.sleepStageDao().getStagesBySessionHcUid(hcUid)
        assertThat(retrievedStages.size).isEqualTo(avroRecord.stages.size)
    }

    private suspend fun testHeartRateRecordProcessing(avroRecord: AvroHeartRateRecord) {
        val hcUid = avroRecord.metadata.id
        val file = File(stagingDir, "HeartRateRecord_${hcUid}.avro")
        createAvroFileInDir(stagingDir, file.name, listOf(avroRecord))

        val worker = TestListenableWorkerBuilder<AvroFileProcessorWorker>(context)
            .setWorkerFactory(workerFactory)
            .build()

        val result = worker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        assertThat(stagingDir.listFiles()?.size).isEqualTo(0)
        assertThat(completedDir.listFiles()?.size).isEqualTo(1)

        val retrievedSamples = database.heartRateSampleDao().getSamplesByRecordHcUid(hcUid)
        assertThat(retrievedSamples).isNotEmpty()
        assertThat(retrievedSamples.size).isEqualTo(avroRecord.samples.size)
    }

    private suspend fun testStepsCadenceRecordProcessing(avroRecord: AvroStepsCadenceRecord) {
        val hcUid = avroRecord.metadata.id
        val file = File(stagingDir, "StepsCadenceRecord_${hcUid}.avro")
        createAvroFileInDir(stagingDir, file.name, listOf(avroRecord))

        val worker = TestListenableWorkerBuilder<AvroFileProcessorWorker>(context)
            .setWorkerFactory(workerFactory)
            .build()

        val result = worker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        assertThat(stagingDir.listFiles()?.size).isEqualTo(0)
        assertThat(completedDir.listFiles()?.size).isEqualTo(1)

        val retrievedRecord = database.stepsCadenceRecordDao().getRecordByHcUid(hcUid)
        assertThat(retrievedRecord).isNotNull()

        val retrievedSamples = database.stepsCadenceRecordDao().getSamplesForRecord(hcUid)
        assertThat(retrievedSamples.size).isEqualTo(avroRecord.samples.size)
    }

    private suspend fun testSpeedRecordProcessing(avroRecord: AvroSpeedRecord) {
        val hcUid = avroRecord.metadata.id
        val file = File(stagingDir, "SpeedRecord_${hcUid}.avro")
        createAvroFileInDir(stagingDir, file.name, listOf(avroRecord))

        val worker = TestListenableWorkerBuilder<AvroFileProcessorWorker>(context)
            .setWorkerFactory(workerFactory)
            .build()

        val result = worker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        assertThat(stagingDir.listFiles()?.size).isEqualTo(0)
        assertThat(completedDir.listFiles()?.size).isEqualTo(1)

        val retrievedRecord = database.speedRecordDao().getRecordByHcUid(hcUid)
        assertThat(retrievedRecord).isNotNull()

        val retrievedSamples = database.speedRecordDao().getSamplesForRecord(hcUid)
        assertThat(retrievedSamples.size).isEqualTo(avroRecord.samples.size)
    }

    private suspend fun testPowerRecordProcessing(avroRecord: AvroPowerRecord) {
        val hcUid = avroRecord.metadata.id
        val file = File(stagingDir, "PowerRecord_${hcUid}.avro")
        createAvroFileInDir(stagingDir, file.name, listOf(avroRecord))

        val worker = TestListenableWorkerBuilder<AvroFileProcessorWorker>(context)
            .setWorkerFactory(workerFactory)
            .build()

        val result = worker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        assertThat(stagingDir.listFiles()?.size).isEqualTo(0)
        assertThat(completedDir.listFiles()?.size).isEqualTo(1)

        val retrievedRecord = database.powerRecordDao().getRecordByHcUid(hcUid)
        assertThat(retrievedRecord).isNotNull()

        val retrievedSamples = database.powerRecordDao().getSamplesForRecord(hcUid)
        assertThat(retrievedSamples.size).isEqualTo(avroRecord.samples.size)
    }

    private suspend fun testCyclingPedalingCadenceRecordProcessing(avroRecord: AvroCyclingPedalingCadenceRecord) {
        val hcUid = avroRecord.metadata.id
        val file = File(stagingDir, "CyclingPedalingCadenceRecord_${hcUid}.avro")
        createAvroFileInDir(stagingDir, file.name, listOf(avroRecord))

        val worker = TestListenableWorkerBuilder<AvroFileProcessorWorker>(context)
            .setWorkerFactory(workerFactory)
            .build()

        val result = worker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        assertThat(stagingDir.listFiles()?.size).isEqualTo(0)
        assertThat(completedDir.listFiles()?.size).isEqualTo(1)

        val retrievedRecord = database.cyclingPedalingCadenceRecordDao().getRecordByHcUid(hcUid)
        assertThat(retrievedRecord).isNotNull()

        val retrievedSamples = database.cyclingPedalingCadenceRecordDao().getSamplesForRecord(hcUid)
        assertThat(retrievedSamples.size).isEqualTo(avroRecord.samples.size)
    }

    @Test
    fun testWorker_processesStepsRecord_movesToCompleted_andSavesData() = runBlocking {
        val hcUid = "steps-test-id-${UUID.randomUUID()}"
        val testRecord = AvroStepsRecord(
            metadata = AvroMetadata(id = hcUid, dataOriginPackageName = "test.package", lastModifiedTimeEpochMillis = System.currentTimeMillis(), clientRecordId = "client-steps", clientRecordVersion = 1L, device = AvroDevice("Test Manufacturer", "Test Model", "Test Type")),
            startTimeEpochMillis = 1L,
            endTimeEpochMillis = 2L,
            startZoneOffsetId = null,
            endZoneOffsetId = null,
            count = 100L,
            appRecordFetchTimeEpochMillis = System.currentTimeMillis()
        )
        testSingleRecordTypeProcessing(
            avroRecord = testRecord,
            hcUid = hcUid,
            fileNamePrefix = "StepsRecord",
            daoQueryFunction = { id -> database.stepsRecordDao().getRecordByHcUid(id) },
            verificationFunction = { entity: StepsRecordEntity ->
                assertThat(entity.count).isEqualTo(100L)
                assertThat(entity.hcUid).isEqualTo(hcUid)
            }
        )
    }

    @Test
    fun testWorker_processesBloodGlucoseRecord_movesToCompleted_andSavesData() = runBlocking {
        val hcUid = "blood-glucose-test-id-${UUID.randomUUID()}"
        val testRecord = AvroBloodGlucoseRecord(
            metadata = AvroMetadata(id = hcUid, dataOriginPackageName = "test.package", lastModifiedTimeEpochMillis = System.currentTimeMillis(), clientRecordId = "client-blood-glucose", clientRecordVersion = 1L, device = AvroDevice("Test Manufacturer", "Test Model", "Test Type")),
            timeEpochMillis = System.currentTimeMillis(),
            zoneOffsetId = null,
            levelInMilligramsPerDeciliter = 120.0,
            specimenSource = AvroBloodGlucoseSpecimenSource.CAPILLARY_BLOOD,
            mealType = AvroBloodGlucoseMealType.BREAKFAST,
            relationToMeal = AvroBloodGlucoseRelationToMeal.BEFORE_MEAL,
            appRecordFetchTimeEpochMillis = System.currentTimeMillis()
        )
        testSingleRecordTypeProcessing(
            avroRecord = testRecord,
            hcUid = hcUid,
            fileNamePrefix = "BloodGlucoseRecord",
            daoQueryFunction = { id -> database.bloodGlucoseDao().getRecordByHcUid(id) },
            verificationFunction = { entity: BloodGlucoseEntity ->
                assertThat(entity.levelInMilligramsPerDeciliter).isEqualTo(120.0)
                assertThat(entity.hcUid).isEqualTo(hcUid)
            }
        )
    }

    @Test
    fun testWorker_processesWeightRecord_movesToCompleted_andSavesData() = runBlocking {
        val hcUid = "weight-test-id-${UUID.randomUUID()}"
        val testRecord = AvroWeightRecord(
            metadata = AvroMetadata(id = hcUid, dataOriginPackageName = "test.package", lastModifiedTimeEpochMillis = System.currentTimeMillis(), clientRecordId = "client-weight", clientRecordVersion = 1L, device = AvroDevice("Test Manufacturer", "Test Model", "Test Type")),
            timeEpochMillis = System.currentTimeMillis(),
            zoneOffsetId = null,
            weightInKilograms = 70.5,
            appRecordFetchTimeEpochMillis = System.currentTimeMillis()
        )
        testSingleRecordTypeProcessing(
            avroRecord = testRecord,
            hcUid = hcUid,
            fileNamePrefix = "WeightRecord",
            daoQueryFunction = { id -> database.weightRecordDao().getRecordByHcUid(id) },
            verificationFunction = { entity: WeightRecordEntity ->
                assertThat(entity.weightInKilograms).isEqualTo(70.5)
                assertThat(entity.hcUid).isEqualTo(hcUid)
            }
        )
    }

    @Test
    fun testWorker_processesActiveCaloriesBurnedRecord_movesToCompleted_andSavesData() = runBlocking {
        val hcUid = "active-calories-test-id-${UUID.randomUUID()}"
        val testRecord = AvroActiveCaloriesBurnedRecord(
            metadata = AvroMetadata(id = hcUid, dataOriginPackageName = "test.package", lastModifiedTimeEpochMillis = System.currentTimeMillis(), clientRecordId = "client-active-calories", clientRecordVersion = 1L, device = AvroDevice("Test Manufacturer", "Test Model", "Test Type")),
            startTimeEpochMillis = 1L,
            endTimeEpochMillis = 2L,
            energyInKilocalories = 250.0,
            appRecordFetchTimeEpochMillis = System.currentTimeMillis()
        )
        testSingleRecordTypeProcessing(
            avroRecord = testRecord,
            hcUid = hcUid,
            fileNamePrefix = "ActiveCaloriesBurnedRecord",
            daoQueryFunction = { id -> database.activeCaloriesBurnedRecordDao().getRecordByHcUid(id) },
            verificationFunction = { entity: ActiveCaloriesBurnedRecordEntity ->
                assertThat(entity.energyInKilocalories).isEqualTo(250.0)
                assertThat(entity.hcUid).isEqualTo(hcUid)
            }
        )
    }

    @Test
    fun testWorker_processesBasalBodyTemperatureRecord_movesToCompleted_andSavesData() = runBlocking {
        val hcUid = "basal-body-temp-test-id-${UUID.randomUUID()}"
        val testRecord = AvroBasalBodyTemperatureRecord(
            metadata = AvroMetadata(id = hcUid, dataOriginPackageName = "test.package", lastModifiedTimeEpochMillis = System.currentTimeMillis(), clientRecordId = "client-basal-body-temp", clientRecordVersion = 1L, device = AvroDevice("Test Manufacturer", "Test Model", "Test Type")),
            timeEpochMillis = System.currentTimeMillis(),
            zoneOffsetId = null,
            temperatureInCelsius = 36.5,
            measurementLocation = AvroBodyTemperatureMeasurementLocation.ARMPIT.ordinal,
            appRecordFetchTimeEpochMillis = System.currentTimeMillis()
        )
        testSingleRecordTypeProcessing(
            avroRecord = testRecord,
            hcUid = hcUid,
            fileNamePrefix = "BasalBodyTemperatureRecord",
            daoQueryFunction = { id -> database.basalBodyTemperatureRecordDao().getRecordByHcUid(id) },
            verificationFunction = { entity: BasalBodyTemperatureRecordEntity ->
                assertThat(entity.temperatureInCelsius).isEqualTo(36.5)
                assertThat(entity.hcUid).isEqualTo(hcUid)
            }
        )
    }

    @Test
    fun testWorker_processesBasalMetabolicRateRecord_movesToCompleted_andSavesData() = runBlocking {
        val hcUid = "basal-metabolic-rate-test-id-${UUID.randomUUID()}"
        val testRecord = AvroBasalMetabolicRateRecord(
            metadata = AvroMetadata(id = hcUid, dataOriginPackageName = "test.package", lastModifiedTimeEpochMillis = System.currentTimeMillis(), clientRecordId = "client-basal-metabolic-rate", clientRecordVersion = 1L, device = AvroDevice("Test Manufacturer", "Test Model", "Test Type")),
            timeEpochMillis = System.currentTimeMillis(),
            zoneOffsetId = null,
            basalMetabolicRateInKilocaloriesPerDay = 1500.0,
            appRecordFetchTimeEpochMillis = System.currentTimeMillis()
        )
        testSingleRecordTypeProcessing(
            avroRecord = testRecord,
            hcUid = hcUid,
            fileNamePrefix = "BasalMetabolicRateRecord",
            daoQueryFunction = { id -> database.basalMetabolicRateRecordDao().getRecordByHcUid(id) },
            verificationFunction = { entity: BasalMetabolicRateRecordEntity ->
                assertThat(entity.basalMetabolicRateInKilocaloriesPerDay).isEqualTo(1500.0)
                assertThat(entity.hcUid).isEqualTo(hcUid)
            }
        )
    }

    @Test
    fun testWorker_processesDistanceRecord_movesToCompleted_andSavesData() = runBlocking {
        val hcUid = "distance-test-id-${UUID.randomUUID()}"
        val testRecord = AvroDistanceRecord(
            metadata = AvroMetadata(id = hcUid, dataOriginPackageName = "test.package", lastModifiedTimeEpochMillis = System.currentTimeMillis(), clientRecordId = "client-distance", clientRecordVersion = 1L, device = AvroDevice("Test Manufacturer", "Test Model", "Test Type")),
            startTimeEpochMillis = 1L,
            endTimeEpochMillis = 2L,
            distanceInMeters = 5000.0,
            appRecordFetchTimeEpochMillis = System.currentTimeMillis()
        )
        testSingleRecordTypeProcessing(
            avroRecord = testRecord,
            hcUid = hcUid,
            fileNamePrefix = "DistanceRecord",
            daoQueryFunction = { id -> database.distanceRecordDao().getRecordByHcUid(id) },
            verificationFunction = { entity: DistanceRecordEntity ->
                assertThat(entity.distanceInMeters).isEqualTo(5000.0)
                assertThat(entity.hcUid).isEqualTo(hcUid)
            }
        )
    }

    @Test
    fun testWorker_processesElevationGainedRecord_movesToCompleted_andSavesData() = runBlocking {
        val hcUid = "elevation-gained-test-id-${UUID.randomUUID()}"
        val testRecord = AvroElevationGainedRecord(
            metadata = AvroMetadata(id = hcUid, dataOriginPackageName = "test.package", lastModifiedTimeEpochMillis = System.currentTimeMillis(), clientRecordId = "client-elevation-gained", clientRecordVersion = 1L, device = AvroDevice("Test Manufacturer", "Test Model", "Test Type")),
            startTimeEpochMillis = 1L,
            endTimeEpochMillis = 2L,
            elevationInMeters = 100.0,
            appRecordFetchTimeEpochMillis = System.currentTimeMillis()
        )
        testSingleRecordTypeProcessing(
            avroRecord = testRecord,
            hcUid = hcUid,
            fileNamePrefix = "ElevationGainedRecord",
            daoQueryFunction = { id -> database.elevationGainedRecordDao().getRecordByHcUid(id) },
            verificationFunction = { entity: ElevationGainedRecordEntity ->
                assertThat(entity.elevationInMeters).isEqualTo(100.0)
                assertThat(entity.hcUid).isEqualTo(hcUid)
            }
        )
    }

    @Test
    fun testWorker_processesExerciseSessionRecord_movesToCompleted_andSavesData() = runBlocking {
        val hcUid = "exercise-session-test-id-${UUID.randomUUID()}"
        val testRecord = AvroExerciseSessionRecord(
            metadata = AvroMetadata(id = hcUid, dataOriginPackageName = "test.package", lastModifiedTimeEpochMillis = System.currentTimeMillis(), clientRecordId = "client-exercise-session", clientRecordVersion = 1L, device = AvroDevice("Test Manufacturer", "Test Model", "Test Type")),
            startTimeEpochMillis = 1L,
            endTimeEpochMillis = 2L,
            startZoneOffsetId = null,
            endZoneOffsetId = null,
            exerciseType = AvroExerciseType.RUNNING,
            title = "Morning Run",
            notes = "5k",
            appRecordFetchTimeEpochMillis = System.currentTimeMillis()
        )
        testSingleRecordTypeProcessing(
            avroRecord = testRecord,
            hcUid = hcUid,
            fileNamePrefix = "ExerciseSessionRecord",
            daoQueryFunction = { id -> database.exerciseSessionRecordDao().getRecordByHcUid(id) },
            verificationFunction = { entity: ExerciseSessionRecordEntity ->
                assertThat(entity.exerciseType).isEqualTo(AvroExerciseType.RUNNING.ordinal)
                assertThat(entity.title).isEqualTo("Morning Run")
                assertThat(entity.hcUid).isEqualTo(hcUid)
            }
        )
    }

    @Test
    fun testWorker_processesFloorsClimbedRecord_movesToCompleted_andSavesData() = runBlocking {
        val hcUid = "floors-climbed-test-id-${UUID.randomUUID()}"
        val testRecord = AvroFloorsClimbedRecord(
            metadata = AvroMetadata(id = hcUid, dataOriginPackageName = "test.package", lastModifiedTimeEpochMillis = System.currentTimeMillis(), clientRecordId = "client-floors-climbed", clientRecordVersion = 1L, device = AvroDevice("Test Manufacturer", "Test Model", "Test Type")),
            startTimeEpochMillis = 1L,
            endTimeEpochMillis = 2L,
            startZoneOffsetId = null,
            endZoneOffsetId = null,
            floors = 10.0,
            appRecordFetchTimeEpochMillis = System.currentTimeMillis()
        )
        testSingleRecordTypeProcessing(
            avroRecord = testRecord,
            hcUid = hcUid,
            fileNamePrefix = "FloorsClimbedRecord",
            daoQueryFunction = { id -> database.floorsClimbedRecordDao().getRecordByHcUid(id) },
            verificationFunction = { entity: FloorsClimbedRecordEntity ->
                assertThat(entity.floors).isEqualTo(10.0)
                assertThat(entity.hcUid).isEqualTo(hcUid)
            }
        )
    }

    @Test
    fun testWorker_processesHeartRateVariabilityRmssdRecord_movesToCompleted_andSavesData() = runBlocking {
        val hcUid = "hrv-rmssd-test-id-${UUID.randomUUID()}"
        val testRecord = AvroHeartRateVariabilityRmssdRecord(
            metadata = AvroMetadata(id = hcUid, dataOriginPackageName = "test.package", lastModifiedTimeEpochMillis = System.currentTimeMillis(), clientRecordId = "client-hrv-rmssd", clientRecordVersion = 1L, device = AvroDevice("Test Manufacturer", "Test Model", "Test Type")),
            timeEpochMillis = System.currentTimeMillis(),
            zoneOffsetId = null,
            heartRateVariabilityRmssd = 50.0,
            appRecordFetchTimeEpochMillis = System.currentTimeMillis()
        )
        testSingleRecordTypeProcessing(
            avroRecord = testRecord,
            hcUid = hcUid,
            fileNamePrefix = "HeartRateVariabilityRmssdRecord",
            daoQueryFunction = { id -> database.heartRateVariabilityRmssdRecordDao().getRecordByHcUid(id) },
            verificationFunction = { entity: HeartRateVariabilityRmssdRecordEntity ->
                assertThat(entity.heartRateVariabilityRmssd).isEqualTo(50.0)
                assertThat(entity.hcUid).isEqualTo(hcUid)
            }
        )
    }

    @Test
    fun testWorker_processesRestingHeartRateRecord_movesToCompleted_andSavesData() = runBlocking {
        val hcUid = "resting-hr-test-id-${UUID.randomUUID()}"
        val testRecord = AvroRestingHeartRateRecord(
            metadata = AvroMetadata(id = hcUid, dataOriginPackageName = "test.package", lastModifiedTimeEpochMillis = System.currentTimeMillis(), clientRecordId = "client-resting-hr", clientRecordVersion = 1L, device = AvroDevice("Test Manufacturer", "Test Model", "Test Type")),
            timeEpochMillis = System.currentTimeMillis(),
            zoneOffsetId = null,
            beatsPerMinute = 60L,
            appRecordFetchTimeEpochMillis = System.currentTimeMillis()
        )
        testSingleRecordTypeProcessing(
            avroRecord = testRecord,
            hcUid = hcUid,
            fileNamePrefix = "RestingHeartRateRecord",
            daoQueryFunction = { id -> database.restingHeartRateRecordDao().getRecordByHcUid(id) },
            verificationFunction = { entity: RestingHeartRateRecordEntity ->
                assertThat(entity.beatsPerMinute).isEqualTo(60L)
                assertThat(entity.hcUid).isEqualTo(hcUid)
            }
        )
    }

    @Test
    fun testWorker_processesTotalCaloriesBurnedRecord_movesToCompleted_andSavesData() = runBlocking {
        val hcUid = "total-calories-test-id-${UUID.randomUUID()}"
        val testRecord = AvroTotalCaloriesBurnedRecord(
            metadata = AvroMetadata(id = hcUid, dataOriginPackageName = "test.package", lastModifiedTimeEpochMillis = System.currentTimeMillis(), clientRecordId = "client-total-calories", clientRecordVersion = 1L, device = AvroDevice("Test Manufacturer", "Test Model", "Test Type")),
            startTimeEpochMillis = 1L,
            endTimeEpochMillis = 2L,
            startZoneOffsetId = null,
            endZoneOffsetId = null,
            energyInKilocalories = 2000.0,
            appRecordFetchTimeEpochMillis = System.currentTimeMillis()
        )
        testSingleRecordTypeProcessing(
            avroRecord = testRecord,
            hcUid = hcUid,
            fileNamePrefix = "TotalCaloriesBurnedRecord",
            daoQueryFunction = { id -> database.totalCaloriesBurnedRecordDao().getRecordByHcUid(id) },
            verificationFunction = { entity: TotalCaloriesBurnedRecordEntity ->
                assertThat(entity.energyInKilocalories).isEqualTo(2000.0)
                assertThat(entity.hcUid).isEqualTo(hcUid)
            }
        )
    }

    @Test
    fun testWorker_processesVo2MaxRecord_movesToCompleted_andSavesData() = runBlocking {
        val hcUid = "vo2max-test-id-${UUID.randomUUID()}"
        val testRecord = AvroVo2MaxRecord(
            metadata = AvroMetadata(id = hcUid, dataOriginPackageName = "test.package", lastModifiedTimeEpochMillis = System.currentTimeMillis(), clientRecordId = "client-vo2max", clientRecordVersion = 1L, device = AvroDevice("Test Manufacturer", "Test Model", "Test Type")),
            timeEpochMillis = System.currentTimeMillis(),
            zoneOffsetId = null,
            vo2MillilitersPerMinuteKilogram = 45.0,
            measurementMethod = AvroVo2MaxMeasurementMethod.OTHER,
            appRecordFetchTimeEpochMillis = System.currentTimeMillis()
        )
        testSingleRecordTypeProcessing(
            avroRecord = testRecord,
            hcUid = hcUid,
            fileNamePrefix = "Vo2MaxRecord",
            daoQueryFunction = { id -> database.vo2MaxRecordDao().getRecordByHcUid(id) },
            verificationFunction = { entity: Vo2MaxRecordEntity ->
                assertThat(entity.vo2Max).isEqualTo(45.0)
                assertThat(entity.hcUid).isEqualTo(hcUid)
            }
        )
    }

    @Test
    fun testWorker_processesBodyFatRecord_movesToCompleted_andSavesData() = runBlocking {
        val hcUid = "body-fat-test-id-${UUID.randomUUID()}"
        val testRecord = AvroBodyFatRecord(
            metadata = AvroMetadata(id = hcUid, dataOriginPackageName = "test.package", lastModifiedTimeEpochMillis = System.currentTimeMillis(), clientRecordId = "client-body-fat", clientRecordVersion = 1L, device = AvroDevice("Test Manufacturer", "Test Model", "Test Type")),
            timeEpochMillis = System.currentTimeMillis(),
            zoneOffsetId = null,
            percentage = 20.5,
            appRecordFetchTimeEpochMillis = System.currentTimeMillis()
        )
        testSingleRecordTypeProcessing(
            avroRecord = testRecord,
            hcUid = hcUid,
            fileNamePrefix = "BodyFatRecord",
            daoQueryFunction = { id -> database.bodyFatRecordDao().getRecordByHcUid(id) },
            verificationFunction = { entity: BodyFatRecordEntity ->
                assertThat(entity.percentage).isEqualTo(20.5)
                assertThat(entity.hcUid).isEqualTo(hcUid)
            }
        )
    }

    @Test
    fun testWorker_processesBodyTemperatureRecord_movesToCompleted_andSavesData() = runBlocking {
        val hcUid = "body-temp-test-id-${UUID.randomUUID()}"
        val testRecord = AvroBodyTemperatureRecord(
            metadata = AvroMetadata(id = hcUid, dataOriginPackageName = "test.package", lastModifiedTimeEpochMillis = System.currentTimeMillis(), clientRecordId = "client-body-temp", clientRecordVersion = 1L, device = AvroDevice("Test Manufacturer", "Test Model", "Test Type")),
            timeEpochMillis = System.currentTimeMillis(),
            zoneOffsetId = null,
            temperatureInCelsius = 37.0,
            measurementLocation = AvroBodyTemperatureMeasurementLocation.ARMPIT,
            appRecordFetchTimeEpochMillis = System.currentTimeMillis()
        )
        testSingleRecordTypeProcessing(
            avroRecord = testRecord,
            hcUid = hcUid,
            fileNamePrefix = "BodyTemperatureRecord",
            daoQueryFunction = { id -> database.bodyTemperatureRecordDao().getRecordByHcUid(id) },
            verificationFunction = { entity: BodyTemperatureRecordEntity ->
                assertThat(entity.temperatureInCelsius).isEqualTo(37.0)
                assertThat(entity.hcUid).isEqualTo(hcUid)
            }
        )
    }

    @Test
    fun testWorker_processesBodyWaterMassRecord_movesToCompleted_andSavesData() = runBlocking {
        val hcUid = "body-water-mass-test-id-${UUID.randomUUID()}"
        val testRecord = AvroBodyWaterMassRecord(
            metadata = AvroMetadata(id = hcUid, dataOriginPackageName = "test.package", lastModifiedTimeEpochMillis = System.currentTimeMillis(), clientRecordId = "client-body-water-mass", clientRecordVersion = 1L, device = AvroDevice("Test Manufacturer", "Test Model", "Test Type")),
            timeEpochMillis = System.currentTimeMillis(),
            zoneOffsetId = null,
            massInKilograms = 50.0,
            appRecordFetchTimeEpochMillis = System.currentTimeMillis()
        )
        testSingleRecordTypeProcessing(
            avroRecord = testRecord,
            hcUid = hcUid,
            fileNamePrefix = "BodyWaterMassRecord",
            daoQueryFunction = { id -> database.bodyWaterMassRecordDao().getRecordByHcUid(id) },
            verificationFunction = { entity: BodyWaterMassRecordEntity ->
                assertThat(entity.massInKilograms).isEqualTo(50.0)
                assertThat(entity.hcUid).isEqualTo(hcUid)
            }
        )
    }

    @Test
    fun testWorker_processesBoneMassRecord_movesToCompleted_andSavesData() = runBlocking {
        val hcUid = "bone-mass-test-id-${UUID.randomUUID()}"
        val testRecord = AvroBoneMassRecord(
            metadata = AvroMetadata(id = hcUid, dataOriginPackageName = "test.package", lastModifiedTimeEpochMillis = System.currentTimeMillis(), clientRecordId = "client-bone-mass", clientRecordVersion = 1L, device = AvroDevice("Test Manufacturer", "Test Model", "Test Type")),
            timeEpochMillis = System.currentTimeMillis(),
            zoneOffsetId = null,
            massInKilograms = 3.0,
            appRecordFetchTimeEpochMillis = System.currentTimeMillis()
        )
        testSingleRecordTypeProcessing(
            avroRecord = testRecord,
            hcUid = hcUid,
            fileNamePrefix = "BoneMassRecord",
            daoQueryFunction = { id -> database.boneMassRecordDao().getRecordByHcUid(id) },
            verificationFunction = { entity: BoneMassRecordEntity ->
                assertThat(entity.massInKilograms).isEqualTo(3.0)
                assertThat(entity.hcUid).isEqualTo(hcUid)
            }
        )
    }

    @Test
    fun testWorker_processesHeightRecord_movesToCompleted_andSavesData() = runBlocking {
        val hcUid = "height-test-id-${UUID.randomUUID()}"
        val testRecord = AvroHeightRecord(
            metadata = AvroMetadata(id = hcUid, dataOriginPackageName = "test.package", lastModifiedTimeEpochMillis = System.currentTimeMillis(), clientRecordId = "client-height", clientRecordVersion = 1L, device = AvroDevice("Test Manufacturer", "Test Model", "Test Type")),
            timeEpochMillis = System.currentTimeMillis(),
            zoneOffsetId = null,
            heightInMeters = 1.75,
            appRecordFetchTimeEpochMillis = System.currentTimeMillis()
        )
        testSingleRecordTypeProcessing(
            avroRecord = testRecord,
            hcUid = hcUid,
            fileNamePrefix = "HeightRecord",
            daoQueryFunction = { id -> database.heightRecordDao().getRecordByHcUid(id) },
            verificationFunction = { entity: HeightRecordEntity ->
                assertThat(entity.heightInMeters).isEqualTo(1.75)
                assertThat(entity.hcUid).isEqualTo(hcUid)
            }
        )
    }

    @Test
    fun testWorker_processesHydrationRecord_movesToCompleted_andSavesData() = runBlocking {
        val hcUid = "hydration-test-id-${UUID.randomUUID()}"
        val testRecord = AvroHydrationRecord(
            metadata = AvroMetadata(id = hcUid, dataOriginPackageName = "test.package", lastModifiedTimeEpochMillis = System.currentTimeMillis(), clientRecordId = "client-hydration", clientRecordVersion = 1L, device = AvroDevice("Test Manufacturer", "Test Model", "Test Type")),
            startTimeEpochMillis = 1L,
            endTimeEpochMillis = 2L,
            startZoneOffsetId = null,
            endZoneOffsetId = null,
            volumeInMilliliters = 500.0,
            appRecordFetchTimeEpochMillis = System.currentTimeMillis()
        )
        testSingleRecordTypeProcessing(
            avroRecord = testRecord,
            hcUid = hcUid,
            fileNamePrefix = "HydrationRecord",
            daoQueryFunction = { id -> database.hydrationRecordDao().getRecordByHcUid(id) },
            verificationFunction = { entity: HydrationRecordEntity ->
                assertThat(entity.volumeInMilliliters).isEqualTo(500.0)
                assertThat(entity.hcUid).isEqualTo(hcUid)
            }
        )
    }

    @Test
    fun testWorker_processesLeanBodyMassRecord_movesToCompleted_andSavesData() = runBlocking {
        val hcUid = "lean-body-mass-test-id-${UUID.randomUUID()}"
        val testRecord = AvroLeanBodyMassRecord(
            metadata = AvroMetadata(id = hcUid, dataOriginPackageName = "test.package", lastModifiedTimeEpochMillis = System.currentTimeMillis(), clientRecordId = "client-lean-body-mass", clientRecordVersion = 1L, device = AvroDevice("Test Manufacturer", "Test Model", "Test Type")),
            timeEpochMillis = System.currentTimeMillis(),
            zoneOffsetId = null,
            massInKilograms = 60.0,
            appRecordFetchTimeEpochMillis = System.currentTimeMillis()
        )
        testSingleRecordTypeProcessing(
            avroRecord = testRecord,
            hcUid = hcUid,
            fileNamePrefix = "LeanBodyMassRecord",
            daoQueryFunction = { id -> database.leanBodyMassRecordDao().getRecordByHcUid(id) },
            verificationFunction = { entity: LeanBodyMassRecordEntity ->
                assertThat(entity.massInKilograms).isEqualTo(60.0)
                assertThat(entity.hcUid).isEqualTo(hcUid)
            }
        )
    }

    @Test
    fun testWorker_processesNutritionRecord_movesToCompleted_andSavesData() = runBlocking {
        val hcUid = "nutrition-test-id-${UUID.randomUUID()}"
        val testRecord = AvroNutritionRecord(
            metadata = AvroMetadata(id = hcUid, dataOriginPackageName = "test.package", lastModifiedTimeEpochMillis = System.currentTimeMillis(), clientRecordId = "client-nutrition", clientRecordVersion = 1L, device = AvroDevice("Test Manufacturer", "Test Model", "Test Type")),
            startTimeEpochMillis = 1L,
            endTimeEpochMillis = 2L,
            startZoneOffsetId = null,
            endZoneOffsetId = null,
            name = "Apple",
            calories = 95.0,
            appRecordFetchTimeEpochMillis = System.currentTimeMillis()
        )
        testSingleRecordTypeProcessing(
            avroRecord = testRecord,
            hcUid = hcUid,
            fileNamePrefix = "NutritionRecord",
            daoQueryFunction = { id -> database.nutritionRecordDao().getRecordByHcUid(id) },
            verificationFunction = { entity: NutritionRecordEntity ->
                assertThat(entity.name).isEqualTo("Apple")
                assertThat(entity.calories).isEqualTo(95.0)
                assertThat(entity.hcUid).isEqualTo(hcUid)
            }
        )
    }

    @Test
    fun testWorker_processesBloodPressureRecord_movesToCompleted_andSavesData() = runBlocking {
        val hcUid = "blood-pressure-test-id-${UUID.randomUUID()}"
        val testRecord = AvroBloodPressureRecord(
            metadata = AvroMetadata(id = hcUid, dataOriginPackageName = "test.package", lastModifiedTimeEpochMillis = System.currentTimeMillis(), clientRecordId = "client-blood-pressure", clientRecordVersion = 1L, device = AvroDevice("Test Manufacturer", "Test Model", "Test Type")),
            timeEpochMillis = System.currentTimeMillis(),
            zoneOffsetId = null,
            systolic = 120.0,
            diastolic = 80.0,
            bodyPosition = AvroBloodPressureBodyPosition.SITTING_DOWN,
            measurementLocation = AvroBloodPressureMeasurementLocation.LEFT_WRIST,
            appRecordFetchTimeEpochMillis = System.currentTimeMillis()
        )
        testSingleRecordTypeProcessing(
            avroRecord = testRecord,
            hcUid = hcUid,
            fileNamePrefix = "BloodPressureRecord",
            daoQueryFunction = { id -> database.bloodPressureRecordDao().getRecordByHcUid(id) },
            verificationFunction = { entity: BloodPressureRecordEntity ->
                assertThat(entity.systolic).isEqualTo(120.0)
                assertThat(entity.diastolic).isEqualTo(80.0)
                assertThat(entity.hcUid).isEqualTo(hcUid)
            }
        )
    }

    @Test
    fun testWorker_processesOxygenSaturationRecord_movesToCompleted_andSavesData() = runBlocking {
        val hcUid = "oxygen-saturation-test-id-${UUID.randomUUID()}"
        val testRecord = AvroOxygenSaturationRecord(
            metadata = AvroMetadata(id = hcUid, dataOriginPackageName = "test.package", lastModifiedTimeEpochMillis = System.currentTimeMillis(), clientRecordId = "client-oxygen-saturation", clientRecordVersion = 1L, device = AvroDevice("Test Manufacturer", "Test Model", "Test Type")),
            timeEpochMillis = System.currentTimeMillis(),
            zoneOffsetId = null,
            percentage = 98.0,
            appRecordFetchTimeEpochMillis = System.currentTimeMillis()
        )
        testSingleRecordTypeProcessing(
            avroRecord = testRecord,
            hcUid = hcUid,
            fileNamePrefix = "OxygenSaturationRecord",
            daoQueryFunction = { id -> database.oxygenSaturationRecordDao().getRecordByHcUid(id) },
            verificationFunction = { entity: OxygenSaturationRecordEntity ->
                assertThat(entity.percentage).isEqualTo(98.0)
                assertThat(entity.hcUid).isEqualTo(hcUid)
            }
        )
    }

    @Test
    fun testWorker_processesRespiratoryRateRecord_movesToCompleted_andSavesData() = runBlocking {
        val hcUid = "respiratory-rate-test-id-${UUID.randomUUID()}"
        val testRecord = AvroRespiratoryRateRecord(
            metadata = AvroMetadata(id = hcUid, dataOriginPackageName = "test.package", lastModifiedTimeEpochMillis = System.currentTimeMillis(), clientRecordId = "client-respiratory-rate", clientRecordVersion = 1L, device = AvroDevice("Test Manufacturer", "Test Model", "Test Type")),
            timeEpochMillis = System.currentTimeMillis(),
            zoneOffsetId = null,
            rate = 16.0,
            appRecordFetchTimeEpochMillis = System.currentTimeMillis()
        )
        testSingleRecordTypeProcessing(
            avroRecord = testRecord,
            hcUid = hcUid,
            fileNamePrefix = "RespiratoryRateRecord",
            daoQueryFunction = { id -> database.respiratoryRateRecordDao().getRecordByHcUid(id) },
            verificationFunction = { entity: RespiratoryRateRecordEntity ->
                assertThat(entity.rate).isEqualTo(16.0)
                assertThat(entity.hcUid).isEqualTo(hcUid)
            }
        )
    }

    @Test
    fun testWorker_processesSleepSessionRecord_movesToCompleted_andSavesData() = runBlocking {
        val hcUid = "sleep-test-id-${UUID.randomUUID()}"
        val testRecord = AvroSleepSessionRecord(
            metadata = AvroMetadata(id = hcUid, dataOriginPackageName = "test.package", lastModifiedTimeEpochMillis = System.currentTimeMillis(), clientRecordId = "client-sleep", clientRecordVersion = 1L, device = AvroDevice("Test Manufacturer", "Test Model", "Test Type")),
            title = "Night Sleep",
            notes = "Good sleep",
            startTimeEpochMillis = 1L,
            endTimeEpochMillis = 2L,
            startZoneOffsetId = null,
            endZoneOffsetId = null,
            appRecordFetchTimeEpochMillis = System.currentTimeMillis(),
            stages = listOf(
                AvroSleepStageRecord(startTimeEpochMillis = 1L, endTimeEpochMillis = 100L, stage = AvroSleepStageType.AWAKE),
                AvroSleepStageRecord(startTimeEpochMillis = 101L, endTimeEpochMillis = 200L, stage = AvroSleepStageType.DEEP)
            )
        )
        testSleepSessionRecordProcessing(testRecord)
    }

    @Test
    fun testWorker_processesHeartRateRecord_movesToCompleted_andSavesData() = runBlocking {
        val hcUid = "heart-rate-test-id-${UUID.randomUUID()}"
        val testRecord = AvroHeartRateRecord(
            metadata = AvroMetadata(id = hcUid, dataOriginPackageName = "test.package", lastModifiedTimeEpochMillis = System.currentTimeMillis(), clientRecordId = "client-heart-rate", clientRecordVersion = 1L, device = AvroDevice("Test Manufacturer", "Test Model", "Test Type")),
            startTimeEpochMillis = 1L,
            endTimeEpochMillis = 2L,
            startZoneOffsetId = null,
            endZoneOffsetId = null,
            appRecordFetchTimeEpochMillis = System.currentTimeMillis(),
            samples = listOf(AvroHeartRateSample(1L, 75L), AvroHeartRateSample(2L, 80L))
        )
        testHeartRateRecordProcessing(testRecord)
    }

    @Test
    fun testWorker_processesStepsCadenceRecord_movesToCompleted_andSavesData() = runBlocking {
        val hcUid = "steps-cadence-test-id-${UUID.randomUUID()}"
        val testRecord = AvroStepsCadenceRecord(
            metadata = AvroMetadata(id = hcUid, dataOriginPackageName = "test.package", lastModifiedTimeEpochMillis = System.currentTimeMillis(), clientRecordId = "client-steps-cadence", clientRecordVersion = 1L, device = AvroDevice("Test Manufacturer", "Test Model", "Test Type")),
            startTimeEpochMillis = 1L,
            endTimeEpochMillis = 2L,
            startZoneOffsetId = null,
            endZoneOffsetId = null,
            appRecordFetchTimeEpochMillis = System.currentTimeMillis(),
            samples = listOf(AvroStepsCadenceSample(1L, 100.0), AvroStepsCadenceSample(2L, 110.0))
        )
        testStepsCadenceRecordProcessing(testRecord)
    }

    @Test
    fun testWorker_processesSpeedRecord_movesToCompleted_andSavesData() = runBlocking {
        val hcUid = "speed-test-id-${UUID.randomUUID()}"
        val testRecord = AvroSpeedRecord(
            metadata = AvroMetadata(id = hcUid, dataOriginPackageName = "test.package", lastModifiedTimeEpochMillis = System.currentTimeMillis(), clientRecordId = "client-speed", clientRecordVersion = 1L, device = AvroDevice("Test Manufacturer", "Test Model", "Test Type")),
            startTimeEpochMillis = 1L,
            endTimeEpochMillis = 2L,
            startZoneOffsetId = null,
            endZoneOffsetId = null,
            appRecordFetchTimeEpochMillis = System.currentTimeMillis(),
            samples = listOf(AvroSpeedSample(1L, 5.0), AvroSpeedSample(2L, 5.5))
        )
        testSpeedRecordProcessing(testRecord)
    }

    @Test
    fun testWorker_processesPowerRecord_movesToCompleted_andSavesData() = runBlocking {
        val hcUid = "power-test-id-${UUID.randomUUID()}"
        val testRecord = AvroPowerRecord(
            metadata = AvroMetadata(id = hcUid, dataOriginPackageName = "test.package", lastModifiedTimeEpochMillis = System.currentTimeMillis(), clientRecordId = "client-power", clientRecordVersion = 1L, device = AvroDevice("Test Manufacturer", "Test Model", "Test Type")),
            startTimeEpochMillis = 1L,
            endTimeEpochMillis = 2L,
            startZoneOffsetId = null,
            endZoneOffsetId = null,
            appRecordFetchTimeEpochMillis = System.currentTimeMillis(),
            samples = listOf(AvroPowerSample(1L, 150.0), AvroPowerSample(2L, 160.0))
        )
        testPowerRecordProcessing(testRecord)
    }

    @Test
    fun testWorker_processesCyclingPedalingCadenceRecord_movesToCompleted_andSavesData() = runBlocking {
        val hcUid = "cycling-cadence-test-id-${UUID.randomUUID()}"
        val testRecord = AvroCyclingPedalingCadenceRecord(
            metadata = AvroMetadata(id = hcUid, dataOriginPackageName = "test.package", lastModifiedTimeEpochMillis = System.currentTimeMillis(), clientRecordId = "client-cycling-cadence", clientRecordVersion = 1L, device = AvroDevice("Test Manufacturer", "Test Model", "Test Type")),
            startTimeEpochMillis = 1L,
            endTimeEpochMillis = 2L,
            startZoneOffsetId = null,
            endZoneOffsetId = null,
            appRecordFetchTimeEpochMillis = System.currentTimeMillis(),
            samples = listOf(AvroCyclingPedalingCadenceSample(1L, 80.0), AvroCyclingPedalingCadenceSample(2L, 85.0))
        )
        testCyclingPedalingCadenceRecordProcessing(testRecord)
    }

    @Test
    fun testWorker_emptyStagingDirectory_succeeds() = runBlocking {
        val worker = TestListenableWorkerBuilder<AvroFileProcessorWorker>(context)
            .setWorkerFactory(workerFactory)
            .build()
        val result = worker.doWork()
        assertThat(result).isEqualTo(ListenableWorker.Result.success())
    }
}