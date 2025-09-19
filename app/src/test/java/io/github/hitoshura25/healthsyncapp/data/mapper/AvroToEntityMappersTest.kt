package io.github.hitoshura25.healthsyncapp.data.mapper

import io.github.hitoshura25.healthsyncapp.avro.AvroBloodGlucoseMealType
import io.github.hitoshura25.healthsyncapp.avro.AvroBloodGlucoseRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroBloodGlucoseRelationToMeal
import io.github.hitoshura25.healthsyncapp.avro.AvroBloodGlucoseSpecimenSource
import io.github.hitoshura25.healthsyncapp.avro.AvroDevice
import io.github.hitoshura25.healthsyncapp.avro.AvroHeartRateRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroHeartRateSample
import io.github.hitoshura25.healthsyncapp.avro.AvroMetadata
import io.github.hitoshura25.healthsyncapp.avro.AvroSleepSessionRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroSleepStageRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroSleepStageType
import io.github.hitoshura25.healthsyncapp.avro.AvroStepsRecord
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toBloodGlucoseEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toHeartRateSampleEntities
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toSleepSessionEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toSleepStageEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toStepsRecordEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class AvroToEntityMappersTest {

    private val testMetadata = AvroMetadata(
        id = "test-hcuid-123",
        dataOriginPackageName = "com.example.healthapp",
        lastModifiedTimeEpochMillis = Instant.now().toEpochMilli() - 5000L,
        clientRecordId = "client-rec-001",
        clientRecordVersion = 2L,
        device = AvroDevice(
            manufacturer = "Google",
            model = "Pixel Watch",
            type = "WATCH"
        )
    )

    @Test
    fun `AvroStepsRecord toStepsRecordEntity correctly maps all fields`() {
        val nowEpochMillis = Instant.now().toEpochMilli()
        val avroRecord = AvroStepsRecord(
            metadata = testMetadata,
            startTimeEpochMillis = nowEpochMillis - 20000L,
            endTimeEpochMillis = nowEpochMillis - 10000L,
            startZoneOffsetId = "Europe/London",
            count = 500L,
            appRecordFetchTimeEpochMillis = nowEpochMillis
        )

        val entity = avroRecord.toStepsRecordEntity()

        assertEquals(testMetadata.id, entity.hcUid)
        assertEquals(500L, entity.count)
        assertEquals(nowEpochMillis - 20000L, entity.startTimeEpochMillis)
        assertEquals(nowEpochMillis - 10000L, entity.endTimeEpochMillis)
        assertEquals("Europe/London", entity.zoneOffsetId)
        assertEquals(nowEpochMillis, entity.appRecordFetchTimeEpochMillis)
        assertEquals(testMetadata.dataOriginPackageName, entity.dataOriginPackageName)
        assertEquals(testMetadata.lastModifiedTimeEpochMillis, entity.hcLastModifiedTimeEpochMillis)
        assertEquals(testMetadata.clientRecordId, entity.clientRecordId)
        assertEquals(testMetadata.clientRecordVersion, entity.clientRecordVersion)
        assertEquals(testMetadata.device?.manufacturer, entity.deviceManufacturer)
        assertEquals(testMetadata.device?.model, entity.deviceModel)
        assertEquals(testMetadata.device?.type, entity.deviceType)
    }

    @Test
    fun `AvroHeartRateRecord toHeartRateSampleEntities correctly maps all fields`() {
        val nowEpochMillis = Instant.now().toEpochMilli()
        val avroRecord = AvroHeartRateRecord(
            metadata = testMetadata.copy(id = "hr-record-uid"),
            startTimeEpochMillis = nowEpochMillis - 30000L,
            endTimeEpochMillis = nowEpochMillis - 20000L,
            startZoneOffsetId = "America/New_York",
            appRecordFetchTimeEpochMillis = nowEpochMillis,
            samples = listOf(
                AvroHeartRateSample(nowEpochMillis - 25000L, 80L),
                AvroHeartRateSample(nowEpochMillis - 24000L, 82L)
            )
        )

        val entities = avroRecord.toHeartRateSampleEntities()

        assertEquals(2, entities.size)
        entities.forEach {
            assertEquals("hr-record-uid", it.hcRecordUid)
            assertEquals("America/New_York", it.zoneOffsetId)
            assertEquals(nowEpochMillis, it.appRecordFetchTimeEpochMillis)
            assertEquals(testMetadata.dataOriginPackageName, it.dataOriginPackageName)
            assertEquals(testMetadata.lastModifiedTimeEpochMillis, it.hcLastModifiedTimeEpochMillis)
            assertEquals(testMetadata.clientRecordId, it.clientRecordId)
            assertEquals(testMetadata.clientRecordVersion, it.clientRecordVersion)
            assertEquals(testMetadata.device?.manufacturer, it.deviceManufacturer)
            assertEquals(testMetadata.device?.model, it.deviceModel)
            assertEquals(testMetadata.device?.type, it.deviceType)
        }
        assertEquals(80L, entities[0].beatsPerMinute)
        assertEquals(82L, entities[1].beatsPerMinute)
    }

    @Test
    fun `AvroSleepSessionRecord toSleepSessionEntity correctly maps all fields`() {
        val nowEpochMillis = Instant.now().toEpochMilli()
        val avroRecord = AvroSleepSessionRecord(
            metadata = testMetadata.copy(id = "sleep-session-uid"),
            title = "Night Sleep",
            notes = "Good sleep",
            startTimeEpochMillis = nowEpochMillis - 8 * 3600 * 1000,
            endTimeEpochMillis = nowEpochMillis,
            startZoneOffsetId = "UTC",
            endZoneOffsetId = "UTC",
            durationMillis = 8 * 3600 * 1000,
            appRecordFetchTimeEpochMillis = nowEpochMillis + 1000,
            stages = emptyList()
        )

        val entity = avroRecord.toSleepSessionEntity()

        assertEquals("sleep-session-uid", entity.hcUid)
        assertEquals("Night Sleep", entity.title)
        assertEquals(nowEpochMillis - 8 * 3600 * 1000, entity.startTimeEpochMillis)
        assertEquals(nowEpochMillis, entity.endTimeEpochMillis)
        assertEquals(nowEpochMillis + 1000, entity.appRecordFetchTimeEpochMillis)
        assertEquals(testMetadata.dataOriginPackageName, entity.dataOriginPackageName)
        assertEquals(testMetadata.lastModifiedTimeEpochMillis, entity.hcLastModifiedTimeEpochMillis)
        assertEquals(testMetadata.clientRecordId, entity.clientRecordId)
        assertEquals(testMetadata.clientRecordVersion, entity.clientRecordVersion)
        assertEquals(testMetadata.device?.manufacturer, entity.deviceManufacturer)
        assertEquals(testMetadata.device?.model, entity.deviceModel)
        assertEquals(testMetadata.device?.type, entity.deviceType)
    }

    @Test
    fun `AvroSleepStageRecord toSleepStageEntity correctly maps all fields`() {
        val nowEpochMillis = Instant.now().toEpochMilli()
        val avroRecord = AvroSleepStageRecord(
            startTimeEpochMillis = nowEpochMillis - 3600 * 1000,
            endTimeEpochMillis = nowEpochMillis,
            stage = AvroSleepStageType.DEEP
        )

        val entity = avroRecord.toSleepStageEntity("parent-sleep-session-uid")

        assertEquals("parent-sleep-session-uid", entity.sessionHcUid)
        assertEquals(nowEpochMillis - 3600 * 1000, entity.startTimeEpochMillis)
        assertEquals(nowEpochMillis, entity.endTimeEpochMillis)
        assertEquals(AvroSleepStageType.DEEP.name, entity.stage)
    }

    @Test
    fun `AvroBloodGlucoseRecord toBloodGlucoseEntity correctly maps all fields`() {
        val nowEpochMillis = Instant.now().toEpochMilli()
        val avroRecord = AvroBloodGlucoseRecord(
            metadata = testMetadata.copy(id = "test-hcuid-bg-123"),
            timeEpochMillis = nowEpochMillis - 10000L,
            zoneOffsetId = "Europe/London",
            levelInMilligramsPerDeciliter = 120.5,
            specimenSource = AvroBloodGlucoseSpecimenSource.CAPILLARY_BLOOD,
            mealType = AvroBloodGlucoseMealType.LUNCH,
            relationToMeal = AvroBloodGlucoseRelationToMeal.AFTER_MEAL,
            appRecordFetchTimeEpochMillis = nowEpochMillis
        )

        val entity = avroRecord.toBloodGlucoseEntity()

        assertEquals("test-hcuid-bg-123", entity.hcUid)
        assertEquals(nowEpochMillis - 10000L, entity.timeEpochMillis)
        assertEquals("Europe/London", entity.zoneOffsetId)
        assertEquals(120.5, entity.levelInMilligramsPerDeciliter, 0.001)
        assertEquals(2, entity.specimenSource)
        assertEquals(2, entity.mealType)
        assertEquals(4, entity.relationToMeal)
        assertEquals(testMetadata.dataOriginPackageName, entity.dataOriginPackageName)
        assertEquals(testMetadata.lastModifiedTimeEpochMillis, entity.hcLastModifiedTimeEpochMillis)
        assertEquals(testMetadata.clientRecordId, entity.clientRecordId)
        assertEquals(testMetadata.clientRecordVersion, entity.clientRecordVersion)
        assertEquals(nowEpochMillis, entity.appRecordFetchTimeEpochMillis)
        assertEquals(testMetadata.device?.manufacturer, entity.deviceManufacturer)
        assertEquals(testMetadata.device?.model, entity.deviceModel)
        assertEquals(testMetadata.device?.type, entity.deviceType)
    }
}
