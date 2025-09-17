package io.github.hitoshura25.healthsyncapp.avro

import com.github.avrokotlin.avro4k.AvroObjectContainer
import com.github.avrokotlin.avro4k.ExperimentalAvro4kApi
import com.github.avrokotlin.avro4k.decodeFromStream
import com.github.avrokotlin.avro4k.openWriter
import kotlinx.io.Buffer
import kotlinx.io.asInputStream
import kotlinx.io.asOutputStream
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.Assert.assertEquals
import org.junit.Test


@OptIn(ExperimentalSerializationApi::class, ExperimentalAvro4kApi::class)
class AvroSerializationTest {

    private fun createTestMetadata(id: String): AvroMetadata {
        return AvroMetadata(
            id = id,
            dataOriginPackageName = "com.example.healthapp",
            lastModifiedTimeEpochMillis = 1672534860000L, // A bit after end time
            clientRecordId = "client-xyz",
            clientRecordVersion = 1L,
            device = AvroDevice("Google", "Pixel Watch", "WATCH")
        )
    }

    @Test
    fun `AvroStepsRecord data file should serialize and deserialize correctly`() {
        val originalRecord = AvroStepsRecord(
            metadata = createTestMetadata("uid-123"),
            startTimeEpochMillis = 1672531200000L, // 2023-01-01 00:00:00 UTC
            endTimeEpochMillis = 1672534800000L,   // 2023-01-01 01:00:00 UTC
            startZoneOffsetId = "UTC",
            endZoneOffsetId = "UTC",
            count = 1500L,
            appRecordFetchTimeEpochMillis = System.currentTimeMillis()
        )

        val buffer = Buffer()
        val valuesToEncode = sequenceOf(originalRecord)

        buffer.asOutputStream().use { stream ->
            AvroObjectContainer.openWriter<AvroStepsRecord>(stream).use { writer ->
                valuesToEncode.forEach { record ->
                    writer.writeValue(record)
                }
            }
        }

        assert(buffer.size != 0L)

        val deserializedRecord = buffer.asInputStream().use { stream ->
            AvroObjectContainer.decodeFromStream<AvroStepsRecord>(stream)
        }.first()

        assertEquals("Deserialized record from data file should match original", originalRecord, deserializedRecord)
    }

    @Test
    fun `AvroHeartRateRecord data file should serialize and deserialize correctly`() {
        val originalRecord = AvroHeartRateRecord(
            metadata = createTestMetadata("uid-hr-456"),
            startTimeEpochMillis = 1672531200000L, // 2023-01-01 00:00:00 UTC
            endTimeEpochMillis = 1672531500000L,   // 2023-01-01 00:05:00 UTC
            startZoneOffsetId = "America/New_York",
            endZoneOffsetId = "America/New_York",
            appRecordFetchTimeEpochMillis = System.currentTimeMillis(),
            samples = listOf(
                AvroHeartRateSample(timeEpochMillis = 1672531260000L, beatsPerMinute = 75L),
                AvroHeartRateSample(timeEpochMillis = 1672531320000L, beatsPerMinute = 78L),
                AvroHeartRateSample(timeEpochMillis = 1672531380000L, beatsPerMinute = 76L)
            )
        )

        val buffer = Buffer()
        val valuesToEncode = sequenceOf(originalRecord)

        buffer.asOutputStream().use { stream ->
            AvroObjectContainer.openWriter<AvroHeartRateRecord>(stream).use { writer ->
                valuesToEncode.forEach { record ->
                    writer.writeValue(record)
                }
            }
        }

        assert(buffer.size != 0L) { "Buffer should not be empty after writing Avro data file" }

        val deserializedRecord = buffer.asInputStream().use { stream ->
            AvroObjectContainer.decodeFromStream<AvroHeartRateRecord>(stream)
        }.first()

        assertEquals("Deserialized HeartRateRecord from data file should match original", originalRecord, deserializedRecord)
    }

    @Test
    fun `AvroSleepSessionRecord data file should serialize and deserialize correctly`() {
        val originalRecord = AvroSleepSessionRecord(
            metadata = createTestMetadata("uid-sleep-789"),
            title = "Nightly Sleep",
            notes = "Felt well rested",
            startTimeEpochMillis = 1672531200000L, // 2023-01-01 00:00:00 UTC
            endTimeEpochMillis = 1672552800000L,   // 2023-01-01 06:00:00 UTC
            startZoneOffsetId = "Europe/Paris",
            endZoneOffsetId = "Europe/Paris",
            durationMillis = 21600000L, // 6 hours
            appRecordFetchTimeEpochMillis = System.currentTimeMillis(),
            stages = listOf(
                AvroSleepStageRecord(
                    startTimeEpochMillis = 1672531200000L, // 00:00
                    endTimeEpochMillis = 1672533000000L,   // 00:30
                    stage = AvroSleepStageType.AWAKE
                ),
                AvroSleepStageRecord(
                    startTimeEpochMillis = 1672533000000L, // 00:30
                    endTimeEpochMillis = 1672538400000L,   // 02:00
                    stage = AvroSleepStageType.LIGHT
                ),
                AvroSleepStageRecord(
                    startTimeEpochMillis = 1672538400000L, // 02:00
                    endTimeEpochMillis = 1672545600000L,   // 04:00
                    stage = AvroSleepStageType.DEEP
                ),
                AvroSleepStageRecord(
                    startTimeEpochMillis = 1672545600000L, // 04:00
                    endTimeEpochMillis = 1672552800000L,   // 06:00
                    stage = AvroSleepStageType.REM
                )
            )
        )

        val buffer = Buffer()
        val valuesToEncode = sequenceOf(originalRecord)

        buffer.asOutputStream().use { stream ->
            AvroObjectContainer.openWriter<AvroSleepSessionRecord>(stream).use { writer ->
                valuesToEncode.forEach { record ->
                    writer.writeValue(record)
                }
            }
        }

        assert(buffer.size != 0L) { "Buffer should not be empty after writing Avro data file" }

        val deserializedRecord = buffer.asInputStream().use { stream ->
            AvroObjectContainer.decodeFromStream<AvroSleepSessionRecord>(stream)
        }.first()

        assertEquals("Deserialized SleepSessionRecord from data file should match original", originalRecord, deserializedRecord)
    }
}
