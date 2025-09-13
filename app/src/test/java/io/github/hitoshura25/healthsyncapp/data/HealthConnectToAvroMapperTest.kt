package io.github.hitoshura25.healthsyncapp.data

import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord // Added
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Device.Companion.TYPE_PHONE
import androidx.health.connect.client.records.metadata.Device.Companion.TYPE_UNKNOWN // Added
import androidx.health.connect.client.records.metadata.Device.Companion.TYPE_WATCH
import androidx.health.connect.client.records.metadata.Metadata
import io.github.hitoshura25.healthsyncapp.avro.AvroHeartRateRecord
import io.github.hitoshura25.healthsyncapp.avro.AvroHeartRateSample
import io.github.hitoshura25.healthsyncapp.avro.AvroSleepSessionRecord // Added
import io.github.hitoshura25.healthsyncapp.avro.AvroSleepStageRecord // Added
import io.github.hitoshura25.healthsyncapp.avro.AvroSleepStageType // Added
import io.github.hitoshura25.healthsyncapp.avro.AvroStepsRecord
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset

class HealthConnectToAvroMapperTest {

    @Test
    fun `mapStepsRecord should correctly map HC StepsRecord to AvroStepsRecord`() {
        val now = Instant.now()
        val fetchedTime = System.currentTimeMillis()

        val hcRecord = StepsRecord(
            startTime = now.minusSeconds(3600), // 1 hour ago
            startZoneOffset = ZoneOffset.UTC,
            endTime = now,
            endZoneOffset = ZoneOffset.ofHoursMinutes(5, 30), // Example: IST
            count = 5000L,
            metadata = Metadata.manualEntry(
                clientRecordId = "client-steps-id-001",
                clientRecordVersion = 2L,
                device = Device(manufacturer = "Google", model = "Pixel Test", type = TYPE_PHONE)
            )
        )

        val expectedAvroRecord = AvroStepsRecord(
            hcUid = hcRecord.metadata.id,
            startTimeEpochMillis = hcRecord.startTime.toEpochMilli(),
            endTimeEpochMillis = hcRecord.endTime.toEpochMilli(),
            startZoneOffsetId = hcRecord.startZoneOffset?.id,
            endZoneOffsetId = hcRecord.endZoneOffset?.id,
            count = hcRecord.count,
            dataOriginPackageName = hcRecord.metadata.dataOrigin.packageName,
            hcLastModifiedTimeEpochMillis = hcRecord.metadata.lastModifiedTime.toEpochMilli(),
            clientRecordId = hcRecord.metadata.clientRecordId,
            clientRecordVersion = hcRecord.metadata.clientRecordVersion,
            appRecordFetchTimeEpochMillis = fetchedTime
        )
        
        val actualAvroRecord = HealthConnectToAvroMapper.mapStepsRecord(hcRecord, fetchedTime)
        assertEquals(expectedAvroRecord, actualAvroRecord)
    }

    @Test
    fun `mapHeartRateRecord should correctly map HC HeartRateRecord to AvroHeartRateRecord`() {
        val now = Instant.now()
        val fetchedTime = System.currentTimeMillis()

        val hcHeartRateRecord = HeartRateRecord(
            startTime = now.minusSeconds(600), // 10 minutes ago
            startZoneOffset = ZoneOffset.ofHours(-5),
            endTime = now.minusSeconds(300), // 5 minutes ago
            endZoneOffset = ZoneOffset.ofHours(-5),
            samples = listOf(
                HeartRateRecord.Sample(time = now.minusSeconds(500), beatsPerMinute = 70L),
                HeartRateRecord.Sample(time = now.minusSeconds(400), beatsPerMinute = 72L)
            ),
            metadata = Metadata.manualEntry(
                clientRecordId = "client-hr-id-002",
                clientRecordVersion = 3L,
                device = Device(manufacturer = "Fit Example", model = "Pulse Pro", type = TYPE_WATCH)
            )
        )

        val expectedAvroRecord = AvroHeartRateRecord(
            hcUid = hcHeartRateRecord.metadata.id,
            startTimeEpochMillis = hcHeartRateRecord.startTime.toEpochMilli(),
            endTimeEpochMillis = hcHeartRateRecord.endTime.toEpochMilli(),
            startZoneOffsetId = hcHeartRateRecord.startZoneOffset?.id,
            endZoneOffsetId = hcHeartRateRecord.endZoneOffset?.id,
            dataOriginPackageName = hcHeartRateRecord.metadata.dataOrigin.packageName,
            hcLastModifiedTimeEpochMillis = hcHeartRateRecord.metadata.lastModifiedTime.toEpochMilli(),
            clientRecordId = hcHeartRateRecord.metadata.clientRecordId,
            clientRecordVersion = hcHeartRateRecord.metadata.clientRecordVersion,
            appRecordFetchTimeEpochMillis = fetchedTime,
            samples = listOf(
                AvroHeartRateSample(timeEpochMillis = now.minusSeconds(500).toEpochMilli(), beatsPerMinute = 70L),
                AvroHeartRateSample(timeEpochMillis = now.minusSeconds(400).toEpochMilli(), beatsPerMinute = 72L)
            )
        )

        val actualAvroRecord = HealthConnectToAvroMapper.mapHeartRateRecord(hcHeartRateRecord, fetchedTime)
        assertEquals(expectedAvroRecord, actualAvroRecord)
    }

