package io.github.hitoshura25.healthsyncapp.worker

import android.content.Context
import android.util.Log 
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.response.ReadRecordsResponse
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.impl.utils.taskexecutor.TaskExecutor
import androidx.work.impl.utils.taskexecutor.SerialExecutor
import io.github.hitoshura25.healthsyncapp.data.HealthConnectToAvroMapper
import io.github.hitoshura25.healthsyncapp.avro.AvroStepsRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroHeartRateRecord // Using actual Avro class
import io.github.hitoshura25.healthsyncapp.avro.AvroHeartRateSample // Using actual Avro class
import io.github.hitoshura25.healthsyncapp.file.FileHandler
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals 
import org.junit.Assert.assertTrue 
import org.junit.After 
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockedStatic 
import org.mockito.Mockito // Keep for mockStatic, doReturn, doNothing
import org.mockito.Mockito.`when` // Specific import for when
import org.mockito.Mockito.verify
import org.mockito.Mockito.never
import org.mockito.Mockito.mock
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.doNothing
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any 
import org.mockito.kotlin.argThat
import org.mockito.kotlin.argumentCaptor
import java.io.File
import java.time.Instant
import java.time.ZoneOffset

@RunWith(MockitoJUnitRunner.Silent::class)
class HealthDataFetcherWorkerTest {

    @Mock
    private lateinit var mockContext: Context

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

    // Using the real mapper instance
    private val mapper = HealthConnectToAvroMapper 

    @Mock
    private lateinit var mockFileHandler: FileHandler
    
    @Mock
    private lateinit var mockApplicationContextFilesDir: File 

    private lateinit var worker: HealthDataFetcherWorker

    private lateinit var mockedLog: MockedStatic<Log> 
    private lateinit var mockedInstant: MockedStatic<Instant> 

    private val FIXED_INSTANT = Instant.ofEpochMilli(1678886400000L) 

