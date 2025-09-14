package io.github.hitoshura25.healthsyncapp.worker

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.response.ReadRecordsResponse
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.impl.utils.taskexecutor.TaskExecutor
import androidx.work.impl.utils.taskexecutor.SerialExecutor
import com.github.avrokotlin.avro4k.AvroObjectContainer
import com.github.avrokotlin.avro4k.ExperimentalAvro4kApi
import com.github.avrokotlin.avro4k.decodeFromStream
import io.github.hitoshura25.healthsyncapp.avro.AvroStepsRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroHeartRateRecord // Assuming this Avro DTO exists
import io.github.hitoshura25.healthsyncapp.avro.AvroSleepSessionRecord // Assuming this Avro DTO exists
import io.github.hitoshura25.healthsyncapp.data.HealthConnectToAvroMapper
import io.github.hitoshura25.healthsyncapp.file.FileHandler // The interface
import io.github.hitoshura25.healthsyncapp.file.FileHandlerImpl // Concrete implementation
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.argThat
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.nio.file.Files
import java.time.Instant
import java.time.ZoneOffset
import java.time.Duration

// Configure Robolectric to run tests
@RunWith(RobolectricTestRunner::class)
// Configure the SDK level for the test environment (optional, but good practice)
@Config(sdk = [Build.VERSION_CODES.P]) 
class HealthDataFetcherWorkerRobolectricTest {

    private lateinit var appContext: Context

    @Mock
    private lateinit var mockWorkerParams: WorkerParameters

    @Mock
    private lateinit var mockTaskExecutor: TaskExecutor

    @Mock
    private lateinit var mockSerialTaskExecutor: SerialExecutor

    @Mock
    private lateinit var mockHealthConnectClient: HealthConnectClient

    @Mock
    private lateinit var mockPermissionController: PermissionController

    private val mapper = HealthConnectToAvroMapper // Real instance
    private lateinit var fileHandler: FileHandler // Real instance of your FileHandler implementation

    private lateinit var worker: HealthDataFetcherWorker

    private lateinit var mockedLog: MockedStatic<Log>
    private lateinit var mockedInstant: MockedStatic<Instant>

    private val FIXED_INSTANT = Instant.ofEpochMilli(1678886400000L)
    private val AVRO_STAGING_SUBDIR = "avro_staging"

