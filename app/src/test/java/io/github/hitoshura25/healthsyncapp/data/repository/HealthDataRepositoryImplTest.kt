package io.github.hitoshura25.healthsyncapp.data.repository

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.MealType
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Device.Companion.TYPE_PHONE
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.response.ReadRecordsResponse
import androidx.health.connect.client.units.BloodGlucose
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.BloodGlucoseDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.HeartRateSampleDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.SleepSessionDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.StepsRecordDao
import io.github.hitoshura25.healthsyncapp.data.local.database.entity.BloodGlucoseEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import java.time.Instant
import java.time.ZoneOffset

class HealthDataRepositoryImplTest {

    @Mock
    private lateinit var mockHealthConnectClient: HealthConnectClient

    @Mock
    private lateinit var mockStepsRecordDao: StepsRecordDao

    @Mock
    private lateinit var mockHeartRateSampleDao: HeartRateSampleDao

    @Mock
    private lateinit var mockSleepSessionDao: SleepSessionDao

    @Mock
    private lateinit var mockBloodGlucoseDao: BloodGlucoseDao

    private lateinit var repository: HealthDataRepositoryImpl

    // Removed @Captor field

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        repository = HealthDataRepositoryImpl(
            stepsRecordDao = mockStepsRecordDao,
            heartRateSampleDao = mockHeartRateSampleDao,
            sleepSessionDao = mockSleepSessionDao,
            bloodGlucoseDao = mockBloodGlucoseDao
        )
    }

    @Test
    fun `fetchAndSaveBloodGlucoseData correctly maps and saves data`() = runBlocking {
        val startTime = Instant.now().minusSeconds(3600)
        val endTime = Instant.now()
        val fixedTime = Instant.now()

        val sdkRecord1 = BloodGlucoseRecord(
            time = fixedTime.minusSeconds(100),
            zoneOffset = ZoneOffset.UTC,
            level = BloodGlucose.milligramsPerDeciliter(110.0),
            specimenSource = BloodGlucoseRecord.SPECIMEN_SOURCE_CAPILLARY_BLOOD,
            mealType = MealType.MEAL_TYPE_BREAKFAST,
            relationToMeal = BloodGlucoseRecord.RELATION_TO_MEAL_BEFORE_MEAL,
            metadata = Metadata.manualEntry(
                clientRecordId = "client_bg_1",
                clientRecordVersion = 1L,
                device = Device(manufacturer = "Google", model = "Pixel Test", type = TYPE_PHONE)
            ),
        )
        val sdkRecord2 = BloodGlucoseRecord(
            time = fixedTime.minusSeconds(200),
            zoneOffset = ZoneOffset.ofHours(-5),
            level = BloodGlucose.millimolesPerLiter(6.5), // Test with mmol/L to ensure conversion
            specimenSource = BloodGlucoseRecord.SPECIMEN_SOURCE_INTERSTITIAL_FLUID,
            mealType = MealType.MEAL_TYPE_LUNCH,
            relationToMeal = BloodGlucoseRecord.RELATION_TO_MEAL_AFTER_MEAL,
            metadata = Metadata.manualEntry(
                clientRecordId = "client_bg_2",
                clientRecordVersion = 3L,
                device = Device(manufacturer = "Google", model = "Pixel Test", type = TYPE_PHONE)
            ),
        )

        val mockResponse = mock(ReadRecordsResponse::class.java) as ReadRecordsResponse<BloodGlucoseRecord>
        `when`(mockResponse.records).thenReturn(listOf(sdkRecord1, sdkRecord2))

        `when`(mockHealthConnectClient.readRecords(any<ReadRecordsRequest<BloodGlucoseRecord>>()))
            .thenReturn(mockResponse)

        val result = repository.fetchAndSaveBloodGlucoseData(mockHealthConnectClient, startTime, endTime)

        assertEquals(2, result.size) 

        val captor = argumentCaptor<List<BloodGlucoseEntity>>() // Declared captor inline
        verify(mockBloodGlucoseDao).insertAll(captor.capture()) // Used new captor
        val capturedEntities = captor.firstValue // Accessed captured value correctly
        assertEquals(2, capturedEntities.size)

        // Validate mapping for sdkRecord1
        val entity1 = capturedEntities.first { it.clientRecordId == "client_bg_1" }
        assertEquals(sdkRecord1.time.toEpochMilli(), entity1.timeEpochMillis)
        assertEquals(sdkRecord1.zoneOffset?.id, entity1.zoneOffsetId)
        assertEquals(sdkRecord1.level.inMilligramsPerDeciliter, entity1.levelInMilligramsPerDeciliter, 0.001)
        assertEquals(sdkRecord1.specimenSource, entity1.specimenSource)
        assertEquals(sdkRecord1.mealType, entity1.mealType)
        assertEquals(sdkRecord1.relationToMeal, entity1.relationToMeal)
        assertEquals("", entity1.dataOriginPackageName)
        assertEquals(sdkRecord1.metadata.lastModifiedTime.toEpochMilli(), entity1.hcLastModifiedTimeEpochMillis)
        assertEquals("client_bg_1", entity1.clientRecordId)
        assertEquals(1L, entity1.clientRecordVersion)
        assertEquals(false, entity1.isSynced)
        assertEquals(sdkRecord1.metadata.id, entity1.hcUid)


        // Validate mapping for sdkRecord2
        val entity2 = capturedEntities.first { it.clientRecordId == "client_bg_2" }
        assertEquals(sdkRecord2.time.toEpochMilli(), entity2.timeEpochMillis)
        assertEquals(sdkRecord2.zoneOffset?.id, entity2.zoneOffsetId)
        assertEquals(sdkRecord2.level.inMilligramsPerDeciliter, entity2.levelInMilligramsPerDeciliter, 0.001)
        assertEquals(sdkRecord2.specimenSource, entity2.specimenSource)
        assertEquals(sdkRecord2.mealType, entity2.mealType)
        assertEquals(sdkRecord2.relationToMeal, entity2.relationToMeal)
        assertEquals("", entity2.dataOriginPackageName)
        assertEquals(sdkRecord2.metadata.lastModifiedTime.toEpochMilli(), entity2.hcLastModifiedTimeEpochMillis)
        assertEquals("client_bg_2", entity2.clientRecordId)
        assertEquals(3L, entity2.clientRecordVersion)
        assertEquals(false, entity2.isSynced)
        assertEquals(sdkRecord2.metadata.id, entity2.hcUid)

    }
}
