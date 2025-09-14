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
import io.github.hitoshura25.healthsyncapp.data.HealthConnectToAvroMapper
import io.github.hitoshura25.healthsyncapp.file.FileHandler // The interface
import io.github.hitoshura25.healthsyncapp.file.FileHandlerImpl // Concrete implementation
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
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
            val deserializedRecords = Files.newInputStream(outputFile.toPath()).buffered().use { inputStream ->
                AvroObjectContainer.decodeFromStream<AvroStepsRecord>(inputStream).toList()
            }
            assertEquals("Deserialized Avro content mismatch. Expected: $expectedAvroList, Actual: $deserializedRecords", expectedAvroList.toList(), deserializedRecords.toList())
        } catch (e: Exception) {
            throw AssertionError("Failed to read or deserialize Avro file ${outputFile.absolutePath}: ${e.message}", e)
        }
        // ---- END: AVRO FILE CONTENT VERIFICATION ----

        println("LOG I: StepsRecord Avro file created at: ${outputFile.absolutePath} and content verified.")
    }
}
