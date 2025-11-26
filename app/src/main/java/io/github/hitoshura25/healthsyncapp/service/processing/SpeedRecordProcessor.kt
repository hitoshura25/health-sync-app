@file:OptIn(ExperimentalAvro4kApi::class)
package io.github.hitoshura25.healthsyncapp.service.processing

import android.util.Log
import com.github.avrokotlin.avro4k.AvroObjectContainer
import com.github.avrokotlin.avro4k.ExperimentalAvro4kApi
import com.github.avrokotlin.avro4k.decodeFromStream
import io.github.hitoshura25.healthsyncapp.data.avro.AvroSpeedRecord
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.SpeedRecordDao
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toSpeedRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toSpeedSampleEntity
import java.io.File
import java.nio.file.Files
import javax.inject.Inject

class SpeedRecordProcessor @Inject constructor(
    private val speedRecordDao: SpeedRecordDao
) : RecordProcessor {
    private val TAG = "SpeedRecordProcessor"

    override suspend fun process(file: File): Boolean {
        return try {
            Files.newInputStream(file.toPath()).buffered().use { stream ->
                val avroRecords = AvroObjectContainer.decodeFromStream<AvroSpeedRecord>(stream)
                var totalRecordsInserted = 0
                avroRecords.forEach { avroRecord ->
                    val recordEntity = avroRecord.toSpeedRecordEntity()
                    val sampleEntities = avroRecord.samples.map {
                        it.toSpeedSampleEntity(parentRecordUid = recordEntity.hcUid)
                    }
                    speedRecordDao.insertRecordWithSamples(recordEntity, sampleEntities)
                    totalRecordsInserted++
                }
                if (totalRecordsInserted > 0) {
                    Log.i(TAG, "Inserted $totalRecordsInserted SpeedRecord records from ${file.name}")
                } else {
                    Log.i(TAG, "No SpeedRecord objects found in ${file.name}, file is empty.")
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error processing Avro file ${file.name} for type SpeedRecord", e)
            false
        }
    }
}