    @Test
    fun `mapSleepSessionRecord should correctly map HC SleepSessionRecord to AvroSleepSessionRecord`() {
        val now = Instant.now()
        val fetchedTime = System.currentTimeMillis()

        // Construct the Health Connect SleepSessionRecord
        val hcSleepSessionRecord = SleepSessionRecord(
            title = "Afternoon Nap",
            notes = "Felt refreshing",
            startTime = now.minusSeconds(7200), // 2 hours ago
            startZoneOffset = ZoneOffset.UTC,
            endTime = now.minusSeconds(3600),   // 1 hour ago
            endZoneOffset = ZoneOffset.UTC,
            stages = listOf(
                SleepSessionRecord.Stage(
                    startTime = now.minusSeconds(7200), // Start of session
                    endTime = now.minusSeconds(6300),   // After 30 mins
                    stage = SleepSessionRecord.STAGE_TYPE_AWAKE // Corrected: stage to type
                ),
                SleepSessionRecord.Stage(
                    startTime = now.minusSeconds(6300),
                    endTime = now.minusSeconds(4500),
                    stage = SleepSessionRecord.STAGE_TYPE_LIGHT // Corrected: stage to type
                ),
                SleepSessionRecord.Stage(
                    startTime = now.minusSeconds(4500),
                    endTime = now.minusSeconds(3600),   // End of session
                    stage = SleepSessionRecord.STAGE_TYPE_REM // Corrected: stage to type
                )
            ),
            metadata = Metadata.manualEntry(
                clientRecordId = "client-sleep-id-003",
                clientRecordVersion = 1L,
                device = Device(manufacturer = "SleepCo", model = "DreamCatcher", type = TYPE_UNKNOWN)
            )
        )

        // Define the expected AvroSleepSessionRecord
        val expectedAvroRecord = AvroSleepSessionRecord(
            hcUid = hcSleepSessionRecord.metadata.id,
            title = hcSleepSessionRecord.title,
            notes = hcSleepSessionRecord.notes,
            startTimeEpochMillis = hcSleepSessionRecord.startTime.toEpochMilli(),
            endTimeEpochMillis = hcSleepSessionRecord.endTime.toEpochMilli(),
            startZoneOffsetId = hcSleepSessionRecord.startZoneOffset?.id,
            endZoneOffsetId = hcSleepSessionRecord.endZoneOffset?.id,
            durationMillis = hcSleepSessionRecord.endTime.toEpochMilli() - hcSleepSessionRecord.startTime.toEpochMilli(), // Calculated for verification
            dataOriginPackageName = hcSleepSessionRecord.metadata.dataOrigin.packageName,
            hcLastModifiedTimeEpochMillis = hcSleepSessionRecord.metadata.lastModifiedTime.toEpochMilli(),
            clientRecordId = hcSleepSessionRecord.metadata.clientRecordId,
            clientRecordVersion = hcSleepSessionRecord.metadata.clientRecordVersion,
            appRecordFetchTimeEpochMillis = fetchedTime,
            stages = listOf(
                AvroSleepStageRecord(
                    startTimeEpochMillis = now.minusSeconds(7200).toEpochMilli(),
                    endTimeEpochMillis = now.minusSeconds(6300).toEpochMilli(),
                    stage = AvroSleepStageType.AWAKE
                ),
                AvroSleepStageRecord(
                    startTimeEpochMillis = now.minusSeconds(6300).toEpochMilli(),
                    endTimeEpochMillis = now.minusSeconds(4500).toEpochMilli(),
                    stage = AvroSleepStageType.LIGHT
                ),
                AvroSleepStageRecord(
                    startTimeEpochMillis = now.minusSeconds(4500).toEpochMilli(),
                    endTimeEpochMillis = now.minusSeconds(3600).toEpochMilli(),
                    stage = AvroSleepStageType.REM
                )
            )
        )

        // After implementation, the assertion will be:
        val actualAvroRecord = HealthConnectToAvroMapper.mapSleepSessionRecord(hcSleepSessionRecord, fetchedTime)
        assertEquals(expectedAvroRecord, actualAvroRecord)
    }
}
