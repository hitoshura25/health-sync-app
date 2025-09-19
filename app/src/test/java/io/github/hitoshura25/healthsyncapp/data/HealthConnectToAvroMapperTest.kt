package io.github.hitoshura25.healthsyncapp.data

import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.metadata.Device.Companion.TYPE_PHONE
import androidx.health.connect.client.records.metadata.Metadata
import io.github.hitoshura25.healthsyncapp.avro.AvroSleepStageType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset
import androidx.health.connect.client.records.metadata.Device as HcDevice

import io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro.mapStepsRecord
import io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro.mapHeartRateRecord
import io.github.hitoshura25.healthsyncapp.data.mapper.healthconnectToAvro.mapSleepSessionRecord

class HealthConnectToAvroMapperTest {

    private fun createHcMetadata(clientRecordId: String, device: HcDevice): Metadata {
        return Metadata.manualEntry(
            clientRecordId = clientRecordId,
            device = device
        )
    }

    @Test
    fun `mapStepsRecord should correctly map HC StepsRecord to AvroStepsRecord`() {
        val now = Instant.now()
        val fetchedTime = System.currentTimeMillis()
        val device = HcDevice(manufacturer = "Google", model = "Pixel Test", type = TYPE_PHONE)
        val metadata = createHcMetadata("client-steps-id-001", device)

        val hcRecord = StepsRecord(
            startTime = now.minusSeconds(3600), // 1 hour ago
            startZoneOffset = ZoneOffset.UTC,
            endTime = now,
            endZoneOffset = ZoneOffset.ofHoursMinutes(5, 30), // Example: IST
            count = 5000L,
            metadata = metadata
        )

        val actualAvroRecord = mapStepsRecord(hcRecord, fetchedTime)

        assertEquals(metadata.id, actualAvroRecord.metadata.id)
        assertEquals(5000L, actualAvroRecord.count)
        assertEquals(device.manufacturer, actualAvroRecord.metadata.device?.manufacturer)
    }

    @Test
    fun `mapHeartRateRecord should correctly map HC HeartRateRecord to AvroHeartRateRecord`() {
        val now = Instant.now()
        val fetchedTime = System.currentTimeMillis()
        val device = HcDevice(manufacturer = "Fit Example", model = "Pulse Pro", type = HcDevice.TYPE_WATCH)
        val metadata = createHcMetadata("client-hr-id-002", device)

        val hcHeartRateRecord = HeartRateRecord(
            startTime = now.minusSeconds(600), // 10 minutes ago
            startZoneOffset = ZoneOffset.ofHours(-5),
            endTime = now.minusSeconds(300), // 5 minutes ago
            endZoneOffset = ZoneOffset.ofHours(-5),
            samples = listOf(
                HeartRateRecord.Sample(time = now.minusSeconds(500), beatsPerMinute = 70L),
                HeartRateRecord.Sample(time = now.minusSeconds(400), beatsPerMinute = 72L)
            ),
            metadata = metadata
        )

        val actualAvroRecord = mapHeartRateRecord(hcHeartRateRecord, fetchedTime)

        assertEquals(metadata.id, actualAvroRecord.metadata.id)
        assertEquals(2, actualAvroRecord.samples.size)
        assertEquals(70L, actualAvroRecord.samples[0].beatsPerMinute)
    }

    @Test
    fun `mapSleepSessionRecord should correctly map HC SleepSessionRecord to AvroSleepSessionRecord`() {
        val now = Instant.now()
        val fetchedTime = System.currentTimeMillis()
        val device = HcDevice(manufacturer = "SleepCo", model = "DreamCatcher", type = HcDevice.TYPE_RING)
        val metadata = createHcMetadata("client-sleep-id-003", device)

        val hcSleepSessionRecord = SleepSessionRecord(
            title = "Afternoon Nap",
            notes = "Felt refreshing",
            startTime = now.minusSeconds(7200), // 2 hours ago
            startZoneOffset = ZoneOffset.UTC,
            endTime = now.minusSeconds(3600),   // 1 hour ago
            endZoneOffset = ZoneOffset.UTC,
            stages = listOf(
                SleepSessionRecord.Stage(
                    startTime = now.minusSeconds(7200),
                    endTime = now.minusSeconds(6300),
                    stage = SleepSessionRecord.STAGE_TYPE_AWAKE
                ),
                SleepSessionRecord.Stage(
                    startTime = now.minusSeconds(6300),
                    endTime = now.minusSeconds(4500),
                    stage = SleepSessionRecord.STAGE_TYPE_LIGHT
                )
            ),
            metadata = metadata
        )

        val actualAvroRecord = mapSleepSessionRecord(hcSleepSessionRecord, fetchedTime)

        assertEquals(metadata.id, actualAvroRecord.metadata.id)
        assertEquals("Afternoon Nap", actualAvroRecord.title)
        assertEquals(2, actualAvroRecord.stages.size)
        assertEquals(AvroSleepStageType.AWAKE, actualAvroRecord.stages[0].stage)
    }
}