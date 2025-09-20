@file:OptIn(ExperimentalAvro4kApi::class)
package io.github.hitoshura25.healthsyncapp.service.processing

import android.util.Log
import com.github.avrokotlin.avro4k.AvroObjectContainer
import com.github.avrokotlin.avro4k.ExperimentalAvro4kApi
import com.github.avrokotlin.avro4k.decodeFromStream
import io.github.hitoshura25.healthsyncapp.data.avro.AvroHeartRateRecord
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.HeartRateSampleDao
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toHeartRateSampleEntity
import java.io.File
import java.nio.file.Files
import javax.inject.Inject

class HeartRateRecordProcessor @Inject constructor(
    private val heartRateSampleDao: HeartRateSampleDao
) : RecordProcessor {
    private val TAG = "HeartRateRecordProcessor"

    override suspend fun process(file: File): Boolean {
        return try {
            Files.newInputStream(file.toPath()).buffered().use { stream ->
                val avroRecords = AvroObjectContainer.decodeFromStream<AvroHeartRateRecord>(stream)
                var totalSamplesInserted = 0
                avroRecords.forEach { avroRecord ->
                    if (avroRecord.samples.isNotEmpty()) {
                        val entities = avroRecord.samples.map {
                            it.toHeartRateSampleEntity(
                                recordUid = avroRecord.metadata.id,
                                appRecordFetchTimeEpochMillis = avroRecord.appRecordFetchTimeEpochMillis,
                                dataOriginPackageName = avroRecord.metadata.dataOriginPackageName,
                                hcLastModifiedTimeEpochMillis = avroRecord.metadata.lastModifiedTimeEpochMillis,
                                clientRecordId = avroRecord.metadata.clientRecordId,
                                clientRecordVersion = avroRecord.metadata.clientRecordVersion,
                                deviceManufacturer = avroRecord.metadata.device?.manufacturer,
                                deviceModel = avroRecord.metadata.device?.model,
                                deviceType = avroRecord.metadata.device?.type
                            )
                        }
                        heartRateSampleDao.insertAll(entities)
                        totalSamplesInserted += entities.size
                    }
                }
                if (totalSamplesInserted > 0) {
                    Log.i(TAG, "Inserted $totalSamplesInserted HeartRateSample entities from ${file.name}")
                } else {
                    Log.i(TAG, "No HeartRateSample objects found in ${file.name}, file is empty.")
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error processing Avro file ${file.name} for type HeartRateRecord", e)
            false
        }
    }
}
