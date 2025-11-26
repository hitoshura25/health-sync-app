@file:OptIn(ExperimentalAvro4kApi::class)
package io.github.hitoshura25.healthsyncapp.service.processing

import android.util.Log
import com.github.avrokotlin.avro4k.AvroObjectContainer
import com.github.avrokotlin.avro4k.ExperimentalAvro4kApi
import com.github.avrokotlin.avro4k.decodeFromStream
import io.github.hitoshura25.healthsyncapp.data.avro.AvroPowerRecord
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.PowerRecordDao
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toPowerRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toPowerSampleEntity
import java.io.File
import java.nio.file.Files
import javax.inject.Inject

class PowerRecordProcessor @Inject constructor(
    private val powerRecordDao: PowerRecordDao
) : RecordProcessor {
    private val TAG = "PowerRecordProcessor"

    override suspend fun process(file: File): Boolean {
        return try {
            Files.newInputStream(file.toPath()).buffered().use { stream ->
                val avroRecords = AvroObjectContainer.decodeFromStream<AvroPowerRecord>(stream)
                var totalRecordsInserted = 0
                avroRecords.forEach { avroRecord ->
                    val recordEntity = avroRecord.toPowerRecordEntity()
                    val sampleEntities = avroRecord.samples.map {
                        it.toPowerSampleEntity(parentRecordUid = recordEntity.hcUid)
                    }
                    powerRecordDao.insertRecordWithSamples(recordEntity, sampleEntities)
                    totalRecordsInserted++
                }
                if (totalRecordsInserted > 0) {
                    Log.i(TAG, "Inserted $totalRecordsInserted PowerRecord records from ${file.name}")
                } else {
                    Log.i(TAG, "No PowerRecord objects found in ${file.name}, file is empty.")
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error processing Avro file ${file.name} for type PowerRecord", e)
            false
        }
    }
}
