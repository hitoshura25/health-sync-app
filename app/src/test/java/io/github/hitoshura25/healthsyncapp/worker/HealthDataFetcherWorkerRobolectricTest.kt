package io.github.hitoshura25.healthsyncapp.worker

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.BasalBodyTemperatureRecord
import androidx.health.connect.client.records.BasalMetabolicRateRecord
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.BodyTemperatureMeasurementLocation.MEASUREMENT_LOCATION_MOUTH
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
import androidx.health.connect.client.records.MealType.MEAL_TYPE_BREAKFAST
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
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.response.ReadRecordsResponse
import androidx.health.connect.client.units.BloodGlucose
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.units.Length
import androidx.health.connect.client.units.Mass
import androidx.health.connect.client.units.Percentage
import androidx.health.connect.client.units.Power
import androidx.health.connect.client.units.Pressure
import androidx.health.connect.client.units.Temperature
import androidx.health.connect.client.units.Velocity
import androidx.health.connect.client.units.Volume
import androidx.hilt.work.HiltWorkerFactory
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import com.github.avrokotlin.avro4k.ExperimentalAvro4kApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import io.github.hitoshura25.healthsyncapp.data.HealthConnectConstants
import io.github.hitoshura25.healthsyncapp.di.HealthConnectModule
import io.github.hitoshura25.healthsyncapp.file.FileHandler
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.IOException
import java.time.Instant
import java.time.ZoneOffset
import java.util.Locale.getDefault
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

@UninstallModules(HealthConnectModule::class)
@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class, manifest = Config.NONE, sdk = [Build.VERSION_CODES.P])
class HealthDataFetcherWorkerRobolectricTest {

    @Module
    @InstallIn(SingletonComponent::class)
    object TestHealthConnectClientModule {
        lateinit var mockClient: HealthConnectClient

        @Provides
        @Singleton
        fun provideMockHealthConnectClient(): HealthConnectClient {
            return mockClient
        }
    }

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Mock
    lateinit var mockHealthConnectClient: HealthConnectClient

    @Inject
    lateinit var fileHandler: FileHandler // Using actual FileHandler injected by Hilt


    private lateinit var appContext: Context
    private lateinit var mockedLog: MockedStatic<Log>
    private lateinit var mockedInstant: MockedStatic<Instant>

    @Mock
    private lateinit var mockPermissionController: PermissionController

    private val FIXED_INSTANT = Instant.ofEpochMilli(1678886400000L)

    private lateinit var synchronousExecutor: Executor

    private val allDataPermissions = HealthConnectConstants.RECORD_PERMISSIONS

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        TestHealthConnectClientModule.mockClient = mockHealthConnectClient
        hiltRule.inject() // fileHandler is injected here

        appContext = ApplicationProvider.getApplicationContext<HiltTestApplication>()
        synchronousExecutor = Executors.newSingleThreadExecutor()

        val config = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(synchronousExecutor)
            .setTaskExecutor(synchronousExecutor)
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(appContext, config)

        mockedLog = Mockito.mockStatic(Log::class.java)
        `when`(
            Log.d(
                Mockito.anyString(),
                Mockito.anyString()
            )
        ).thenAnswer { println("LOG D: ${it.arguments[1]}"); 0 }
        `when`(
            Log.i(
                Mockito.anyString(),
                Mockito.anyString()
            )
        ).thenAnswer { println("LOG I: ${it.arguments[1]}"); 0 }
        `when`(
            Log.w(
                Mockito.anyString(),
                Mockito.anyString()
            )
        ).thenAnswer { System.err.println("LOG W: ${it.arguments[1]}"); 0 }
        `when`(
            Log.e(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.any(Throwable::class.java)
            )
        ).thenAnswer { System.err.println("LOG E: ${it.arguments[1]} | ${it.arguments[2]}"); 0 }

        mockedInstant = Mockito.mockStatic(Instant::class.java, Mockito.CALLS_REAL_METHODS)
        `when`(Instant.now()).thenReturn(FIXED_INSTANT)

        `when`(mockHealthConnectClient.permissionController).thenReturn(mockPermissionController)

        val stagingDir = fileHandler.getStagingDirectory()
        if (stagingDir.exists()) stagingDir.deleteRecursively()
        stagingDir.mkdirs()

        val completedDir = fileHandler.getCompletedDirectory()
        if (completedDir.exists()) completedDir.deleteRecursively()
        completedDir.mkdirs()

