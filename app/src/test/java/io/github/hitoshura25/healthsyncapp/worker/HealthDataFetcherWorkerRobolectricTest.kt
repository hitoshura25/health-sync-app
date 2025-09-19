package io.github.hitoshura25.healthsyncapp.worker

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.BasalBodyTemperatureRecord
import androidx.health.connect.client.records.BasalMetabolicRateRecord
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
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.HeightRecord
import androidx.health.connect.client.records.HydrationRecord
import androidx.health.connect.client.records.LeanBodyMassRecord
import androidx.health.connect.client.records.NutritionRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.PowerRecord
import androidx.health.connect.client.records.RespiratoryRateRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.SpeedRecord
import androidx.health.connect.client.records.StepsCadenceRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.Vo2MaxRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.response.ReadRecordsResponse
import androidx.health.connect.client.units.BloodGlucose
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
import io.github.hitoshura25.healthsyncapp.MainViewModel
import io.github.hitoshura25.healthsyncapp.data.HealthConnectConstants
import io.github.hitoshura25.healthsyncapp.data.HealthConnectConstants.RECORD_PERMISSIONS
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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.invocation.InvocationOnMock
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.IOException
import java.time.Instant
import java.time.ZoneOffset
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

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

    @Before
    fun setUp()  {
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
        `when`(Log.d(Mockito.anyString(), Mockito.anyString())).thenAnswer { println("LOG D: ${it.arguments[1]}"); 0 }
        `when`(Log.i(Mockito.anyString(), Mockito.anyString())).thenAnswer { println("LOG I: ${it.arguments[1]}"); 0 }
        `when`(Log.w(Mockito.anyString(), Mockito.anyString())).thenAnswer { System.err.println("LOG W: ${it.arguments[1]}"); 0 }
        `when`(Log.e(Mockito.anyString(), Mockito.anyString(), Mockito.any(Throwable::class.java))).thenAnswer { System.err.println("LOG E: ${it.arguments[1]} | ${it.arguments[2]}"); 0 }

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
    fun `doWork processes StepsRecord data and writes file successfully and enqueues processor`() = runBlocking {
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(RECORD_PERMISSIONS)
        val testEndTime = FIXED_INSTANT
        val testStartTime = testEndTime.minusSeconds(3600)
        val simulatedFetchedTimeMillis = FIXED_INSTANT.toEpochMilli()

        val hcStepsRecord1 = StepsRecord(
            startTime = testStartTime,
            startZoneOffset = ZoneOffset.UTC,
            endTime = testEndTime,
            endZoneOffset = ZoneOffset.UTC,
            count = 100L,
            metadata = Metadata.manualEntry(clientRecordId = "client-steps-id-test-001")
        )
        val stepsRecords = listOf(hcStepsRecord1)
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

    @OptIn(ExperimentalAvro4kApi::class)
    @Test
    fun `doWork processes HeartRateRecord data and writes file successfully and enqueues processor`() = runBlocking {
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(RECORD_PERMISSIONS)
        val testEndTime = FIXED_INSTANT
        val testStartTime = testEndTime.minusSeconds(3600)
        val simulatedFetchedTimeMillis = FIXED_INSTANT.toEpochMilli()

        val hcHeartRateRecord1 = HeartRateRecord(
            startTime = testStartTime, startZoneOffset = ZoneOffset.UTC, endTime = testEndTime, endZoneOffset = ZoneOffset.UTC,
            samples = listOf(HeartRateRecord.Sample(time = testStartTime.plusSeconds(300), beatsPerMinute = 75L)),
            metadata = Metadata.manualEntry(clientRecordId = "client-hr-id-test-001")
        )
        val heartRateRecords = listOf(hcHeartRateRecord1)
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

    @OptIn(ExperimentalAvro4kApi::class)
    @Test
    fun `doWork processes all data types and writes multiple files successfully and enqueues processor`() = runBlocking {
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(RECORD_PERMISSIONS)
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

        val hcBgRecord = BloodGlucoseRecord(time = testStartTime, zoneOffset = ZoneOffset.UTC, level = androidx.health.connect.client.units.BloodGlucose.milligramsPerDeciliter(100.0), metadata = Metadata.manualEntry("client-bg-all"))
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
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(RECORD_PERMISSIONS)

        val worker = createWorker()
        val result = worker.doWork()
        assertTrue("Worker should succeed when permissions are granted but no data is returned.", result is ListenableWorker.Result.Success)

        val workInfos = WorkManager.getInstance(appContext).getWorkInfosForUniqueWork(AvroFileProcessorWorker.WORK_NAME).get()
        assertFalse("AvroFileProcessorWorker should NOT have been enqueued when no files written (empty data).", workInfos.any { it.state == WorkInfo.State.ENQUEUED })
    }

    @OptIn(ExperimentalAvro4kApi::class)
    @Test
    fun `doWork when readRecords fails for one type returns Failure and processes others`() = runBlocking {
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(RECORD_PERMISSIONS)
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
            level = androidx.health.connect.client.units.BloodGlucose.milligramsPerDeciliter(95.0),
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
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(RECORD_PERMISSIONS)
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
}