    private val allDataPermissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class)
    )

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this) // Initialize mocks for Robolectric

        appContext = ApplicationProvider.getApplicationContext<Context>()

        // Mock static Log methods
        mockedLog = Mockito.mockStatic(Log::class.java)
        `when`(Log.d(Mockito.anyString(), Mockito.anyString())).thenAnswer { println("LOG D: ${it.arguments[1]}"); 0 }
        `when`(Log.i(Mockito.anyString(), Mockito.anyString())).thenAnswer { println("LOG I: ${it.arguments[1]}"); 0 }
        `when`(Log.w(Mockito.anyString(), Mockito.anyString())).thenAnswer { System.err.println("LOG W: ${it.arguments[1]}"); 0 }
        `when`(Log.e(Mockito.anyString(), Mockito.anyString())).thenAnswer { System.err.println("LOG E: ${it.arguments[1]}"); 0 }
        `when`(Log.e(Mockito.anyString(), Mockito.anyString(), Mockito.any(Throwable::class.java))).thenAnswer { System.err.println("LOG E: ${it.arguments[1]} | ${it.arguments[2]}"); 0 }

        mockedInstant = Mockito.mockStatic(Instant::class.java, Mockito.CALLS_REAL_METHODS)
        `when`(Instant.now()).thenReturn(FIXED_INSTANT)

        // Setup WorkerParameters mocks
        `when`(mockWorkerParams.taskExecutor).thenReturn(mockTaskExecutor)
        `when`(mockTaskExecutor.serialTaskExecutor).thenReturn(mockSerialTaskExecutor)
        doNothing().`when`(mockSerialTaskExecutor).execute(Mockito.any())

        // Setup HealthConnectClient mocks
        `when`(mockHealthConnectClient.permissionController).thenReturn(mockPermissionController)

        // Instantiate your concrete FileHandler implementation
        fileHandler = FileHandlerImpl() 

        worker = HealthDataFetcherWorker(
            appContext, // Real context from Robolectric
            mockWorkerParams,
            mockHealthConnectClient,
            mapper,      // Real mapper
            fileHandler  // Real FileHandler
        )
    }

    @After
    fun tearDown() {
        mockedLog.close()
        mockedInstant.close()
        // Clean up files created by the test to ensure test isolation
        val stagingDir = File(appContext.filesDir, AVRO_STAGING_SUBDIR)
        if (stagingDir.exists()) {
            stagingDir.deleteRecursively()
        }
    }

    @OptIn(ExperimentalAvro4kApi::class)
    @Test
    fun `doWork processes StepsRecord data and writes file successfully`() = runBlocking {
        // 1. Setup mocks for this specific test
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(allDataPermissions)

        val testEndTime = FIXED_INSTANT
        val testStartTime = testEndTime.minusSeconds(3600)
        val simulatedFetchedTimeMillis = FIXED_INSTANT.toEpochMilli()

        val hcStepsRecord1 = StepsRecord(
            startTime = testStartTime,
            startZoneOffset = ZoneOffset.UTC,
            endTime = testEndTime,
            endZoneOffset = ZoneOffset.UTC,
            count = 100L,
            metadata = Metadata.manualEntry(
                device = Device(type = Device.TYPE_WATCH),
                clientRecordId = "client-steps-id-test-001"
            )
        )
        val stepsRecords = listOf(hcStepsRecord1)
        val stepsResponse = mock<ReadRecordsResponse<StepsRecord>>()
        `when`(stepsResponse.records).thenReturn(stepsRecords)
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<StepsRecord>? -> req != null && req.recordType == StepsRecord::class }))
            .thenReturn(stepsResponse)

        // Mock other data types to return empty lists
        val emptyHeartRateResponse = mock<ReadRecordsResponse<HeartRateRecord>>()
        `when`(emptyHeartRateResponse.records).thenReturn(emptyList())
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<HeartRateRecord>? -> req != null && req.recordType == HeartRateRecord::class }))
            .thenReturn(emptyHeartRateResponse)

        val emptySleepResponse = mock<ReadRecordsResponse<SleepSessionRecord>>()
        `when`(emptySleepResponse.records).thenReturn(emptyList())
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<SleepSessionRecord>? -> req != null && req.recordType == SleepSessionRecord::class }))
            .thenReturn(emptySleepResponse)

        // Expected Avro data (real mapper will be used)
        val expectedAvroStepsRecord1 = mapper.mapStepsRecord(hcStepsRecord1, simulatedFetchedTimeMillis)
        val expectedAvroList = listOf(expectedAvroStepsRecord1)

        // 2. Execute the worker
        val result = worker.doWork()

        // 3. Assertions
        assertTrue("Worker should succeed.", result is ListenableWorker.Result.Success)

        val expectedFileName = "StepsRecord_${FIXED_INSTANT.toEpochMilli()}.avro"
        val stagingDir = File(appContext.filesDir, AVRO_STAGING_SUBDIR)
        val outputFile = File(stagingDir, expectedFileName)

        // Verify file was created
        assertTrue("Output file should exist: ${outputFile.absolutePath}", outputFile.exists())
        assertTrue("Output file should not be empty.", outputFile.length() > 0)

        // ---- START: AVRO FILE CONTENT VERIFICATION ----
        try {
            val deserializedRecordsList = Files.newInputStream(outputFile.toPath()).buffered().use { inputStream ->
                AvroObjectContainer.decodeFromStream<AvroStepsRecord>(inputStream).toList()
            }
            assertEquals("Deserialized Avro content mismatch for StepsRecord. Expected: $expectedAvroList, Actual: $deserializedRecordsList", expectedAvroList, deserializedRecordsList)
        } catch (e: Exception) {
            throw AssertionError("Failed to read or deserialize StepsRecord Avro file ${outputFile.absolutePath}: ${e.message}", e)
        }
        // ---- END: AVRO FILE CONTENT VERIFICATION ----

        println("LOG I: StepsRecord Avro file created at: ${outputFile.absolutePath} and content verified.")
    }

    @OptIn(ExperimentalAvro4kApi::class)
    @Test
    fun `doWork processes HeartRateRecord data and writes file successfully`() = runBlocking {
        // 1. Setup mocks for this specific test
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(allDataPermissions)

        val testEndTime = FIXED_INSTANT
        val testStartTime = testEndTime.minusSeconds(3600) // Example time range
        val simulatedFetchedTimeMillis = FIXED_INSTANT.toEpochMilli()

        // Create sample HeartRateRecord data
        val sampleTime1 = testStartTime.plusSeconds(300) // 5 minutes in
        val sampleTime2 = testStartTime.plusSeconds(600) // 10 minutes in
        val hcHeartRateRecord1 = HeartRateRecord(
            startTime = testStartTime,
            startZoneOffset = ZoneOffset.UTC,
            endTime = testEndTime,
            endZoneOffset = ZoneOffset.UTC,
            samples = listOf(
                HeartRateRecord.Sample(time = sampleTime1, beatsPerMinute = 75L),
                HeartRateRecord.Sample(time = sampleTime2, beatsPerMinute = 78L)
            ),
            metadata = Metadata.manualEntry(
                device = Device(type = Device.TYPE_WATCH),
                clientRecordId = "client-hr-id-test-001"
            )
        )
        val heartRateRecords = listOf(hcHeartRateRecord1)
        val heartRateResponse = mock<ReadRecordsResponse<HeartRateRecord>>()
        `when`(heartRateResponse.records).thenReturn(heartRateRecords)
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<HeartRateRecord>? -> req != null && req.recordType == HeartRateRecord::class }))
            .thenReturn(heartRateResponse)

        // Mock other data types to return empty lists
        val emptyStepsResponse = mock<ReadRecordsResponse<StepsRecord>>()
        `when`(emptyStepsResponse.records).thenReturn(emptyList())
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<StepsRecord>? -> req != null && req.recordType == StepsRecord::class }))
            .thenReturn(emptyStepsResponse)

        val emptySleepResponse = mock<ReadRecordsResponse<SleepSessionRecord>>()
        `when`(emptySleepResponse.records).thenReturn(emptyList())
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<SleepSessionRecord>? -> req != null && req.recordType == SleepSessionRecord::class }))
            .thenReturn(emptySleepResponse)

        // Expected Avro data (real mapper will be used)
        val expectedAvroHeartRateRecord1 = mapper.mapHeartRateRecord(hcHeartRateRecord1, simulatedFetchedTimeMillis)
        val expectedAvroList = listOf(expectedAvroHeartRateRecord1)

        // 2. Execute the worker
        val result = worker.doWork()

        // 3. Assertions
        assertTrue("Worker should succeed.", result is ListenableWorker.Result.Success)

        val expectedFileName = "HeartRateRecord_${FIXED_INSTANT.toEpochMilli()}.avro"
        val stagingDir = File(appContext.filesDir, AVRO_STAGING_SUBDIR)
        val outputFile = File(stagingDir, expectedFileName)

        // Verify file was created
        assertTrue("Output file should exist: ${outputFile.absolutePath}", outputFile.exists())
        assertTrue("Output file should not be empty.", outputFile.length() > 0)

        // ---- START: AVRO FILE CONTENT VERIFICATION ----
        try {
            val deserializedRecordsList = Files.newInputStream(outputFile.toPath()).buffered().use { inputStream ->
                AvroObjectContainer.decodeFromStream<AvroHeartRateRecord>(inputStream).toList()
            }
            assertEquals("Deserialized Avro content mismatch for HeartRateRecord. Expected: $expectedAvroList, Actual: $deserializedRecordsList", expectedAvroList, deserializedRecordsList)
        } catch (e: Exception) {
            throw AssertionError("Failed to read or deserialize HeartRateRecord Avro file ${outputFile.absolutePath}: ${e.message}", e)
        }
        // ---- END: AVRO FILE CONTENT VERIFICATION ----

        println("LOG I: HeartRateRecord Avro file created at: ${outputFile.absolutePath} and content verified.")
    }

    @OptIn(ExperimentalAvro4kApi::class)
    @Test
    fun `doWork processes SleepSessionRecord data and writes file successfully`() = runBlocking {
        // 1. Setup mocks for this specific test
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(allDataPermissions)

        val sessionEndTime = FIXED_INSTANT
        val sessionStartTime = sessionEndTime.minusSeconds(8 * 3600) // 8 hour sleep session
        val simulatedFetchedTimeMillis = FIXED_INSTANT.toEpochMilli()

        // Create sample SleepSessionRecord data
        val stages = listOf(
            SleepSessionRecord.Stage(startTime = sessionStartTime, endTime = sessionStartTime.plus(Duration.ofHours(1)), stage = SleepSessionRecord.STAGE_TYPE_AWAKE),
            SleepSessionRecord.Stage(startTime = sessionStartTime.plus(Duration.ofHours(1)), endTime = sessionStartTime.plus(Duration.ofHours(3)), stage = SleepSessionRecord.STAGE_TYPE_LIGHT),
            SleepSessionRecord.Stage(startTime = sessionStartTime.plus(Duration.ofHours(3)), endTime = sessionStartTime.plus(Duration.ofHours(5)), stage = SleepSessionRecord.STAGE_TYPE_DEEP),
            SleepSessionRecord.Stage(startTime = sessionStartTime.plus(Duration.ofHours(5)), endTime = sessionStartTime.plus(Duration.ofHours(7)), stage = SleepSessionRecord.STAGE_TYPE_REM),
            SleepSessionRecord.Stage(startTime = sessionStartTime.plus(Duration.ofHours(7)), endTime = sessionEndTime, stage = SleepSessionRecord.STAGE_TYPE_LIGHT)
        )
        val hcSleepSessionRecord1 = SleepSessionRecord(
            startTime = sessionStartTime,
            startZoneOffset = ZoneOffset.UTC,
            endTime = sessionEndTime,
            endZoneOffset = ZoneOffset.UTC,
            stages = stages,
            title = "Nightly Sleep",
            notes = "Tested with Robolectric",
            metadata = Metadata.manualEntry(
                device = Device(type = Device.TYPE_PHONE),
                clientRecordId = "client-sleep-id-test-001"
            )
        )
        val sleepSessionRecords = listOf(hcSleepSessionRecord1)
        val sleepResponse = mock<ReadRecordsResponse<SleepSessionRecord>>()
        `when`(sleepResponse.records).thenReturn(sleepSessionRecords)
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<SleepSessionRecord>? -> req != null && req.recordType == SleepSessionRecord::class }))
            .thenReturn(sleepResponse)

        // Mock other data types to return empty lists
        val emptyStepsResponse = mock<ReadRecordsResponse<StepsRecord>>()
        `when`(emptyStepsResponse.records).thenReturn(emptyList())
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<StepsRecord>? -> req != null && req.recordType == StepsRecord::class }))
            .thenReturn(emptyStepsResponse)

        val emptyHeartRateResponse = mock<ReadRecordsResponse<HeartRateRecord>>()
        `when`(emptyHeartRateResponse.records).thenReturn(emptyList())
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<HeartRateRecord>? -> req != null && req.recordType == HeartRateRecord::class }))
            .thenReturn(emptyHeartRateResponse)

        // Expected Avro data (real mapper will be used)
        val expectedAvroSleepSessionRecord1 = mapper.mapSleepSessionRecord(hcSleepSessionRecord1, simulatedFetchedTimeMillis)
        val expectedAvroList = listOf(expectedAvroSleepSessionRecord1)

        // 2. Execute the worker
        val result = worker.doWork()

        // 3. Assertions
        assertTrue("Worker should succeed.", result is ListenableWorker.Result.Success)

        val expectedFileName = "SleepSessionRecord_${FIXED_INSTANT.toEpochMilli()}.avro"
        val stagingDir = File(appContext.filesDir, AVRO_STAGING_SUBDIR)
        val outputFile = File(stagingDir, expectedFileName)

        // Verify file was created
        assertTrue("Output file should exist: ${outputFile.absolutePath}", outputFile.exists())
        assertTrue("Output file should not be empty.", outputFile.length() > 0)

        // ---- START: AVRO FILE CONTENT VERIFICATION ----
        try {
            val deserializedRecordsList = Files.newInputStream(outputFile.toPath()).buffered().use { inputStream ->
                AvroObjectContainer.decodeFromStream<AvroSleepSessionRecord>(inputStream).toList()
            }
            assertEquals("Deserialized Avro content mismatch for SleepSessionRecord. Expected: $expectedAvroList, Actual: $deserializedRecordsList", expectedAvroList, deserializedRecordsList)
        } catch (e: Exception) {
            throw AssertionError("Failed to read or deserialize SleepSessionRecord Avro file ${outputFile.absolutePath}: ${e.message}", e)
        }
        // ---- END: AVRO FILE CONTENT VERIFICATION ----

        println("LOG I: SleepSessionRecord Avro file created at: ${outputFile.absolutePath} and content verified.")
    }

    @OptIn(ExperimentalAvro4kApi::class)
    @Test
    fun `doWork processes all data types and writes multiple files successfully`() = runBlocking {
        // 1. Setup mocks for this specific test
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(allDataPermissions)

        val testEndTime = FIXED_INSTANT
        val testStartTime = testEndTime.minusSeconds(3600) // Shared time range for simplicity
        val sessionStartTimeSleep = testEndTime.minusSeconds(8 * 3600) // For sleep
        val simulatedFetchedTimeMillis = FIXED_INSTANT.toEpochMilli()

        // --- StepsRecord Data ---
        val hcStepsRecord = StepsRecord(
            startTime = testStartTime, startZoneOffset = ZoneOffset.UTC, endTime = testEndTime, endZoneOffset = ZoneOffset.UTC, count = 150L,
            metadata = Metadata.manualEntry(device = Device(type = Device.TYPE_WATCH), clientRecordId = "client-steps-all-001")
        )
        val stepsRecords = listOf(hcStepsRecord)
        val stepsResponse = mock<ReadRecordsResponse<StepsRecord>>()
        `when`(stepsResponse.records).thenReturn(stepsRecords)
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<StepsRecord>? -> req != null && req.recordType == StepsRecord::class }))
            .thenReturn(stepsResponse)
        val expectedAvroSteps = mapper.mapStepsRecord(hcStepsRecord, simulatedFetchedTimeMillis)

        // --- HeartRateRecord Data ---
        val sampleHrTime1 = testStartTime.plusSeconds(100)
        val hcHeartRateRecord = HeartRateRecord(
            startTime = testStartTime, startZoneOffset = ZoneOffset.UTC, endTime = testEndTime, endZoneOffset = ZoneOffset.UTC,
            samples = listOf(HeartRateRecord.Sample(time = sampleHrTime1, beatsPerMinute = 80L)),
            metadata = Metadata.manualEntry(device = Device(type = Device.TYPE_WATCH), clientRecordId = "client-hr-all-001")
        )
        val heartRateRecords = listOf(hcHeartRateRecord)
        val heartRateResponse = mock<ReadRecordsResponse<HeartRateRecord>>()
        `when`(heartRateResponse.records).thenReturn(heartRateRecords)
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<HeartRateRecord>? -> req != null && req.recordType == HeartRateRecord::class }))
            .thenReturn(heartRateResponse)
        val expectedAvroHeartRate = mapper.mapHeartRateRecord(hcHeartRateRecord, simulatedFetchedTimeMillis)

        // --- SleepSessionRecord Data ---
        val hcSleepSessionRecord = SleepSessionRecord(
            startTime = sessionStartTimeSleep, startZoneOffset = ZoneOffset.UTC, endTime = testEndTime, endZoneOffset = ZoneOffset.UTC, // Using testEndTime for session for simplicity
            stages = listOf(SleepSessionRecord.Stage(startTime = sessionStartTimeSleep, endTime = testEndTime, stage = SleepSessionRecord.STAGE_TYPE_DEEP)),
            title = "Full Night Sleep",
            metadata = Metadata.manualEntry(device = Device(type = Device.TYPE_PHONE), clientRecordId = "client-sleep-all-001")
        )
        val sleepSessionRecords = listOf(hcSleepSessionRecord)
        val sleepResponse = mock<ReadRecordsResponse<SleepSessionRecord>>()
        `when`(sleepResponse.records).thenReturn(sleepSessionRecords)
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<SleepSessionRecord>? -> req != null && req.recordType == SleepSessionRecord::class }))
            .thenReturn(sleepResponse)
        val expectedAvroSleep = mapper.mapSleepSessionRecord(hcSleepSessionRecord, simulatedFetchedTimeMillis)
        
        // 2. Execute the worker
        val result = worker.doWork()

        // 3. Assertions
        assertTrue("Worker should succeed.", result is ListenableWorker.Result.Success)
        val stagingDir = File(appContext.filesDir, AVRO_STAGING_SUBDIR)

        // ---- Verify StepsRecord File ----
        val stepsFileName = "StepsRecord_${FIXED_INSTANT.toEpochMilli()}.avro"
        val stepsOutputFile = File(stagingDir, stepsFileName)
        assertTrue("StepsRecord output file should exist: ${stepsOutputFile.absolutePath}", stepsOutputFile.exists())
        assertTrue("StepsRecord output file should not be empty.", stepsOutputFile.length() > 0)
        try {
            val deserializedSteps = Files.newInputStream(stepsOutputFile.toPath()).buffered().use { inputStream ->
                AvroObjectContainer.decodeFromStream<AvroStepsRecord>(inputStream).toList()
            }
            assertEquals("Deserialized Avro content mismatch for StepsRecord.", listOf(expectedAvroSteps), deserializedSteps)
        } catch (e: Exception) {
            throw AssertionError("Failed to read/deserialize StepsRecord Avro file ${stepsOutputFile.absolutePath}: ${e.message}", e)
        }
        println("LOG I: StepsRecord Avro file (all types test) verified.")

        // ---- Verify HeartRateRecord File ----
        val hrFileName = "HeartRateRecord_${FIXED_INSTANT.toEpochMilli()}.avro"
        val hrOutputFile = File(stagingDir, hrFileName)
        assertTrue("HeartRateRecord output file should exist: ${hrOutputFile.absolutePath}", hrOutputFile.exists())
        assertTrue("HeartRateRecord output file should not be empty.", hrOutputFile.length() > 0)
        try {
            val deserializedHr = Files.newInputStream(hrOutputFile.toPath()).buffered().use { inputStream ->
                AvroObjectContainer.decodeFromStream<AvroHeartRateRecord>(inputStream).toList()
            }
            assertEquals("Deserialized Avro content mismatch for HeartRateRecord.", listOf(expectedAvroHeartRate), deserializedHr)
        } catch (e: Exception) {
            throw AssertionError("Failed to read/deserialize HeartRateRecord Avro file ${hrOutputFile.absolutePath}: ${e.message}", e)
        }
        println("LOG I: HeartRateRecord Avro file (all types test) verified.")

        // ---- Verify SleepSessionRecord File ----
        val sleepFileName = "SleepSessionRecord_${FIXED_INSTANT.toEpochMilli()}.avro"
        val sleepOutputFile = File(stagingDir, sleepFileName)
        assertTrue("SleepSessionRecord output file should exist: ${sleepOutputFile.absolutePath}", sleepOutputFile.exists())
        assertTrue("SleepSessionRecord output file should not be empty.", sleepOutputFile.length() > 0)
        try {
            val deserializedSleep = Files.newInputStream(sleepOutputFile.toPath()).buffered().use { inputStream ->
                AvroObjectContainer.decodeFromStream<AvroSleepSessionRecord>(inputStream).toList()
            }
            assertEquals("Deserialized Avro content mismatch for SleepSessionRecord.", listOf(expectedAvroSleep), deserializedSleep)
        } catch (e: Exception) {
            throw AssertionError("Failed to read/deserialize SleepSessionRecord Avro file ${sleepOutputFile.absolutePath}: ${e.message}", e)
        }
        println("LOG I: SleepSessionRecord Avro file (all types test) verified.")
    }

    @Test
    fun `doWork returns success when no permissions are granted`() = runBlocking {
        // 1. Setup mocks: No permissions granted
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(emptySet()) // No permissions

        // Mock HealthConnectClient.readRecords for all types to ensure they are NOT called if permissions are checked first.
        // If the worker correctly checks permissions and finds none, it should not attempt to read.
        val stepsResponse = mock<ReadRecordsResponse<StepsRecord>>()
        `when`(stepsResponse.records).thenReturn(emptyList()) // Should not be reached
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<StepsRecord>? -> req != null && req.recordType == StepsRecord::class }))
            .thenReturn(stepsResponse)

        val heartRateResponse = mock<ReadRecordsResponse<HeartRateRecord>>()
        `when`(heartRateResponse.records).thenReturn(emptyList()) // Should not be reached
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<HeartRateRecord>? -> req != null && req.recordType == HeartRateRecord::class }))
            .thenReturn(heartRateResponse)

        val sleepResponse = mock<ReadRecordsResponse<SleepSessionRecord>>()
        `when`(sleepResponse.records).thenReturn(emptyList()) // Should not be reached
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<SleepSessionRecord>? -> req != null && req.recordType == SleepSessionRecord::class }))
            .thenReturn(sleepResponse)

        // 2. Execute the worker
        val result = worker.doWork()

        // 3. Assertions
        assertTrue("Worker should succeed even if no permissions are granted.", result is ListenableWorker.Result.Success)

        // Verify that NO files were created
        val stagingDir = File(appContext.filesDir, AVRO_STAGING_SUBDIR)
        val stepsFile = File(stagingDir, "StepsRecord_${FIXED_INSTANT.toEpochMilli()}.avro")
        val hrFile = File(stagingDir, "HeartRateRecord_${FIXED_INSTANT.toEpochMilli()}.avro")
        val sleepFile = File(stagingDir, "SleepSessionRecord_${FIXED_INSTANT.toEpochMilli()}.avro")

        assertFalse("StepsRecord file should not be created when no permissions granted.", stepsFile.exists())
        assertFalse("HeartRateRecord file should not be created when no permissions granted.", hrFile.exists())
        assertFalse("SleepSessionRecord file should not be created when no permissions granted.", sleepFile.exists())

        println("LOG I: Worker succeeded and no files created when no permissions granted, as expected.")
    }

    @Test
    fun `doWork succeeds and creates no files when permissions granted but no data`() = runBlocking {
        // 1. Setup mocks: All permissions granted
        `when`(mockPermissionController.getGrantedPermissions()).thenReturn(allDataPermissions)

        // Mock HealthConnectClient.readRecords to return empty lists for all data types
        val emptyStepsResponse = mock<ReadRecordsResponse<StepsRecord>>()
        `when`(emptyStepsResponse.records).thenReturn(emptyList())
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<StepsRecord>? -> req != null && req.recordType == StepsRecord::class }))
            .thenReturn(emptyStepsResponse)

        val emptyHeartRateResponse = mock<ReadRecordsResponse<HeartRateRecord>>()
        `when`(emptyHeartRateResponse.records).thenReturn(emptyList())
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<HeartRateRecord>? -> req != null && req.recordType == HeartRateRecord::class }))
            .thenReturn(emptyHeartRateResponse)

        val emptySleepResponse = mock<ReadRecordsResponse<SleepSessionRecord>>()
        `when`(emptySleepResponse.records).thenReturn(emptyList())
        `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<SleepSessionRecord>? -> req != null && req.recordType == SleepSessionRecord::class }))
            .thenReturn(emptySleepResponse)

        // 2. Execute the worker
        val result = worker.doWork()

        // 3. Assertions
        assertTrue("Worker should succeed when permissions are granted but no data is returned.", result is ListenableWorker.Result.Success)

        // Verify that NO files were created because the record lists were empty
        val stagingDir = File(appContext.filesDir, AVRO_STAGING_SUBDIR)
        val stepsFile = File(stagingDir, "StepsRecord_${FIXED_INSTANT.toEpochMilli()}.avro")
        val hrFile = File(stagingDir, "HeartRateRecord_${FIXED_INSTANT.toEpochMilli()}.avro")
        val sleepFile = File(stagingDir, "SleepSessionRecord_${FIXED_INSTANT.toEpochMilli()}.avro")

        assertFalse("StepsRecord file should not be created when data is empty.", stepsFile.exists())
        assertFalse("HeartRateRecord file should not be created when data is empty.", hrFile.exists())
        assertFalse("SleepSessionRecord file should not be created when data is empty.", sleepFile.exists())

        println("LOG I: Worker succeeded and no files created when permissions granted but no data, as expected.")
    }
}