        runBlocking {
            // Mock empty responses for all record types in setup()
            `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<BloodGlucoseRecord>? -> req != null && req.recordType == BloodGlucoseRecord::class }))
                .thenReturn(ReadRecordsResponse(emptyList(), null))
            `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<ActiveCaloriesBurnedRecord>? -> req != null && req.recordType == ActiveCaloriesBurnedRecord::class }))
                .thenReturn(ReadRecordsResponse(emptyList(), null))
            `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<BasalBodyTemperatureRecord>? -> req != null && req.recordType == BasalBodyTemperatureRecord::class }))
                .thenReturn(ReadRecordsResponse(emptyList(), null))
            `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<BasalMetabolicRateRecord>? -> req != null && req.recordType == BasalMetabolicRateRecord::class }))
                .thenReturn(ReadRecordsResponse(emptyList(), null))
            `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<BloodPressureRecord>? -> req != null && req.recordType == BloodPressureRecord::class }))
                .thenReturn(ReadRecordsResponse(emptyList(), null))
            `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<BodyFatRecord>? -> req != null && req.recordType == BodyFatRecord::class }))
                .thenReturn(ReadRecordsResponse(emptyList(), null))
            `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<BodyTemperatureRecord>? -> req != null && req.recordType == BodyTemperatureRecord::class }))
                .thenReturn(ReadRecordsResponse(emptyList(), null))
            `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<BodyWaterMassRecord>? -> req != null && req.recordType == BodyWaterMassRecord::class }))
                .thenReturn(ReadRecordsResponse(emptyList(), null))
            `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<BoneMassRecord>? -> req != null && req.recordType == BoneMassRecord::class }))
                .thenReturn(ReadRecordsResponse(emptyList(), null))
            `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<CyclingPedalingCadenceRecord>? -> req != null && req.recordType == CyclingPedalingCadenceRecord::class }))
                .thenReturn(ReadRecordsResponse(emptyList(), null))
            `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<DistanceRecord>? -> req != null && req.recordType == DistanceRecord::class }))
                .thenReturn(ReadRecordsResponse(emptyList(), null))
            `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<ElevationGainedRecord>? -> req != null && req.recordType == ElevationGainedRecord::class }))
                .thenReturn(ReadRecordsResponse(emptyList(), null))
            `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<ExerciseSessionRecord>? -> req != null && req.recordType == ExerciseSessionRecord::class }))
                .thenReturn(ReadRecordsResponse(emptyList(), null))
            `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<FloorsClimbedRecord>? -> req != null && req.recordType == FloorsClimbedRecord::class }))
                .thenReturn(ReadRecordsResponse(emptyList(), null))
            `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<HeartRateRecord>? -> req != null && req.recordType == HeartRateRecord::class }))
                .thenReturn(ReadRecordsResponse(emptyList(), null))
            `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<HeartRateVariabilityRmssdRecord>? -> req != null && req.recordType == HeartRateVariabilityRmssdRecord::class }))
                .thenReturn(ReadRecordsResponse(emptyList(), null))
            `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<HeightRecord>? -> req != null && req.recordType == HeightRecord::class }))
                .thenReturn(ReadRecordsResponse(emptyList(), null))
            `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<HydrationRecord>? -> req != null && req.recordType == HydrationRecord::class }))
                .thenReturn(ReadRecordsResponse(emptyList(), null))
            `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<LeanBodyMassRecord>? -> req != null && req.recordType == LeanBodyMassRecord::class }))
                .thenReturn(ReadRecordsResponse(emptyList(), null))
            `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<NutritionRecord>? -> req != null && req.recordType == NutritionRecord::class }))
                .thenReturn(ReadRecordsResponse(emptyList(), null))
            `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<OxygenSaturationRecord>? -> req != null && req.recordType == OxygenSaturationRecord::class }))
                .thenReturn(ReadRecordsResponse(emptyList(), null))
            `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<PowerRecord>? -> req != null && req.recordType == PowerRecord::class }))
                .thenReturn(ReadRecordsResponse(emptyList(), null))
            `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<RespiratoryRateRecord>? -> req != null && req.recordType == RespiratoryRateRecord::class }))
                .thenReturn(ReadRecordsResponse(emptyList(), null))
            `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<RestingHeartRateRecord>? -> req != null && req.recordType == RestingHeartRateRecord::class }))
                .thenReturn(ReadRecordsResponse(emptyList(), null))
            `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<SleepSessionRecord>? -> req != null && req.recordType == SleepSessionRecord::class }))
                .thenReturn(ReadRecordsResponse(emptyList(), null))
            `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<SpeedRecord>? -> req != null && req.recordType == SpeedRecord::class }))
                .thenReturn(ReadRecordsResponse(emptyList(), null))
            `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<StepsCadenceRecord>? -> req != null && req.recordType == StepsCadenceRecord::class }))
                .thenReturn(ReadRecordsResponse(emptyList(), null))
            `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<StepsRecord>? -> req != null && req.recordType == StepsRecord::class }))
                .thenReturn(ReadRecordsResponse(emptyList(), null))
            `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<TotalCaloriesBurnedRecord>? -> req != null && req.recordType == TotalCaloriesBurnedRecord::class }))
                .thenReturn(ReadRecordsResponse(emptyList(), null))
            `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<Vo2MaxRecord>? -> req != null && req.recordType == Vo2MaxRecord::class }))
                .thenReturn(ReadRecordsResponse(emptyList(), null))
            `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<WeightRecord>? -> req != null && req.recordType == WeightRecord::class }))
                .thenReturn(ReadRecordsResponse(emptyList(), null))
        }
    }

    @After
    fun tearDown() {
        mockedLog.close()
        mockedInstant.close()
        val stagingDir = fileHandler.getStagingDirectory()
        if (stagingDir.exists()) {
            stagingDir.deleteRecursively()
        }
        val completedDir = fileHandler.getCompletedDirectory()
        if (completedDir.exists()) {
            completedDir.deleteRecursively()
        }
    }

    private fun createWorker(): HealthDataFetcherWorker {
        return TestListenableWorkerBuilder<HealthDataFetcherWorker>(appContext)
            .setWorkerFactory(workerFactory)
            .build()
    }

    @OptIn(ExperimentalAvro4kApi::class)
    @Test
    fun `doWork processes all data types and writes multiple files successfully and enqueues processor`() = runBlocking {
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(HealthConnectConstants.RECORD_PERMISSIONS)
        val testEndTime = FIXED_INSTANT
        val testStartTime = testEndTime.minusSeconds(3600)
        val simulatedFetchedTimeMillis = FIXED_INSTANT.toEpochMilli()

        val hcStepsRecord = StepsRecord(startTime = testStartTime, startZoneOffset = ZoneOffset.UTC, endTime = testEndTime, endZoneOffset = ZoneOffset.UTC, count = 150L, metadata = Metadata.manualEntry("client-steps-all"))
        val stepsResponse = mock<ReadRecordsResponse<StepsRecord>>()
        `when`(stepsResponse.records).thenReturn(listOf(hcStepsRecord))
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<StepsRecord>? -> req != null && req.recordType == StepsRecord::class }))
            .thenReturn(stepsResponse)

        val hcHeartRateRecord = HeartRateRecord(startTime = testStartTime, startZoneOffset = ZoneOffset.UTC, endTime = testEndTime, endZoneOffset = ZoneOffset.UTC, samples = listOf(HeartRateRecord.Sample(FIXED_INSTANT, 70L)), metadata = Metadata.manualEntry("client-hr-all"))
        val hrResponse = mock<ReadRecordsResponse<HeartRateRecord>>()
        `when`(hrResponse.records).thenReturn(listOf(hcHeartRateRecord))
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<HeartRateRecord>? -> req != null && req.recordType == HeartRateRecord::class }))
            .thenReturn(hrResponse)

        val hcSleepSessionRecord = SleepSessionRecord(startTime = testStartTime, startZoneOffset = ZoneOffset.UTC, endTime = testEndTime, endZoneOffset = ZoneOffset.UTC, stages = emptyList(), metadata = Metadata.manualEntry("client-sleep-all"))
        val sleepResponse = mock<ReadRecordsResponse<SleepSessionRecord>>()
        `when`(sleepResponse.records).thenReturn(listOf(hcSleepSessionRecord))
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<SleepSessionRecord>? -> req != null && req.recordType == SleepSessionRecord::class }))
            .thenReturn(sleepResponse)

        val hcBgRecord = BloodGlucoseRecord(time = testStartTime, zoneOffset = ZoneOffset.UTC, level = BloodGlucose.milligramsPerDeciliter(100.0), metadata = Metadata.manualEntry("client-bg-all"))
        val bgResponse = mock<ReadRecordsResponse<BloodGlucoseRecord>>()
        `when`(bgResponse.records).thenReturn(listOf(hcBgRecord))
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<BloodGlucoseRecord>? -> req != null && req.recordType == BloodGlucoseRecord::class }))
            .thenReturn(bgResponse)

        val worker = createWorker()
        val result = worker.doWork()
        assertTrue("Worker should succeed when all types have data.", result is ListenableWorker.Result.Success)

        val stagingDir = fileHandler.getStagingDirectory()
        val stepsFile = File(stagingDir, "StepsRecord_${simulatedFetchedTimeMillis}.avro")
        assertTrue("Expected StepsRecord Avro file should exist and be a file", stepsFile.exists() && stepsFile.isFile)
        val hrFile = File(stagingDir, "HeartRateRecord_${simulatedFetchedTimeMillis}.avro")
        assertTrue("Expected HeartRateRecord Avro file should exist and be a file", hrFile.exists() && hrFile.isFile)
        val sleepFile = File(stagingDir, "SleepSessionRecord_${simulatedFetchedTimeMillis}.avro")
        assertTrue("Expected SleepSessionRecord Avro file should exist and be a file", sleepFile.exists() && sleepFile.isFile)
        val bgFile = File(stagingDir, "BloodGlucoseRecord_${simulatedFetchedTimeMillis}.avro")
        assertTrue("Expected BloodGlucoseRecord Avro file should exist and be a file", bgFile.exists() && bgFile.isFile)

        val workInfos = WorkManager.getInstance(appContext).getWorkInfosForUniqueWork(AvroFileProcessorWorker.WORK_NAME).get()
        assertTrue("AvroFileProcessorWorker should have been enqueued when files written.", workInfos.any { it.state == WorkInfo.State.ENQUEUED })
    }

    @Test
    fun `doWork returns success and does not enqueue processor when no permissions are granted`() = runBlocking {
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(emptySet())

        val worker = createWorker()
        val result = worker.doWork()
        assertTrue("Worker should succeed even if no permissions are granted.", result is ListenableWorker.Result.Success)

        val workInfos = WorkManager.getInstance(appContext).getWorkInfosForUniqueWork(AvroFileProcessorWorker.WORK_NAME).get()
        assertFalse("AvroFileProcessorWorker should NOT have been enqueued when no files written.", workInfos.any { it.state == WorkInfo.State.ENQUEUED })
    }

    @Test
    fun `doWork succeeds and does not enqueue processor when permissions granted but no data`() = runBlocking {
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(HealthConnectConstants.RECORD_PERMISSIONS)

        val worker = createWorker()
        val result = worker.doWork()
        assertTrue("Worker should succeed when permissions are granted but no data is returned.", result is ListenableWorker.Result.Success)

        val workInfos = WorkManager.getInstance(appContext).getWorkInfosForUniqueWork(AvroFileProcessorWorker.WORK_NAME).get()
        assertFalse("AvroFileProcessorWorker should NOT have been enqueued when no files written (empty data).", workInfos.any { it.state == WorkInfo.State.ENQUEUED })
    }

    @OptIn(ExperimentalAvro4kApi::class)
    @Test
    fun `doWork when readRecords fails for one type returns Failure and processes others`() = runBlocking {
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(HealthConnectConstants.RECORD_PERMISSIONS)
        val testEndTime = FIXED_INSTANT
        val testStartTime = testEndTime.minusSeconds(3600)
        val simulatedFetchedTimeMillis = FIXED_INSTANT.toEpochMilli()

        val hcStepsRecord = StepsRecord(
            startTime = testStartTime, startZoneOffset = ZoneOffset.UTC, endTime = testEndTime, endZoneOffset = ZoneOffset.UTC, count = 100L,
            metadata = Metadata.manualEntry(clientRecordId = "steps-error-case")
        )
        val stepsResponse = mock<ReadRecordsResponse<StepsRecord>>()
        `when`(stepsResponse.records).thenReturn(listOf(hcStepsRecord))
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<StepsRecord>? -> req?.recordType == StepsRecord::class }))
            .thenReturn(stepsResponse)

        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<HeartRateRecord>? -> req?.recordType == HeartRateRecord::class }))
            .thenAnswer { throw IOException("Simulated network error reading HeartRate") } // MODIFIED LINE

        val hcBgRecord = BloodGlucoseRecord(
            time = testStartTime, zoneOffset = ZoneOffset.UTC,
            level = BloodGlucose.milligramsPerDeciliter(95.0),
            metadata = Metadata.manualEntry(clientRecordId = "bg-error-case")
        )
        val bgResponse = mock<ReadRecordsResponse<BloodGlucoseRecord>>()
        `when`(bgResponse.records).thenReturn(listOf(hcBgRecord))
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<BloodGlucoseRecord>? -> req?.recordType == BloodGlucoseRecord::class }))
            .thenReturn(bgResponse)

        val worker = createWorker()
        val result = worker.doWork()

        assertTrue("Worker should return Failure when a record type fails to read.", result is ListenableWorker.Result.Failure)

        val stagingDir = fileHandler.getStagingDirectory()
        val stepsFile = File(stagingDir, "StepsRecord_${simulatedFetchedTimeMillis}.avro")
        assertTrue("StepsRecord file should exist and be a file despite later read error", stepsFile.exists() && stepsFile.isFile)
        val bgFile = File(stagingDir, "BloodGlucoseRecord_${simulatedFetchedTimeMillis}.avro")
        assertTrue("BloodGlucoseRecord file should exist and be a file despite earlier read error", bgFile.exists() && bgFile.isFile)

        val hrFile = File(stagingDir, "HeartRateRecord_${simulatedFetchedTimeMillis}.avro")
        assertFalse("HeartRateRecord file should NOT exist as its read failed", hrFile.exists())
        val sleepFile = File(stagingDir, "SleepSessionRecord_${simulatedFetchedTimeMillis}.avro")
        assertFalse("SleepSessionRecord file should NOT exist as it had no data", sleepFile.exists())

        val workInfos = WorkManager.getInstance(appContext).getWorkInfosForUniqueWork(AvroFileProcessorWorker.WORK_NAME).get()
        assertFalse("AvroFileProcessorWorker should NOT be enqueued on overall failure.", workInfos.any { it.state == WorkInfo.State.ENQUEUED })
    }

    @OptIn(ExperimentalAvro4kApi::class)
    @Test
    fun `doWork when fileHandler writeAvroFile fails for one type returns Failure and processes others`() = runBlocking {
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(HealthConnectConstants.RECORD_PERMISSIONS)
        val simulatedFetchedTimeMillis = FIXED_INSTANT.toEpochMilli()
        val stagingDir = fileHandler.getStagingDirectory()

        // Data for all types
        val hcStepsRecord = StepsRecord(FIXED_INSTANT.minusSeconds(100), ZoneOffset.UTC, FIXED_INSTANT, ZoneOffset.UTC, 100L, Metadata.manualEntry("steps-write-fail"))
        val stepsResponse = mock<ReadRecordsResponse<StepsRecord>>()
        `when`(stepsResponse.records).thenReturn(listOf(hcStepsRecord))
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<StepsRecord>? -> req?.recordType == StepsRecord::class })).thenReturn(stepsResponse)

        val hcHeartRateRecord = HeartRateRecord(FIXED_INSTANT.minusSeconds(200), ZoneOffset.UTC, FIXED_INSTANT.minusSeconds(100), ZoneOffset.UTC, listOf(HeartRateRecord.Sample(FIXED_INSTANT.minusSeconds(150), 75L)), Metadata.manualEntry("hr-write-success"))
        val hrResponse = mock<ReadRecordsResponse<HeartRateRecord>>()
        `when`(hrResponse.records).thenReturn(listOf(hcHeartRateRecord))
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<HeartRateRecord>? -> req?.recordType == HeartRateRecord::class })).thenReturn(hrResponse)

        val hcBgRecord = BloodGlucoseRecord(FIXED_INSTANT.minusSeconds(50), ZoneOffset.UTC,Metadata.manualEntry("bg-write-success"), BloodGlucose.milligramsPerDeciliter(90.0))
        val bgResponse = mock<ReadRecordsResponse<BloodGlucoseRecord>>()
        `when`(bgResponse.records).thenReturn(listOf(hcBgRecord))
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<BloodGlucoseRecord>? -> req?.recordType == BloodGlucoseRecord::class })).thenReturn(bgResponse)

        // --- Induce failure for StepsRecord write --- 
        val stepsFileNameToFail = "StepsRecord_${simulatedFetchedTimeMillis}.avro"
        val fileToFail = File(stagingDir, stepsFileNameToFail)
        fileToFail.mkdirs() // Create a directory at the target file path to cause IOException on write
        assertTrue("Directory to cause failure should exist", fileToFail.exists() && fileToFail.isDirectory)

        val worker = createWorker()
        val result = worker.doWork()

        assertTrue("Worker should return Failure when a file write fails.", result is ListenableWorker.Result.Failure)

        // Assertions
        assertTrue("Directory created to cause StepsRecord write failure should still exist (or file write failed before deleting it)", fileToFail.exists() && fileToFail.isDirectory)
        assertFalse("StepsRecord file should not have been written as a file", fileToFail.isFile) // Double check it's not a file

        val hrFile = File(stagingDir, "HeartRateRecord_${simulatedFetchedTimeMillis}.avro")
        assertTrue("HeartRateRecord file should have been written successfully", hrFile.exists() && hrFile.isFile)
        val bgFile = File(stagingDir, "BloodGlucoseRecord_${simulatedFetchedTimeMillis}.avro")
        assertTrue("BloodGlucoseRecord file should have been written successfully", bgFile.exists() && bgFile.isFile)
        val sleepFile = File(stagingDir, "SleepSessionRecord_${simulatedFetchedTimeMillis}.avro")
        assertFalse("SleepSessionRecord file should NOT exist as it had no data", sleepFile.exists())

        val workInfos = WorkManager.getInstance(appContext).getWorkInfosForUniqueWork(AvroFileProcessorWorker.WORK_NAME).get()
        assertFalse("AvroFileProcessorWorker should NOT be enqueued on overall failure (due to write error).", workInfos.any { it.state == WorkInfo.State.ENQUEUED })
    }

    private fun generateSampleRecord(recordType: KClass<out Record>, testStartTime: Instant, testEndTime: Instant): Record {
        val metadata = Metadata.manualEntry(clientRecordId = "client-${recordType.simpleName?.lowercase(
                getDefault()
            )
        }}-id")
        return when (recordType) {
            ActiveCaloriesBurnedRecord::class -> ActiveCaloriesBurnedRecord(
                startTime = testStartTime,
                startZoneOffset = ZoneOffset.UTC,
                endTime = testEndTime,
                endZoneOffset = ZoneOffset.UTC,
                energy = Energy.calories(100.0),
                metadata = metadata
            )
            BasalBodyTemperatureRecord::class -> BasalBodyTemperatureRecord(
                time = testEndTime,
                zoneOffset = ZoneOffset.UTC,
                temperature = Temperature.celsius(36.5),
                metadata = metadata
            )
            BasalMetabolicRateRecord::class -> BasalMetabolicRateRecord(
                time = testEndTime,
                zoneOffset = ZoneOffset.UTC,
                basalMetabolicRate = Power.kilocaloriesPerDay(1500.0),
                metadata = metadata
            )
            BloodGlucoseRecord::class -> BloodGlucoseRecord(
                time = testEndTime,
                zoneOffset = ZoneOffset.UTC,
                level = BloodGlucose.milligramsPerDeciliter(100.0),
                specimenSource = BloodGlucoseRecord.SPECIMEN_SOURCE_CAPILLARY_BLOOD,
                mealType = MEAL_TYPE_BREAKFAST,
                relationToMeal = BloodGlucoseRecord.RELATION_TO_MEAL_BEFORE_MEAL,
                metadata = metadata
            )
            BloodPressureRecord::class -> BloodPressureRecord(
                time = testEndTime,
                zoneOffset = ZoneOffset.UTC,
                systolic = Pressure.millimetersOfMercury(120.0),
                diastolic = Pressure.millimetersOfMercury(80.0),
                bodyPosition = BloodPressureRecord.BODY_POSITION_SITTING_DOWN,
                measurementLocation = BloodPressureRecord.MEASUREMENT_LOCATION_LEFT_WRIST,
                metadata = metadata
            )
            BodyFatRecord::class -> BodyFatRecord(
                time = testEndTime,
                zoneOffset = ZoneOffset.UTC,
                percentage = Percentage(20.0),
                metadata = metadata
            )
            BodyTemperatureRecord::class -> BodyTemperatureRecord(
                time = testEndTime,
                zoneOffset = ZoneOffset.UTC,
                temperature = Temperature.celsius(37.0),
                measurementLocation = MEASUREMENT_LOCATION_MOUTH,
                metadata = metadata
            )
            BodyWaterMassRecord::class -> BodyWaterMassRecord(
                time = testEndTime,
                zoneOffset = ZoneOffset.UTC,
                mass = Mass.kilograms(50.0),
                metadata = metadata
            )
            BoneMassRecord::class -> BoneMassRecord(
                time = testEndTime,
                zoneOffset = ZoneOffset.UTC,
                mass = Mass.kilograms(3.0),
                metadata = metadata
            )
            CyclingPedalingCadenceRecord::class -> CyclingPedalingCadenceRecord(
                startTime = testStartTime,
                startZoneOffset = ZoneOffset.UTC,
                endZoneOffset = ZoneOffset.UTC,
                endTime = testEndTime,
                samples = listOf(CyclingPedalingCadenceRecord.Sample(time = testStartTime.plusSeconds(30), revolutionsPerMinute = 80.0)),
                metadata = metadata
            )
            DistanceRecord::class -> DistanceRecord(
                startTime = testStartTime,
                startZoneOffset = ZoneOffset.UTC,
                endZoneOffset = ZoneOffset.UTC,
                endTime = testEndTime,
                distance = Length.meters(1000.0),
                metadata = metadata
            )
            ElevationGainedRecord::class -> ElevationGainedRecord(
                startTime = testStartTime,
                startZoneOffset = ZoneOffset.UTC,
                endZoneOffset = ZoneOffset.UTC,
                endTime = testEndTime,
                elevation = Length.meters(50.0),
                metadata = metadata
            )
            ExerciseSessionRecord::class -> ExerciseSessionRecord(
                startTime = testStartTime,
                startZoneOffset = ZoneOffset.UTC,
                endZoneOffset = ZoneOffset.UTC,
                endTime = testEndTime,
                exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_RUNNING,
                metadata = metadata
            )
            FloorsClimbedRecord::class -> FloorsClimbedRecord(
                startTime = testStartTime,
                startZoneOffset = ZoneOffset.UTC,
                endZoneOffset = ZoneOffset.UTC,
                endTime = testEndTime,
                floors = 10.0,
                metadata = metadata
            )
            HeartRateRecord::class -> HeartRateRecord(
                startTime = testStartTime,
                startZoneOffset = ZoneOffset.UTC,
                endZoneOffset = ZoneOffset.UTC,
                endTime = testEndTime,
                samples = listOf(HeartRateRecord.Sample(time = testStartTime.plusSeconds(30), beatsPerMinute = 70L)),
                metadata = metadata
            )
            HeartRateVariabilityRmssdRecord::class -> HeartRateVariabilityRmssdRecord(
                time = testEndTime,
                zoneOffset = ZoneOffset.UTC,
                heartRateVariabilityMillis = 50.0,
                metadata = metadata
            )
            HeightRecord::class -> HeightRecord(
                time = testEndTime,
                zoneOffset = ZoneOffset.UTC,
                height = Length.meters(1.75),
                metadata = metadata
            )
            HydrationRecord::class -> HydrationRecord(
                startTime = testStartTime,
                startZoneOffset = ZoneOffset.UTC,
                endTime = testEndTime,
                endZoneOffset = ZoneOffset.UTC,
                volume = Volume.liters(0.5),
                metadata = metadata
            )
            LeanBodyMassRecord::class -> LeanBodyMassRecord(
                time = testEndTime,
                zoneOffset = ZoneOffset.UTC,
                mass = Mass.kilograms(60.0),
                metadata = metadata
            )
            NutritionRecord::class -> NutritionRecord(
                startTime = testStartTime,
                startZoneOffset = ZoneOffset.UTC,
                endTime = testEndTime,
                endZoneOffset = ZoneOffset.UTC,
                name = "Apple",
                energy = Energy.calories(95.0),
                metadata = metadata,
            )
            OxygenSaturationRecord::class -> OxygenSaturationRecord(
                time = testEndTime,
                zoneOffset = ZoneOffset.UTC,
                percentage = Percentage(98.0),
                metadata = metadata
            )
            PowerRecord::class -> PowerRecord(
                startTime = testStartTime,
                startZoneOffset = ZoneOffset.UTC,
                endTime = testEndTime,
                endZoneOffset = ZoneOffset.UTC,
                samples = listOf(PowerRecord.Sample(time = testStartTime.plusSeconds(30), power = Power.watts(150.0))),
                metadata = metadata
            )
            RespiratoryRateRecord::class -> RespiratoryRateRecord(
                time = testEndTime,
                zoneOffset = ZoneOffset.UTC,
                rate = 16.0,
                metadata = metadata
            )
            RestingHeartRateRecord::class -> RestingHeartRateRecord(
                time = testEndTime,
                zoneOffset = ZoneOffset.UTC,
                beatsPerMinute = 60L,
                metadata = metadata
            )
            SleepSessionRecord::class -> SleepSessionRecord(
                startTime = testStartTime,
                startZoneOffset = ZoneOffset.UTC,
                endTime = testEndTime,
                endZoneOffset = ZoneOffset.UTC,
                stages = emptyList(),
                metadata = metadata
            )
            SpeedRecord::class -> SpeedRecord(
                startTime = testStartTime,
                startZoneOffset = ZoneOffset.UTC,
                endTime = testEndTime,
                endZoneOffset = ZoneOffset.UTC,
                samples = listOf(SpeedRecord.Sample(time = testStartTime.plusSeconds(30), speed = Velocity.metersPerSecond(2.0))),
                metadata = metadata
            )
            StepsCadenceRecord::class -> StepsCadenceRecord(
                startTime = testStartTime,
                startZoneOffset = ZoneOffset.UTC,
                endTime = testEndTime,
                endZoneOffset = ZoneOffset.UTC,
                samples = listOf(
                    StepsCadenceRecord.Sample(
                        time = testStartTime.plusSeconds(30),
                        rate = 100.0
                    )
                ),
                metadata = metadata
            )
            StepsRecord::class -> StepsRecord(
                startTime = testStartTime,
                startZoneOffset = ZoneOffset.UTC,
                endTime = testEndTime,
                endZoneOffset = ZoneOffset.UTC,
                count = 1000L,
                metadata = metadata
            )
            TotalCaloriesBurnedRecord::class -> TotalCaloriesBurnedRecord(
                startTime = testStartTime,
                startZoneOffset = ZoneOffset.UTC,
                endTime = testEndTime,
                endZoneOffset = ZoneOffset.UTC,
                energy = Energy.calories(2000.0),
                metadata = metadata
            )
            Vo2MaxRecord::class -> Vo2MaxRecord(
                time = testEndTime,
                zoneOffset = ZoneOffset.UTC,
                vo2MillilitersPerMinuteKilogram = 45.0,
                measurementMethod = Vo2MaxRecord.MEASUREMENT_METHOD_COOPER_TEST,
                metadata = metadata
            )
            WeightRecord::class -> WeightRecord(
                time = testEndTime,
                zoneOffset = ZoneOffset.UTC,
                weight = Mass.kilograms(70.0),
                metadata = metadata
            )
            else -> throw IllegalArgumentException("Unsupported record type: ${recordType.simpleName}")
        }
    }

    // Test case for ActiveCaloriesBurnedRecord
    @OptIn(ExperimentalAvro4kApi::class)
    @Test
    fun `doWork processes ActiveCaloriesBurnedRecord data and writes file successfully and enqueues processor`() = runBlocking {
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(allDataPermissions)
        val testEndTime = FIXED_INSTANT
        val testStartTime = testEndTime.minusSeconds(3600)
        val simulatedFetchedTimeMillis = FIXED_INSTANT.toEpochMilli()

        val hcActiveCaloriesBurnedRecord = generateSampleRecord(ActiveCaloriesBurnedRecord::class, testStartTime, testEndTime) as ActiveCaloriesBurnedRecord
        val activeCaloriesBurnedRecords = listOf(hcActiveCaloriesBurnedRecord)
        val activeCaloriesBurnedResponse = mock<ReadRecordsResponse<ActiveCaloriesBurnedRecord>>()
        `when`(activeCaloriesBurnedResponse.records).thenReturn(activeCaloriesBurnedRecords)
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<ActiveCaloriesBurnedRecord>? -> req != null && req.recordType == ActiveCaloriesBurnedRecord::class }))
            .thenReturn(activeCaloriesBurnedResponse)

        val worker = createWorker()
        val result = worker.doWork()
        assertTrue("Worker should succeed.", result is ListenableWorker.Result.Success)

        val stagingDir = fileHandler.getStagingDirectory()
        val expectedFile = File(stagingDir, "ActiveCaloriesBurnedRecord_${simulatedFetchedTimeMillis}.avro")
        assertTrue("Expected ActiveCaloriesBurnedRecord Avro file should exist and be a file", expectedFile.exists() && expectedFile.isFile)

        val workInfos = WorkManager.getInstance(appContext).getWorkInfosForUniqueWork(AvroFileProcessorWorker.WORK_NAME).get()
        assertTrue("AvroFileProcessorWorker should have been enqueued.", workInfos.any { it.state == WorkInfo.State.ENQUEUED })
    }

    // Test case for BasalBodyTemperatureRecord
    @OptIn(ExperimentalAvro4kApi::class)
    @Test
    fun `doWork processes BasalBodyTemperatureRecord data and writes file successfully and enqueues processor`() = runBlocking {
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(allDataPermissions)
        val testEndTime = FIXED_INSTANT
        val testStartTime = testEndTime.minusSeconds(3600)
        val simulatedFetchedTimeMillis = FIXED_INSTANT.toEpochMilli()

        val hcBasalBodyTemperatureRecord = generateSampleRecord(BasalBodyTemperatureRecord::class, testStartTime, testEndTime) as BasalBodyTemperatureRecord
        val basalBodyTemperatureRecords = listOf(hcBasalBodyTemperatureRecord)
        val basalBodyTemperatureResponse = mock<ReadRecordsResponse<BasalBodyTemperatureRecord>>()
        `when`(basalBodyTemperatureResponse.records).thenReturn(basalBodyTemperatureRecords)
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<BasalBodyTemperatureRecord>? -> req != null && req.recordType == BasalBodyTemperatureRecord::class }))
            .thenReturn(basalBodyTemperatureResponse)

        val worker = createWorker()
        val result = worker.doWork()
        assertTrue("Worker should succeed.", result is ListenableWorker.Result.Success)

        val stagingDir = fileHandler.getStagingDirectory()
        val expectedFile = File(stagingDir, "BasalBodyTemperatureRecord_${simulatedFetchedTimeMillis}.avro")
        assertTrue("Expected BasalBodyTemperatureRecord Avro file should exist and be a file", expectedFile.exists() && expectedFile.isFile)

        val workInfos = WorkManager.getInstance(appContext).getWorkInfosForUniqueWork(AvroFileProcessorWorker.WORK_NAME).get()
        assertTrue("AvroFileProcessorWorker should have been enqueued.", workInfos.any { it.state == WorkInfo.State.ENQUEUED })
    }

    // Test case for BasalMetabolicRateRecord
    @OptIn(ExperimentalAvro4kApi::class)
    @Test
    fun `doWork processes BasalMetabolicRateRecord data and writes file successfully and enqueues processor`() = runBlocking {
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(allDataPermissions)
        val testEndTime = FIXED_INSTANT
        val testStartTime = testEndTime.minusSeconds(3600)
        val simulatedFetchedTimeMillis = FIXED_INSTANT.toEpochMilli()

        val hcBasalMetabolicRateRecord = generateSampleRecord(BasalMetabolicRateRecord::class, testStartTime, testEndTime) as BasalMetabolicRateRecord
        val basalMetabolicRateRecords = listOf(hcBasalMetabolicRateRecord)
        val basalMetabolicRateResponse = mock<ReadRecordsResponse<BasalMetabolicRateRecord>>()
        `when`(basalMetabolicRateResponse.records).thenReturn(basalMetabolicRateRecords)
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<BasalMetabolicRateRecord>? -> req != null && req.recordType == BasalMetabolicRateRecord::class }))
            .thenReturn(basalMetabolicRateResponse)

        val worker = createWorker()
        val result = worker.doWork()
        assertTrue("Worker should succeed.", result is ListenableWorker.Result.Success)

        val stagingDir = fileHandler.getStagingDirectory()
        val expectedFile = File(stagingDir, "BasalMetabolicRateRecord_${simulatedFetchedTimeMillis}.avro")
        assertTrue("Expected BasalMetabolicRateRecord Avro file should exist and be a file", expectedFile.exists() && expectedFile.isFile)

        val workInfos = WorkManager.getInstance(appContext).getWorkInfosForUniqueWork(AvroFileProcessorWorker.WORK_NAME).get()
        assertTrue("AvroFileProcessorWorker should have been enqueued.", workInfos.any { it.state == WorkInfo.State.ENQUEUED })
    }

    // Test case for BloodGlucoseRecord
    @OptIn(ExperimentalAvro4kApi::class)
    @Test
    fun `doWork processes BloodGlucoseRecord data and writes file successfully and enqueues processor`() = runBlocking {
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(allDataPermissions)
        val testEndTime = FIXED_INSTANT
        val testStartTime = testEndTime.minusSeconds(3600)
        val simulatedFetchedTimeMillis = FIXED_INSTANT.toEpochMilli()

        val hcBloodGlucoseRecord = generateSampleRecord(BloodGlucoseRecord::class, testStartTime, testEndTime) as BloodGlucoseRecord
        val bloodGlucoseRecords = listOf(hcBloodGlucoseRecord)
        val bloodGlucoseResponse = mock<ReadRecordsResponse<BloodGlucoseRecord>>()
        `when`(bloodGlucoseResponse.records).thenReturn(bloodGlucoseRecords)
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<BloodGlucoseRecord>? -> req != null && req.recordType == BloodGlucoseRecord::class }))
            .thenReturn(bloodGlucoseResponse)

        val worker = createWorker()
        val result = worker.doWork()
        assertTrue("Worker should succeed.", result is ListenableWorker.Result.Success)

        val stagingDir = fileHandler.getStagingDirectory()
        val expectedFile = File(stagingDir, "BloodGlucoseRecord_${simulatedFetchedTimeMillis}.avro")
        assertTrue("Expected BloodGlucoseRecord Avro file should exist and be a file", expectedFile.exists() && expectedFile.isFile)

        val workInfos = WorkManager.getInstance(appContext).getWorkInfosForUniqueWork(AvroFileProcessorWorker.WORK_NAME).get()
        assertTrue("AvroFileProcessorWorker should have been enqueued.", workInfos.any { it.state == WorkInfo.State.ENQUEUED })
    }

    // Test case for BloodPressureRecord
    @OptIn(ExperimentalAvro4kApi::class)
    @Test
    fun `doWork processes BloodPressureRecord data and writes file successfully and enqueues processor`() = runBlocking {
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(allDataPermissions)
        val testEndTime = FIXED_INSTANT
        val testStartTime = testEndTime.minusSeconds(3600)
        val simulatedFetchedTimeMillis = FIXED_INSTANT.toEpochMilli()

        val hcBloodPressureRecord = generateSampleRecord(BloodPressureRecord::class, testStartTime, testEndTime) as BloodPressureRecord
        val bloodPressureRecords = listOf(hcBloodPressureRecord)
        val bloodPressureResponse = mock<ReadRecordsResponse<BloodPressureRecord>>()
        `when`(bloodPressureResponse.records).thenReturn(bloodPressureRecords)
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<BloodPressureRecord>? -> req != null && req.recordType == BloodPressureRecord::class }))
            .thenReturn(bloodPressureResponse)

        val worker = createWorker()
        val result = worker.doWork()
        assertTrue("Worker should succeed.", result is ListenableWorker.Result.Success)

        val stagingDir = fileHandler.getStagingDirectory()
        val expectedFile = File(stagingDir, "BloodPressureRecord_${simulatedFetchedTimeMillis}.avro")
        assertTrue("Expected BloodPressureRecord Avro file should exist and be a file", expectedFile.exists() && expectedFile.isFile)

        val workInfos = WorkManager.getInstance(appContext).getWorkInfosForUniqueWork(AvroFileProcessorWorker.WORK_NAME).get()
        assertTrue("AvroFileProcessorWorker should have been enqueued.", workInfos.any { it.state == WorkInfo.State.ENQUEUED })
    }

    // Test case for BodyFatRecord
    @OptIn(ExperimentalAvro4kApi::class)
    @Test
    fun `doWork processes BodyFatRecord data and writes file successfully and enqueues processor`() = runBlocking {
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(allDataPermissions)
        val testEndTime = FIXED_INSTANT
        val testStartTime = testEndTime.minusSeconds(3600)
        val simulatedFetchedTimeMillis = FIXED_INSTANT.toEpochMilli()

        val hcBodyFatRecord = generateSampleRecord(BodyFatRecord::class, testStartTime, testEndTime) as BodyFatRecord
        val bodyFatRecords = listOf(hcBodyFatRecord)
        val bodyFatResponse = mock<ReadRecordsResponse<BodyFatRecord>>()
        `when`(bodyFatResponse.records).thenReturn(bodyFatRecords)
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<BodyFatRecord>? -> req != null && req.recordType == BodyFatRecord::class }))
            .thenReturn(bodyFatResponse)

        val worker = createWorker()
        val result = worker.doWork()
        assertTrue("Worker should succeed.", result is ListenableWorker.Result.Success)

        val stagingDir = fileHandler.getStagingDirectory()
        val expectedFile = File(stagingDir, "BodyFatRecord_${simulatedFetchedTimeMillis}.avro")
        assertTrue("Expected BodyFatRecord Avro file should exist and be a file", expectedFile.exists() && expectedFile.isFile)

        val workInfos = WorkManager.getInstance(appContext).getWorkInfosForUniqueWork(AvroFileProcessorWorker.WORK_NAME).get()
        assertTrue("AvroFileProcessorWorker should have been enqueued.", workInfos.any { it.state == WorkInfo.State.ENQUEUED })
    }

    // Test case for BodyTemperatureRecord
    @OptIn(ExperimentalAvro4kApi::class)
    @Test
    fun `doWork processes BodyTemperatureRecord data and writes file successfully and enqueues processor`() = runBlocking {
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(allDataPermissions)
        val testEndTime = FIXED_INSTANT
        val testStartTime = testEndTime.minusSeconds(3600)
        val simulatedFetchedTimeMillis = FIXED_INSTANT.toEpochMilli()

        val hcBodyTemperatureRecord = generateSampleRecord(BodyTemperatureRecord::class, testStartTime, testEndTime) as BodyTemperatureRecord
        val bodyTemperatureRecords = listOf(hcBodyTemperatureRecord)
        val bodyTemperatureResponse = mock<ReadRecordsResponse<BodyTemperatureRecord>>()
        `when`(bodyTemperatureResponse.records).thenReturn(bodyTemperatureRecords)
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<BodyTemperatureRecord>? -> req != null && req.recordType == BodyTemperatureRecord::class }))
            .thenReturn(bodyTemperatureResponse)

        val worker = createWorker()
        val result = worker.doWork()
        assertTrue("Worker should succeed.", result is ListenableWorker.Result.Success)

        val stagingDir = fileHandler.getStagingDirectory()
        val expectedFile = File(stagingDir, "BodyTemperatureRecord_${simulatedFetchedTimeMillis}.avro")
        assertTrue("Expected BodyTemperatureRecord Avro file should exist and be a file", expectedFile.exists() && expectedFile.isFile)

        val workInfos = WorkManager.getInstance(appContext).getWorkInfosForUniqueWork(AvroFileProcessorWorker.WORK_NAME).get()
        assertTrue("AvroFileProcessorWorker should have been enqueued.", workInfos.any { it.state == WorkInfo.State.ENQUEUED })
    }

    // Test case for BodyWaterMassRecord
    @OptIn(ExperimentalAvro4kApi::class)
    @Test
    fun `doWork processes BodyWaterMassRecord data and writes file successfully and enqueues processor`() = runBlocking {
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(allDataPermissions)
        val testEndTime = FIXED_INSTANT
        val testStartTime = testEndTime.minusSeconds(3600)
        val simulatedFetchedTimeMillis = FIXED_INSTANT.toEpochMilli()

        val hcBodyWaterMassRecord = generateSampleRecord(BodyWaterMassRecord::class, testStartTime, testEndTime) as BodyWaterMassRecord
        val bodyWaterMassRecords = listOf(hcBodyWaterMassRecord)
        val bodyWaterMassResponse = mock<ReadRecordsResponse<BodyWaterMassRecord>>()
        `when`(bodyWaterMassResponse.records).thenReturn(bodyWaterMassRecords)
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<BodyWaterMassRecord>? -> req != null && req.recordType == BodyWaterMassRecord::class }))
            .thenReturn(bodyWaterMassResponse)

        val worker = createWorker()
        val result = worker.doWork()
        assertTrue("Worker should succeed.", result is ListenableWorker.Result.Success)

        val stagingDir = fileHandler.getStagingDirectory()
        val expectedFile = File(stagingDir, "BodyWaterMassRecord_${simulatedFetchedTimeMillis}.avro")
        assertTrue("Expected BodyWaterMassRecord Avro file should exist and be a file", expectedFile.exists() && expectedFile.isFile)

        val workInfos = WorkManager.getInstance(appContext).getWorkInfosForUniqueWork(AvroFileProcessorWorker.WORK_NAME).get()
        assertTrue("AvroFileProcessorWorker should have been enqueued.", workInfos.any { it.state == WorkInfo.State.ENQUEUED })
    }

    // Test case for BoneMassRecord
    @OptIn(ExperimentalAvro4kApi::class)
    @Test
    fun `doWork processes BoneMassRecord data and writes file successfully and enqueues processor`() = runBlocking {
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(allDataPermissions)
        val testEndTime = FIXED_INSTANT
        val testStartTime = testEndTime.minusSeconds(3600)
        val simulatedFetchedTimeMillis = FIXED_INSTANT.toEpochMilli()

        val hcBoneMassRecord = generateSampleRecord(BoneMassRecord::class, testStartTime, testEndTime) as BoneMassRecord
        val boneMassRecords = listOf(hcBoneMassRecord)
        val boneMassResponse = mock<ReadRecordsResponse<BoneMassRecord>>()
        `when`(boneMassResponse.records).thenReturn(boneMassRecords)
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<BoneMassRecord>? -> req != null && req.recordType == BoneMassRecord::class }))
            .thenReturn(boneMassResponse)

        val worker = createWorker()
        val result = worker.doWork()
        assertTrue("Worker should succeed.", result is ListenableWorker.Result.Success)

        val stagingDir = fileHandler.getStagingDirectory()
        val expectedFile = File(stagingDir, "BoneMassRecord_${simulatedFetchedTimeMillis}.avro")
        assertTrue("Expected BoneMassRecord Avro file should exist and be a file", expectedFile.exists() && expectedFile.isFile)

        val workInfos = WorkManager.getInstance(appContext).getWorkInfosForUniqueWork(AvroFileProcessorWorker.WORK_NAME).get()
        assertTrue("AvroFileProcessorWorker should have been enqueued.", workInfos.any { it.state == WorkInfo.State.ENQUEUED })
    }

    // Test case for CyclingPedalingCadenceRecord
    @OptIn(ExperimentalAvro4kApi::class)
    @Test
    fun `doWork processes CyclingPedalingCadenceRecord data and writes file successfully and enqueues processor`() = runBlocking {
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(allDataPermissions)
        val testEndTime = FIXED_INSTANT
        val testStartTime = testEndTime.minusSeconds(3600)
        val simulatedFetchedTimeMillis = FIXED_INSTANT.toEpochMilli()

        val hcCyclingPedalingCadenceRecord = generateSampleRecord(CyclingPedalingCadenceRecord::class, testStartTime, testEndTime) as CyclingPedalingCadenceRecord
        val cyclingPedalingCadenceRecords = listOf(hcCyclingPedalingCadenceRecord)
        val cyclingPedalingCadenceResponse = mock<ReadRecordsResponse<CyclingPedalingCadenceRecord>>()
        `when`(cyclingPedalingCadenceResponse.records).thenReturn(cyclingPedalingCadenceRecords)
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<CyclingPedalingCadenceRecord>? -> req != null && req.recordType == CyclingPedalingCadenceRecord::class }))
            .thenReturn(cyclingPedalingCadenceResponse)

        val worker = createWorker()
        val result = worker.doWork()
        assertTrue("Worker should succeed.", result is ListenableWorker.Result.Success)

        val stagingDir = fileHandler.getStagingDirectory()
        val expectedFile = File(stagingDir, "CyclingPedalingCadenceRecord_${simulatedFetchedTimeMillis}.avro")
        assertTrue("Expected CyclingPedalingCadenceRecord Avro file should exist and be a file", expectedFile.exists() && expectedFile.isFile)

        val workInfos = WorkManager.getInstance(appContext).getWorkInfosForUniqueWork(AvroFileProcessorWorker.WORK_NAME).get()
        assertTrue("AvroFileProcessorWorker should have been enqueued.", workInfos.any { it.state == WorkInfo.State.ENQUEUED })
    }

    // Test case for DistanceRecord
    @OptIn(ExperimentalAvro4kApi::class)
    @Test
    fun `doWork processes DistanceRecord data and writes file successfully and enqueues processor`() = runBlocking {
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(allDataPermissions)
        val testEndTime = FIXED_INSTANT
        val testStartTime = testEndTime.minusSeconds(3600)
        val simulatedFetchedTimeMillis = FIXED_INSTANT.toEpochMilli()

        val hcDistanceRecord = generateSampleRecord(DistanceRecord::class, testStartTime, testEndTime) as DistanceRecord
        val distanceRecords = listOf(hcDistanceRecord)
        val distanceResponse = mock<ReadRecordsResponse<DistanceRecord>>()
        `when`(distanceResponse.records).thenReturn(distanceRecords)
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<DistanceRecord>? -> req != null && req.recordType == DistanceRecord::class }))
            .thenReturn(distanceResponse)

        val worker = createWorker()
        val result = worker.doWork()
        assertTrue("Worker should succeed.", result is ListenableWorker.Result.Success)

        val stagingDir = fileHandler.getStagingDirectory()
        val expectedFile = File(stagingDir, "DistanceRecord_${simulatedFetchedTimeMillis}.avro")
        assertTrue("Expected DistanceRecord Avro file should exist and be a file", expectedFile.exists() && expectedFile.isFile)

        val workInfos = WorkManager.getInstance(appContext).getWorkInfosForUniqueWork(AvroFileProcessorWorker.WORK_NAME).get()
        assertTrue("AvroFileProcessorWorker should have been enqueued.", workInfos.any { it.state == WorkInfo.State.ENQUEUED })
    }

    // Test case for ElevationGainedRecord
    @OptIn(ExperimentalAvro4kApi::class)
    @Test
    fun `doWork processes ElevationGainedRecord data and writes file successfully and enqueues processor`() = runBlocking {
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(allDataPermissions)
        val testEndTime = FIXED_INSTANT
        val testStartTime = testEndTime.minusSeconds(3600)
        val simulatedFetchedTimeMillis = FIXED_INSTANT.toEpochMilli()

        val hcElevationGainedRecord = generateSampleRecord(ElevationGainedRecord::class, testStartTime, testEndTime) as ElevationGainedRecord
        val elevationGainedRecords = listOf(hcElevationGainedRecord)
        val elevationGainedResponse = mock<ReadRecordsResponse<ElevationGainedRecord>>()
        `when`(elevationGainedResponse.records).thenReturn(elevationGainedRecords)
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<ElevationGainedRecord>? -> req != null && req.recordType == ElevationGainedRecord::class }))
            .thenReturn(elevationGainedResponse)

        val worker = createWorker()
        val result = worker.doWork()
        assertTrue("Worker should succeed.", result is ListenableWorker.Result.Success)

        val stagingDir = fileHandler.getStagingDirectory()
        val expectedFile = File(stagingDir, "ElevationGainedRecord_${simulatedFetchedTimeMillis}.avro")
        assertTrue("Expected ElevationGainedRecord Avro file should exist and be a file", expectedFile.exists() && expectedFile.isFile)

        val workInfos = WorkManager.getInstance(appContext).getWorkInfosForUniqueWork(AvroFileProcessorWorker.WORK_NAME).get()
        assertTrue("AvroFileProcessorWorker should have been enqueued.", workInfos.any { it.state == WorkInfo.State.ENQUEUED })
    }

    // Test case for ExerciseSessionRecord
    @OptIn(ExperimentalAvro4kApi::class)
    @Test
    fun `doWork processes ExerciseSessionRecord data and writes file successfully and enqueues processor`() = runBlocking {
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(allDataPermissions)
        val testEndTime = FIXED_INSTANT
        val testStartTime = testEndTime.minusSeconds(3600)
        val simulatedFetchedTimeMillis = FIXED_INSTANT.toEpochMilli()

        val hcExerciseSessionRecord = generateSampleRecord(ExerciseSessionRecord::class, testStartTime, testEndTime) as ExerciseSessionRecord
        val exerciseSessionRecords = listOf(hcExerciseSessionRecord)
        val exerciseSessionResponse = mock<ReadRecordsResponse<ExerciseSessionRecord>>()
        `when`(exerciseSessionResponse.records).thenReturn(exerciseSessionRecords)
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<ExerciseSessionRecord>? -> req != null && req.recordType == ExerciseSessionRecord::class }))
            .thenReturn(exerciseSessionResponse)

        val worker = createWorker()
        val result = worker.doWork()
        assertTrue("Worker should succeed.", result is ListenableWorker.Result.Success)

        val stagingDir = fileHandler.getStagingDirectory()
        val expectedFile = File(stagingDir, "ExerciseSessionRecord_${simulatedFetchedTimeMillis}.avro")
        assertTrue("Expected ExerciseSessionRecord Avro file should exist and be a file", expectedFile.exists() && expectedFile.isFile)

        val workInfos = WorkManager.getInstance(appContext).getWorkInfosForUniqueWork(AvroFileProcessorWorker.WORK_NAME).get()
        assertTrue("AvroFileProcessorWorker should have been enqueued.", workInfos.any { it.state == WorkInfo.State.ENQUEUED })
    }

    // Test case for FloorsClimbedRecord
    @OptIn(ExperimentalAvro4kApi::class)
    @Test
    fun `doWork processes FloorsClimbedRecord data and writes file successfully and enqueues processor`() = runBlocking {
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(allDataPermissions)
        val testEndTime = FIXED_INSTANT
        val testStartTime = testEndTime.minusSeconds(3600)
        val simulatedFetchedTimeMillis = FIXED_INSTANT.toEpochMilli()

        val hcFloorsClimbedRecord = generateSampleRecord(FloorsClimbedRecord::class, testStartTime, testEndTime) as FloorsClimbedRecord
        val floorsClimbedRecords = listOf(hcFloorsClimbedRecord)
        val floorsClimbedResponse = mock<ReadRecordsResponse<FloorsClimbedRecord>>()
        `when`(floorsClimbedResponse.records).thenReturn(floorsClimbedRecords)
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<FloorsClimbedRecord>? -> req != null && req.recordType == FloorsClimbedRecord::class }))
            .thenReturn(floorsClimbedResponse)

        val worker = createWorker()
        val result = worker.doWork()
        assertTrue("Worker should succeed.", result is ListenableWorker.Result.Success)

        val stagingDir = fileHandler.getStagingDirectory()
        val expectedFile = File(stagingDir, "FloorsClimbedRecord_${simulatedFetchedTimeMillis}.avro")
        assertTrue("Expected FloorsClimbedRecord Avro file should exist and be a file", expectedFile.exists() && expectedFile.isFile)

        val workInfos = WorkManager.getInstance(appContext).getWorkInfosForUniqueWork(AvroFileProcessorWorker.WORK_NAME).get()
        assertTrue("AvroFileProcessorWorker should have been enqueued.", workInfos.any { it.state == WorkInfo.State.ENQUEUED })
    }

    // Test case for HeartRateRecord
    @OptIn(ExperimentalAvro4kApi::class)
    @Test
    fun `doWork processes HeartRateRecord data and writes file successfully and enqueues processor`() = runBlocking {
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(allDataPermissions)
        val testEndTime = FIXED_INSTANT
        val testStartTime = testEndTime.minusSeconds(3600)
        val simulatedFetchedTimeMillis = FIXED_INSTANT.toEpochMilli()

        val hcHeartRateRecord = generateSampleRecord(HeartRateRecord::class, testStartTime, testEndTime) as HeartRateRecord
        val heartRateRecords = listOf(hcHeartRateRecord)
        val heartRateResponse = mock<ReadRecordsResponse<HeartRateRecord>>()
        `when`(heartRateResponse.records).thenReturn(heartRateRecords)
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<HeartRateRecord>? -> req != null && req.recordType == HeartRateRecord::class }))
            .thenReturn(heartRateResponse)

        val worker = createWorker()
        val result = worker.doWork()
        assertTrue("Worker should succeed.", result is ListenableWorker.Result.Success)

        val stagingDir = fileHandler.getStagingDirectory()
        val expectedFile = File(stagingDir, "HeartRateRecord_${simulatedFetchedTimeMillis}.avro")
        assertTrue("Expected HeartRateRecord Avro file should exist and be a file", expectedFile.exists() && expectedFile.isFile)

        val workInfos = WorkManager.getInstance(appContext).getWorkInfosForUniqueWork(AvroFileProcessorWorker.WORK_NAME).get()
        assertTrue("AvroFileProcessorWorker should have been enqueued.", workInfos.any { it.state == WorkInfo.State.ENQUEUED })
    }

    // Test case for HeartRateVariabilityRmssdRecord
    @OptIn(ExperimentalAvro4kApi::class)
    @Test
    fun `doWork processes HeartRateVariabilityRmssdRecord data and writes file successfully and enqueues processor`() = runBlocking {
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(allDataPermissions)
        val testEndTime = FIXED_INSTANT
        val testStartTime = testEndTime.minusSeconds(3600)
        val simulatedFetchedTimeMillis = FIXED_INSTANT.toEpochMilli()

        val hcHeartRateVariabilityRmssdRecord = generateSampleRecord(HeartRateVariabilityRmssdRecord::class, testStartTime, testEndTime) as HeartRateVariabilityRmssdRecord
        val heartRateVariabilityRmssdRecords = listOf(hcHeartRateVariabilityRmssdRecord)
        val heartRateVariabilityRmssdResponse = mock<ReadRecordsResponse<HeartRateVariabilityRmssdRecord>>()
        `when`(heartRateVariabilityRmssdResponse.records).thenReturn(heartRateVariabilityRmssdRecords)
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<HeartRateVariabilityRmssdRecord>? -> req != null && req.recordType == HeartRateVariabilityRmssdRecord::class }))
            .thenReturn(heartRateVariabilityRmssdResponse)

        val worker = createWorker()
        val result = worker.doWork()
        assertTrue("Worker should succeed.", result is ListenableWorker.Result.Success)

        val stagingDir = fileHandler.getStagingDirectory()
        val expectedFile = File(stagingDir, "HeartRateVariabilityRmssdRecord_${simulatedFetchedTimeMillis}.avro")
        assertTrue("Expected HeartRateVariabilityRmssdRecord Avro file should exist and be a file", expectedFile.exists() && expectedFile.isFile)

        val workInfos = WorkManager.getInstance(appContext).getWorkInfosForUniqueWork(AvroFileProcessorWorker.WORK_NAME).get()
        assertTrue("AvroFileProcessorWorker should have been enqueued.", workInfos.any { it.state == WorkInfo.State.ENQUEUED })
    }

    // Test case for HeightRecord
    @OptIn(ExperimentalAvro4kApi::class)
    @Test
    fun `doWork processes HeightRecord data and writes file successfully and enqueues processor`() = runBlocking {
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(allDataPermissions)
        val testEndTime = FIXED_INSTANT
        val testStartTime = testEndTime.minusSeconds(3600)
        val simulatedFetchedTimeMillis = FIXED_INSTANT.toEpochMilli()

        val hcHeightRecord = generateSampleRecord(HeightRecord::class, testStartTime, testEndTime) as HeightRecord
        val heightRecords = listOf(hcHeightRecord)
        val heightResponse = mock<ReadRecordsResponse<HeightRecord>>()
        `when`(heightResponse.records).thenReturn(heightRecords)
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<HeightRecord>? -> req != null && req.recordType == HeightRecord::class }))
            .thenReturn(heightResponse)

        val worker = createWorker()
        val result = worker.doWork()
        assertTrue("Worker should succeed.", result is ListenableWorker.Result.Success)

        val stagingDir = fileHandler.getStagingDirectory()
        val expectedFile = File(stagingDir, "HeightRecord_${simulatedFetchedTimeMillis}.avro")
        assertTrue("Expected HeightRecord Avro file should exist and be a file", expectedFile.exists() && expectedFile.isFile)

        val workInfos = WorkManager.getInstance(appContext).getWorkInfosForUniqueWork(AvroFileProcessorWorker.WORK_NAME).get()
        assertTrue("AvroFileProcessorWorker should have been enqueued.", workInfos.any { it.state == WorkInfo.State.ENQUEUED })
    }

    // Test case for HydrationRecord
    @OptIn(ExperimentalAvro4kApi::class)
    @Test
    fun `doWork processes HydrationRecord data and writes file successfully and enqueues processor`() = runBlocking {
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(allDataPermissions)
        val testEndTime = FIXED_INSTANT
        val testStartTime = testEndTime.minusSeconds(3600)
        val simulatedFetchedTimeMillis = FIXED_INSTANT.toEpochMilli()

        val hcHydrationRecord = generateSampleRecord(HydrationRecord::class, testStartTime, testEndTime) as HydrationRecord
        val hydrationRecords = listOf(hcHydrationRecord)
        val hydrationResponse = mock<ReadRecordsResponse<HydrationRecord>>()
        `when`(hydrationResponse.records).thenReturn(hydrationRecords)
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<HydrationRecord>? -> req != null && req.recordType == HydrationRecord::class }))
            .thenReturn(hydrationResponse)

        val worker = createWorker()
        val result = worker.doWork()
        assertTrue("Worker should succeed.", result is ListenableWorker.Result.Success)

        val stagingDir = fileHandler.getStagingDirectory()
        val expectedFile = File(stagingDir, "HydrationRecord_${simulatedFetchedTimeMillis}.avro")
        assertTrue("Expected HydrationRecord Avro file should exist and be a file", expectedFile.exists() && expectedFile.isFile)

        val workInfos = WorkManager.getInstance(appContext).getWorkInfosForUniqueWork(AvroFileProcessorWorker.WORK_NAME).get()
        assertTrue("AvroFileProcessorWorker should have been enqueued.", workInfos.any { it.state == WorkInfo.State.ENQUEUED })
    }

    // Test case for LeanBodyMassRecord
    @OptIn(ExperimentalAvro4kApi::class)
    @Test
    fun `doWork processes LeanBodyMassRecord data and writes file successfully and enqueues processor`() = runBlocking {
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(allDataPermissions)
        val testEndTime = FIXED_INSTANT
        val testStartTime = testEndTime.minusSeconds(3600)
        val simulatedFetchedTimeMillis = FIXED_INSTANT.toEpochMilli()

        val hcLeanBodyMassRecord = generateSampleRecord(LeanBodyMassRecord::class, testStartTime, testEndTime) as LeanBodyMassRecord
        val leanBodyMassRecords = listOf(hcLeanBodyMassRecord)
        val leanBodyMassResponse = mock<ReadRecordsResponse<LeanBodyMassRecord>>()
        `when`(leanBodyMassResponse.records).thenReturn(leanBodyMassRecords)
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<LeanBodyMassRecord>? -> req != null && req.recordType == LeanBodyMassRecord::class }))
            .thenReturn(leanBodyMassResponse)

        val worker = createWorker()
        val result = worker.doWork()
        assertTrue("Worker should succeed.", result is ListenableWorker.Result.Success)

        val stagingDir = fileHandler.getStagingDirectory()
        val expectedFile = File(stagingDir, "LeanBodyMassRecord_${simulatedFetchedTimeMillis}.avro")
        assertTrue("Expected LeanBodyMassRecord Avro file should exist and be a file", expectedFile.exists() && expectedFile.isFile)

        val workInfos = WorkManager.getInstance(appContext).getWorkInfosForUniqueWork(AvroFileProcessorWorker.WORK_NAME).get()
        assertTrue("AvroFileProcessorWorker should have been enqueued.", workInfos.any { it.state == WorkInfo.State.ENQUEUED })
    }

    // Test case for NutritionRecord
    @OptIn(ExperimentalAvro4kApi::class)
    @Test
    fun `doWork processes NutritionRecord data and writes file successfully and enqueues processor`() = runBlocking {
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(allDataPermissions)
        val testEndTime = FIXED_INSTANT
        val testStartTime = testEndTime.minusSeconds(3600)
        val simulatedFetchedTimeMillis = FIXED_INSTANT.toEpochMilli()

        val hcNutritionRecord = generateSampleRecord(NutritionRecord::class, testStartTime, testEndTime) as NutritionRecord
        val nutritionRecords = listOf(hcNutritionRecord)
        val nutritionResponse = mock<ReadRecordsResponse<NutritionRecord>>()
        `when`(nutritionResponse.records).thenReturn(nutritionRecords)
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<NutritionRecord>? -> req != null && req.recordType == NutritionRecord::class }))
            .thenReturn(nutritionResponse)

        val worker = createWorker()
        val result = worker.doWork()
        assertTrue("Worker should succeed.", result is ListenableWorker.Result.Success)

        val stagingDir = fileHandler.getStagingDirectory()
        val expectedFile = File(stagingDir, "NutritionRecord_${simulatedFetchedTimeMillis}.avro")
        assertTrue("Expected NutritionRecord Avro file should exist and be a file", expectedFile.exists() && expectedFile.isFile)

        val workInfos = WorkManager.getInstance(appContext).getWorkInfosForUniqueWork(AvroFileProcessorWorker.WORK_NAME).get()
        assertTrue("AvroFileProcessorWorker should have been enqueued.", workInfos.any { it.state == WorkInfo.State.ENQUEUED })
    }

    // Test case for OxygenSaturationRecord
    @OptIn(ExperimentalAvro4kApi::class)
    @Test
    fun `doWork processes OxygenSaturationRecord data and writes file successfully and enqueues processor`() = runBlocking {
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(allDataPermissions)
        val testEndTime = FIXED_INSTANT
        val testStartTime = testEndTime.minusSeconds(3600)
        val simulatedFetchedTimeMillis = FIXED_INSTANT.toEpochMilli()

        val hcOxygenSaturationRecord = generateSampleRecord(OxygenSaturationRecord::class, testStartTime, testEndTime) as OxygenSaturationRecord
        val oxygenSaturationRecords = listOf(hcOxygenSaturationRecord)
        val oxygenSaturationResponse = mock<ReadRecordsResponse<OxygenSaturationRecord>>()
        `when`(oxygenSaturationResponse.records).thenReturn(oxygenSaturationRecords)
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<OxygenSaturationRecord>? -> req != null && req.recordType == OxygenSaturationRecord::class }))
            .thenReturn(oxygenSaturationResponse)

        val worker = createWorker()
        val result = worker.doWork()
        assertTrue("Worker should succeed.", result is ListenableWorker.Result.Success)

        val stagingDir = fileHandler.getStagingDirectory()
        val expectedFile = File(stagingDir, "OxygenSaturationRecord_${simulatedFetchedTimeMillis}.avro")
        assertTrue("Expected OxygenSaturationRecord Avro file should exist and be a file", expectedFile.exists() && expectedFile.isFile)

        val workInfos = WorkManager.getInstance(appContext).getWorkInfosForUniqueWork(AvroFileProcessorWorker.WORK_NAME).get()
        assertTrue("AvroFileProcessorWorker should have been enqueued.", workInfos.any { it.state == WorkInfo.State.ENQUEUED })
    }

    // Test case for PowerRecord
    @OptIn(ExperimentalAvro4kApi::class)
    @Test
    fun `doWork processes PowerRecord data and writes file successfully and enqueues processor`() = runBlocking {
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(allDataPermissions)
        val testEndTime = FIXED_INSTANT
        val testStartTime = testEndTime.minusSeconds(3600)
        val simulatedFetchedTimeMillis = FIXED_INSTANT.toEpochMilli()

        val hcPowerRecord = generateSampleRecord(PowerRecord::class, testStartTime, testEndTime) as PowerRecord
        val powerRecords = listOf(hcPowerRecord)
        val powerResponse = mock<ReadRecordsResponse<PowerRecord>>()
        `when`(powerResponse.records).thenReturn(powerRecords)
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<PowerRecord>? -> req != null && req.recordType == PowerRecord::class }))
            .thenReturn(powerResponse)

        val worker = createWorker()
        val result = worker.doWork()
        assertTrue("Worker should succeed.", result is ListenableWorker.Result.Success)

        val stagingDir = fileHandler.getStagingDirectory()
        val expectedFile = File(stagingDir, "PowerRecord_${simulatedFetchedTimeMillis}.avro")
        assertTrue("Expected PowerRecord Avro file should exist and be a file", expectedFile.exists() && expectedFile.isFile)

        val workInfos = WorkManager.getInstance(appContext).getWorkInfosForUniqueWork(AvroFileProcessorWorker.WORK_NAME).get()
        assertTrue("AvroFileProcessorWorker should have been enqueued.", workInfos.any { it.state == WorkInfo.State.ENQUEUED })
    }

    // Test case for RespiratoryRateRecord
    @OptIn(ExperimentalAvro4kApi::class)
    @Test
    fun `doWork processes RespiratoryRateRecord data and writes file successfully and enqueues processor`() = runBlocking {
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(allDataPermissions)
        val testEndTime = FIXED_INSTANT
        val testStartTime = testEndTime.minusSeconds(3600)
        val simulatedFetchedTimeMillis = FIXED_INSTANT.toEpochMilli()

        val hcRespiratoryRateRecord = generateSampleRecord(RespiratoryRateRecord::class, testStartTime, testEndTime) as RespiratoryRateRecord
        val respiratoryRateRecords = listOf(hcRespiratoryRateRecord)
        val respiratoryRateResponse = mock<ReadRecordsResponse<RespiratoryRateRecord>>()
        `when`(respiratoryRateResponse.records).thenReturn(respiratoryRateRecords)
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<RespiratoryRateRecord>? -> req != null && req.recordType == RespiratoryRateRecord::class }))
            .thenReturn(respiratoryRateResponse)

        val worker = createWorker()
        val result = worker.doWork()
        assertTrue("Worker should succeed.", result is ListenableWorker.Result.Success)

        val stagingDir = fileHandler.getStagingDirectory()
        val expectedFile = File(stagingDir, "RespiratoryRateRecord_${simulatedFetchedTimeMillis}.avro")
        assertTrue("Expected RespiratoryRateRecord Avro file should exist and be a file", expectedFile.exists() && expectedFile.isFile)

        val workInfos = WorkManager.getInstance(appContext).getWorkInfosForUniqueWork(AvroFileProcessorWorker.WORK_NAME).get()
        assertTrue("AvroFileProcessorWorker should have been enqueued.", workInfos.any { it.state == WorkInfo.State.ENQUEUED })
    }

    // Test case for RestingHeartRateRecord
    @OptIn(ExperimentalAvro4kApi::class)
    @Test
    fun `doWork processes RestingHeartRateRecord data and writes file successfully and enqueues processor`() = runBlocking {
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(allDataPermissions)
        val testEndTime = FIXED_INSTANT
        val testStartTime = testEndTime.minusSeconds(3600)
        val simulatedFetchedTimeMillis = FIXED_INSTANT.toEpochMilli()

        val hcRestingHeartRateRecord = generateSampleRecord(RestingHeartRateRecord::class, testStartTime, testEndTime) as RestingHeartRateRecord
        val restingHeartRateRecords = listOf(hcRestingHeartRateRecord)
        val restingHeartRateResponse = mock<ReadRecordsResponse<RestingHeartRateRecord>>()
        `when`(restingHeartRateResponse.records).thenReturn(restingHeartRateRecords)
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<RestingHeartRateRecord>? -> req != null && req.recordType == RestingHeartRateRecord::class }))
            .thenReturn(restingHeartRateResponse)

        val worker = createWorker()
        val result = worker.doWork()
        assertTrue("Worker should succeed.", result is ListenableWorker.Result.Success)

        val stagingDir = fileHandler.getStagingDirectory()
        val expectedFile = File(stagingDir, "RestingHeartRateRecord_${simulatedFetchedTimeMillis}.avro")
        assertTrue("Expected RestingHeartRateRecord Avro file should exist and be a file", expectedFile.exists() && expectedFile.isFile)

        val workInfos = WorkManager.getInstance(appContext).getWorkInfosForUniqueWork(AvroFileProcessorWorker.WORK_NAME).get()
        assertTrue("AvroFileProcessorWorker should have been enqueued.", workInfos.any { it.state == WorkInfo.State.ENQUEUED })
    }

    // Test case for SleepSessionRecord
    @OptIn(ExperimentalAvro4kApi::class)
    @Test
    fun `doWork processes SleepSessionRecord data and writes file successfully and enqueues processor`() = runBlocking {
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(allDataPermissions)
        val testEndTime = FIXED_INSTANT
        val testStartTime = testEndTime.minusSeconds(3600)
        val simulatedFetchedTimeMillis = FIXED_INSTANT.toEpochMilli()

        val hcSleepSessionRecord = generateSampleRecord(SleepSessionRecord::class, testStartTime, testEndTime) as SleepSessionRecord
        val sleepSessionRecords = listOf(hcSleepSessionRecord)
        val sleepSessionResponse = mock<ReadRecordsResponse<SleepSessionRecord>>()
        `when`(sleepSessionResponse.records).thenReturn(sleepSessionRecords)
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<SleepSessionRecord>? -> req != null && req.recordType == SleepSessionRecord::class }))
            .thenReturn(sleepSessionResponse)

        val worker = createWorker()
        val result = worker.doWork()
        assertTrue("Worker should succeed.", result is ListenableWorker.Result.Success)

        val stagingDir = fileHandler.getStagingDirectory()
        val expectedFile = File(stagingDir, "SleepSessionRecord_${simulatedFetchedTimeMillis}.avro")
        assertTrue("Expected SleepSessionRecord Avro file should exist and be a file", expectedFile.exists() && expectedFile.isFile)

        val workInfos = WorkManager.getInstance(appContext).getWorkInfosForUniqueWork(AvroFileProcessorWorker.WORK_NAME).get()
        assertTrue("AvroFileProcessorWorker should have been enqueued.", workInfos.any { it.state == WorkInfo.State.ENQUEUED })
    }

    // Test case for SpeedRecord
    @OptIn(ExperimentalAvro4kApi::class)
    @Test
    fun `doWork processes SpeedRecord data and writes file successfully and enqueues processor`() = runBlocking {
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(allDataPermissions)
        val testEndTime = FIXED_INSTANT
        val testStartTime = testEndTime.minusSeconds(3600)
        val simulatedFetchedTimeMillis = FIXED_INSTANT.toEpochMilli()

        val hcSpeedRecord = generateSampleRecord(SpeedRecord::class, testStartTime, testEndTime) as SpeedRecord
        val speedRecords = listOf(hcSpeedRecord)
        val speedResponse = mock<ReadRecordsResponse<SpeedRecord>>()
        `when`(speedResponse.records).thenReturn(speedRecords)
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<SpeedRecord>? -> req != null && req.recordType == SpeedRecord::class }))
            .thenReturn(speedResponse)

        val worker = createWorker()
        val result = worker.doWork()
        assertTrue("Worker should succeed.", result is ListenableWorker.Result.Success)

        val stagingDir = fileHandler.getStagingDirectory()
        val expectedFile = File(stagingDir, "SpeedRecord_${simulatedFetchedTimeMillis}.avro")
        assertTrue("Expected SpeedRecord Avro file should exist and be a file", expectedFile.exists() && expectedFile.isFile)

        val workInfos = WorkManager.getInstance(appContext).getWorkInfosForUniqueWork(AvroFileProcessorWorker.WORK_NAME).get()
        assertTrue("AvroFileProcessorWorker should have been enqueued.", workInfos.any { it.state == WorkInfo.State.ENQUEUED })
    }

    // Test case for StepsCadenceRecord
    @OptIn(ExperimentalAvro4kApi::class)
    @Test
    fun `doWork processes StepsCadenceRecord data and writes file successfully and enqueues processor`() = runBlocking {
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(allDataPermissions)
        val testEndTime = FIXED_INSTANT
        val testStartTime = testEndTime.minusSeconds(3600)
        val simulatedFetchedTimeMillis = FIXED_INSTANT.toEpochMilli()

        val hcStepsCadenceRecord = generateSampleRecord(StepsCadenceRecord::class, testStartTime, testEndTime) as StepsCadenceRecord
        val stepsCadenceRecords = listOf(hcStepsCadenceRecord)
        val stepsCadenceResponse = mock<ReadRecordsResponse<StepsCadenceRecord>>()
        `when`(stepsCadenceResponse.records).thenReturn(stepsCadenceRecords)
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<StepsCadenceRecord>? -> req != null && req.recordType == StepsCadenceRecord::class }))
            .thenReturn(stepsCadenceResponse)

        val worker = createWorker()
        val result = worker.doWork()
        assertTrue("Worker should succeed.", result is ListenableWorker.Result.Success)

        val stagingDir = fileHandler.getStagingDirectory()
        val expectedFile = File(stagingDir, "StepsCadenceRecord_${simulatedFetchedTimeMillis}.avro")
        assertTrue("Expected StepsCadenceRecord Avro file should exist and be a file", expectedFile.exists() && expectedFile.isFile)

        val workInfos = WorkManager.getInstance(appContext).getWorkInfosForUniqueWork(AvroFileProcessorWorker.WORK_NAME).get()
        assertTrue("AvroFileProcessorWorker should have been enqueued.", workInfos.any { it.state == WorkInfo.State.ENQUEUED })
    }

    // Test case for StepsRecord
    @OptIn(ExperimentalAvro4kApi::class)
    @Test
    fun `doWork processes StepsRecord data and writes file successfully and enqueues processor`() = runBlocking {
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(allDataPermissions)
        val testEndTime = FIXED_INSTANT
        val testStartTime = testEndTime.minusSeconds(3600)
        val simulatedFetchedTimeMillis = FIXED_INSTANT.toEpochMilli()

        val hcStepsRecord = generateSampleRecord(StepsRecord::class, testStartTime, testEndTime) as StepsRecord
        val stepsRecords = listOf(hcStepsRecord)
        val stepsResponse = mock<ReadRecordsResponse<StepsRecord>>()
        `when`(stepsResponse.records).thenReturn(stepsRecords)
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<StepsRecord>? -> req != null && req.recordType == StepsRecord::class }))
            .thenReturn(stepsResponse)

        val worker = createWorker()
        val result = worker.doWork()
        assertTrue("Worker should succeed.", result is ListenableWorker.Result.Success)

        val stagingDir = fileHandler.getStagingDirectory()
        val expectedFile = File(stagingDir, "StepsRecord_${simulatedFetchedTimeMillis}.avro")
        assertTrue("Expected StepsRecord Avro file should exist and be a file", expectedFile.exists() && expectedFile.isFile)

        val workInfos = WorkManager.getInstance(appContext).getWorkInfosForUniqueWork(AvroFileProcessorWorker.WORK_NAME).get()
        assertTrue("AvroFileProcessorWorker should have been enqueued.", workInfos.any { it.state == WorkInfo.State.ENQUEUED })
    }

    // Test case for TotalCaloriesBurnedRecord
    @OptIn(ExperimentalAvro4kApi::class)
    @Test
    fun `doWork processes TotalCaloriesBurnedRecord data and writes file successfully and enqueues processor`() = runBlocking {
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(allDataPermissions)
        val testEndTime = FIXED_INSTANT
        val testStartTime = testEndTime.minusSeconds(3600)
        val simulatedFetchedTimeMillis = FIXED_INSTANT.toEpochMilli()

        val hcTotalCaloriesBurnedRecord = generateSampleRecord(TotalCaloriesBurnedRecord::class, testStartTime, testEndTime) as TotalCaloriesBurnedRecord
        val totalCaloriesBurnedRecords = listOf(hcTotalCaloriesBurnedRecord)
        val totalCaloriesBurnedResponse = mock<ReadRecordsResponse<TotalCaloriesBurnedRecord>>()
        `when`(totalCaloriesBurnedResponse.records).thenReturn(totalCaloriesBurnedRecords)
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<TotalCaloriesBurnedRecord>? -> req != null && req.recordType == TotalCaloriesBurnedRecord::class }))
            .thenReturn(totalCaloriesBurnedResponse)

        val worker = createWorker()
        val result = worker.doWork()
        assertTrue("Worker should succeed.", result is ListenableWorker.Result.Success)

        val stagingDir = fileHandler.getStagingDirectory()
        val expectedFile = File(stagingDir, "TotalCaloriesBurnedRecord_${simulatedFetchedTimeMillis}.avro")
        assertTrue("Expected TotalCaloriesBurnedRecord Avro file should exist and be a file", expectedFile.exists() && expectedFile.isFile)

        val workInfos = WorkManager.getInstance(appContext).getWorkInfosForUniqueWork(AvroFileProcessorWorker.WORK_NAME).get()
        assertTrue("AvroFileProcessorWorker should have been enqueued.", workInfos.any { it.state == WorkInfo.State.ENQUEUED })
    }

    // Test case for Vo2MaxRecord
    @OptIn(ExperimentalAvro4kApi::class)
    @Test
    fun `doWork processes Vo2MaxRecord data and writes file successfully and enqueues processor`() = runBlocking {
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(allDataPermissions)
        val testEndTime = FIXED_INSTANT
        val testStartTime = testEndTime.minusSeconds(3600)
        val simulatedFetchedTimeMillis = FIXED_INSTANT.toEpochMilli()

        val hcVo2MaxRecord = generateSampleRecord(Vo2MaxRecord::class, testStartTime, testEndTime) as Vo2MaxRecord
        val vo2MaxRecords = listOf(hcVo2MaxRecord)
        val vo2MaxResponse = mock<ReadRecordsResponse<Vo2MaxRecord>>()
        `when`(vo2MaxResponse.records).thenReturn(vo2MaxRecords)
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<Vo2MaxRecord>? -> req != null && req.recordType == Vo2MaxRecord::class }))
            .thenReturn(vo2MaxResponse)

        val worker = createWorker()
        val result = worker.doWork()
        assertTrue("Worker should succeed.", result is ListenableWorker.Result.Success)

        val stagingDir = fileHandler.getStagingDirectory()
        val expectedFile = File(stagingDir, "Vo2MaxRecord_${simulatedFetchedTimeMillis}.avro")
        assertTrue("Expected Vo2MaxRecord Avro file should exist and be a file", expectedFile.exists() && expectedFile.isFile)

        val workInfos = WorkManager.getInstance(appContext).getWorkInfosForUniqueWork(AvroFileProcessorWorker.WORK_NAME).get()
        assertTrue("AvroFileProcessorWorker should have been enqueued.", workInfos.any { it.state == WorkInfo.State.ENQUEUED })
    }

    // Test case for WeightRecord
    @OptIn(ExperimentalAvro4kApi::class)
    @Test
    fun `doWork processes WeightRecord data and writes file successfully and enqueues processor`() = runBlocking {
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(allDataPermissions)
        val testEndTime = FIXED_INSTANT
        val testStartTime = testEndTime.minusSeconds(3600)
        val simulatedFetchedTimeMillis = FIXED_INSTANT.toEpochMilli()

        val hcWeightRecord = generateSampleRecord(WeightRecord::class, testStartTime, testEndTime) as WeightRecord
        val weightRecords = listOf(hcWeightRecord)
        val weightResponse = mock<ReadRecordsResponse<WeightRecord>>()
        `when`(weightResponse.records).thenReturn(weightRecords)
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<WeightRecord>? -> req != null && req.recordType == WeightRecord::class }))
            .thenReturn(weightResponse)

        val worker = createWorker()
        val result = worker.doWork()
        assertTrue("Worker should succeed.", result is ListenableWorker.Result.Success)

        val stagingDir = fileHandler.getStagingDirectory()
        val expectedFile = File(stagingDir, "WeightRecord_${simulatedFetchedTimeMillis}.avro")
        assertTrue("Expected WeightRecord Avro file should exist and be a file", expectedFile.exists() && expectedFile.isFile)

        val workInfos = WorkManager.getInstance(appContext).getWorkInfosForUniqueWork(AvroFileProcessorWorker.WORK_NAME).get()
        assertTrue("AvroFileProcessorWorker should have been enqueued.", workInfos.any { it.state == WorkInfo.State.ENQUEUED })
    }
}