    private val allDataPermissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class)
    )

    private val avroStagingSubDirName = "avro_staging" // Used in assertions

    @Before
    fun setUp() {
        mockedLog = Mockito.mockStatic(Log::class.java)
        `when`<Int>(Log.d(Mockito.anyString(), Mockito.anyString())).thenAnswer { invocation ->
            val tag = invocation.getArgument<String>(0)
            val msg = invocation.getArgument<String>(1)
            println("WORKER LOG D/$tag: $msg")
            0
        }
        `when`<Int>(Log.i(Mockito.anyString(), Mockito.anyString())).thenAnswer { invocation ->
            val tag = invocation.getArgument<String>(0)
            val msg = invocation.getArgument<String>(1)
            println("WORKER LOG I/$tag: $msg")
            0
        }
        `when`<Int>(Log.w(Mockito.anyString(), Mockito.anyString())).thenAnswer { invocation ->
            val tag = invocation.getArgument<String>(0)
            val msg = invocation.getArgument<String>(1)
            System.err.println("WORKER LOG W/$tag: $msg")
            0
        }
        `when`<Int>(Log.w(Mockito.anyString(), Mockito.anyString(), Mockito.any(Throwable::class.java))).thenAnswer { invocation ->
            val tag = invocation.getArgument<String>(0)
            val msg = invocation.getArgument<String>(1)
            val tr = invocation.getArgument<Throwable>(2)
            System.err.println("WORKER LOG W/$tag: $msg\n${tr.stackTraceToString()}")
            0
        }
        `when`<Int>(Log.e(Mockito.anyString(), Mockito.anyString())).thenAnswer { invocation ->
            val tag = invocation.getArgument<String>(0)
            val msg = invocation.getArgument<String>(1)
            System.err.println("WORKER LOG E/$tag: $msg")
            0
        }
        `when`<Int>(Log.e(Mockito.anyString(), Mockito.anyString(), Mockito.any(Throwable::class.java))).thenAnswer { invocation ->
            val tag = invocation.getArgument<String>(0)
            val msg = invocation.getArgument<String>(1)
            val tr = invocation.getArgument<Throwable>(2)
            System.err.println("WORKER LOG E/$tag: $msg\n${tr.stackTraceToString()}")
            0
        }

        mockedInstant = Mockito.mockStatic(Instant::class.java, Mockito.CALLS_REAL_METHODS)
        `when`(Instant.now()).thenReturn(FIXED_INSTANT)
        
        `when`(mockWorkerParams.getTaskExecutor()).thenReturn(mockTaskExecutor)
        `when`(mockTaskExecutor.getSerialTaskExecutor()).thenReturn(mockSerialTaskExecutor)
        doNothing().`when`(mockSerialTaskExecutor).execute(any())

        `when`(mockContext.applicationContext).thenReturn(mockContext)
        `when`(mockHealthConnectClient.permissionController).thenReturn(mockPermissionController)

        val tmpDir = System.getProperty("java.io.tmpdir")
        `when`(mockContext.filesDir).thenReturn(mockApplicationContextFilesDir)
        `when`(mockApplicationContextFilesDir.path).thenReturn(tmpDir) 
        doReturn("mockApplicationContextFilesDir:$tmpDir").`when`(mockApplicationContextFilesDir).toString()

        worker = HealthDataFetcherWorker(
            mockContext,
            mockWorkerParams, 
            mockHealthConnectClient,
            mapper, // Passing the real mapper instance
            mockFileHandler
        )
    }

    @After 
    fun tearDown() {
        mockedLog.close()
        mockedInstant.close() 
    }

    @Test
    fun `doWork returns success when no new data and permissions granted`() {
        runBlocking { 
            `when`(mockPermissionController.getGrantedPermissions()).thenReturn(allDataPermissions)

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

            val result = worker.doWork()

            assertTrue("Worker should succeed when no new data is found but permissions are granted.", result is ListenableWorker.Result.Success)
            verify(mockFileHandler, never()).writeAvroFile(any<Sequence<Any>>(), any<File>())
        }
    }

    @Test
    fun `doWork processes StepsRecord data and writes file successfully when permissions granted`() {
        runBlocking {
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

            val emptyHeartRateResponse = mock<ReadRecordsResponse<HeartRateRecord>>()
            `when`(emptyHeartRateResponse.records).thenReturn(emptyList())
            `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<HeartRateRecord>? -> req != null && req.recordType == HeartRateRecord::class }))
                .thenReturn(emptyHeartRateResponse)

            val emptySleepResponse = mock<ReadRecordsResponse<SleepSessionRecord>>()
            `when`(emptySleepResponse.records).thenReturn(emptyList())
            `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<SleepSessionRecord>? -> req != null && req.recordType == SleepSessionRecord::class }))
                .thenReturn(emptySleepResponse)

            val expectedAvroStepsRecord1 = mapper.mapStepsRecord(hcStepsRecord1, simulatedFetchedTimeMillis) // Real mapper call
            val expectedAvroList = listOf(expectedAvroStepsRecord1)

            Mockito.lenient().`when`(mockFileHandler.writeAvroFile<Any>(any(), any())).thenReturn(true)

            val result = worker.doWork()

            assertTrue("Worker should succeed when data is processed and file is written for StepsRecord.", result is ListenableWorker.Result.Success)

            val sequenceCaptor = argumentCaptor<Sequence<AvroStepsRecord>>() 
            val fileCaptor = argumentCaptor<File>()
            
            verify(mockFileHandler, Mockito.atLeastOnce()).writeAvroFile(sequenceCaptor.capture(), fileCaptor.capture())
            
            val capturedFiles = fileCaptor.allValues
            val capturedSequences = sequenceCaptor.allValues

            var stepsProcessedInThisTest = false
            for (i in capturedFiles.indices) {
                val file = capturedFiles[i]
                if (file.name.startsWith("StepsRecord")) {
                    assertEquals("Captured Avro sequence mismatch for StepsRecord", expectedAvroList, capturedSequences[i].toList())
                    val expectedParentPath = File(System.getProperty("java.io.tmpdir"), avroStagingSubDirName).path
                    assertEquals("Captured output file parent path mismatch for StepsRecord", expectedParentPath, file.parentFile.path)
                    val expectedFileName = "StepsRecord_${FIXED_INSTANT.toEpochMilli()}.avro"
                    assertEquals("Filename mismatch for StepsRecord", expectedFileName, file.name)
                    stepsProcessedInThisTest = true
                    break
                }
            }
            assertTrue("StepsRecord should have been processed and file written in this test flow.", stepsProcessedInThisTest)
        }
    }

    @Test
    fun `doWork processes HeartRateRecord data and writes file successfully when permissions granted`() {
        runBlocking {
            `when`(mockPermissionController.getGrantedPermissions()).thenReturn(allDataPermissions)

            val testEndTime = FIXED_INSTANT
            val testStartTime = testEndTime.minusSeconds(1800) 
            val sampleTime1 = testStartTime.plusSeconds(30)
            val sampleTime2 = testStartTime.plusSeconds(60)
            val simulatedFetchedTimeMillis = FIXED_INSTANT.toEpochMilli()

            val hrSamples = listOf(
                HeartRateRecord.Sample(time = sampleTime1, beatsPerMinute = 75L),
                HeartRateRecord.Sample(time = sampleTime2, beatsPerMinute = 78L)
            )
            val hcHeartRateRecord = HeartRateRecord(
                startTime = testStartTime,
                startZoneOffset = ZoneOffset.UTC,
                endTime = testEndTime,
                endZoneOffset = ZoneOffset.UTC,
                samples = hrSamples,
                metadata = Metadata.manualEntryWithId(
                    device = Device(type = Device.TYPE_WATCH, manufacturer = "Google", model = "Pixel Watch"), // Added more metadata
                    id = "client-hr-id-test-001",
                )
            )
            val heartRateRecords = listOf(hcHeartRateRecord)
            val heartRateResponse = mock<ReadRecordsResponse<HeartRateRecord>>()
            `when`(heartRateResponse.records).thenReturn(heartRateRecords)
            `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<HeartRateRecord>? -> req != null && req.recordType == HeartRateRecord::class }))
                .thenReturn(heartRateResponse)

            val emptyStepsResponse = mock<ReadRecordsResponse<StepsRecord>>()
            `when`(emptyStepsResponse.records).thenReturn(emptyList())
            `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<StepsRecord>? -> req != null && req.recordType == StepsRecord::class }))
                .thenReturn(emptyStepsResponse)

            val emptySleepResponse = mock<ReadRecordsResponse<SleepSessionRecord>>()
            `when`(emptySleepResponse.records).thenReturn(emptyList())
            `when`(mockHealthConnectClient.readRecords(argThat { req: ReadRecordsRequest<SleepSessionRecord>? -> req != null && req.recordType == SleepSessionRecord::class }))
                .thenReturn(emptySleepResponse)

            // Construct the expected AvroHeartRateRecord using actual Avro classes
            // This object should perfectly match what your REAL `mapper.mapHeartRateRecord` produces.
            val expectedAvroHRSamples = hrSamples.map {
                AvroHeartRateSample(
                    timeEpochMillis = it.time.toEpochMilli(),
                    beatsPerMinute = it.beatsPerMinute
                )
            }
            val expectedAvroHeartRateRecord = AvroHeartRateRecord(
                hcUid = "client-hr-id-test-001", // Or however your mapper derives this
                startTimeEpochMillis = testStartTime.toEpochMilli(),
                endTimeEpochMillis = testEndTime.toEpochMilli(),
                startZoneOffsetId = ZoneOffset.UTC.id,
                endZoneOffsetId = ZoneOffset.UTC.id,
                dataOriginPackageName = "",
                hcLastModifiedTimeEpochMillis = hcHeartRateRecord.metadata.lastModifiedTime.toEpochMilli(),
                clientRecordId = hcHeartRateRecord.metadata.clientRecordId,
                clientRecordVersion = hcHeartRateRecord.metadata.clientRecordVersion,
                appRecordFetchTimeEpochMillis = simulatedFetchedTimeMillis,
                samples = expectedAvroHRSamples
            )
            val expectedAvroList = listOf(expectedAvroHeartRateRecord)
            
            // The following line that tried to mock the real mapper has been REMOVED.
            // The real `mapper.mapHeartRateRecord` will now be called by the worker.

            Mockito.lenient().`when`(mockFileHandler.writeAvroFile<Any>(any(), any())).thenReturn(true)

            val result = worker.doWork()

            assertTrue("Worker should succeed when HeartRateRecord data is processed and file is written.", result is ListenableWorker.Result.Success)

            val sequenceCaptor = argumentCaptor<Sequence<AvroHeartRateRecord>>() 
            val fileCaptor = argumentCaptor<File>()
            
            verify(mockFileHandler, Mockito.atLeastOnce()).writeAvroFile(sequenceCaptor.capture(), fileCaptor.capture())

            var hrProcessedInThisTest = false
            val capturedFiles = fileCaptor.allValues
            val capturedSequences = sequenceCaptor.allValues

            for (i in capturedFiles.indices) {
                val file = capturedFiles[i]
                if (file.name.startsWith("HeartRateRecord")) {
                    val actualAvroList = capturedSequences[i].toList()
                    assertEquals("Captured Avro sequence size mismatch for HeartRateRecord", expectedAvroList.size, actualAvroList.size)
                    // This assertion will now compare against the output of your REAL mapper
                    assertEquals("Captured Avro sequence mismatch for HeartRateRecord", expectedAvroList, actualAvroList)
                    
                    val expectedParentPath = File(System.getProperty("java.io.tmpdir"), avroStagingSubDirName).path
                    assertEquals("Captured output file parent path mismatch for HeartRateRecord", expectedParentPath, file.parentFile.path)
                    val expectedFileName = "HeartRateRecord_${FIXED_INSTANT.toEpochMilli()}.avro"
                    assertEquals("Filename mismatch for HeartRateRecord", expectedFileName, file.name)
                    hrProcessedInThisTest = true
                    break 
                }
            }
            assertTrue("HeartRateRecord should have been processed and file written.", hrProcessedInThisTest)
        }
    }
}
