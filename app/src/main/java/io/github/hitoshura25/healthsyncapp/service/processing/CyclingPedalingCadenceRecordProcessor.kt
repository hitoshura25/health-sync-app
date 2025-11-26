@file:OptIn(ExperimentalAvro4kApi::class)
package io.github.hitoshura25.healthsyncapp.service.processing

import android.util.Log
import com.github.avrokotlin.avro4k.AvroObjectContainer
import com.github.avrokotlin.avro4k.ExperimentalAvro4kApi
import com.github.avrokotlin.avro4k.decodeFromStream
import io.github.hitoshura25.healthsyncapp.data.avro.AvroCyclingPedalingCadenceRecord
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.CyclingPedalingCadenceRecordDao
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toCyclingPedalingCadenceRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toCyclingPedalingCadenceSampleEntity
import java.io.File
import java.nio.file.Files
import javax.inject.Inject

class CyclingPedalingCadenceRecordProcessor @Inject constructor(
    private val cyclingPedalingCadenceRecordDao: CyclingPedalingCadenceRecordDao
) : RecordProcessor {
    private val TAG = "CyclingPedalingCadenceRecordProcessor"

    override suspend fun process(file: File): Boolean {
        return try {
            Files.newInputStream(file.toPath()).buffered().use { stream ->
                val avroRecords = AvroObjectContainer.decodeFromStream<AvroCyclingPedalingCadenceRecord>(stream)
                var totalRecordsInserted = 0
                avroRecords.forEach { avroRecord ->
                    val recordEntity = avroRecord.toCyclingPedalingCadenceRecordEntity()
                    val sampleEntities = avroRecord.samples.map {
                        it.toCyclingPedalingCadenceSampleEntity(parentRecordUid = recordEntity.hcUid)
                    }
                    cyclingPedalingCadenceRecordDao.insertRecordWithSamples(recordEntity, sampleEntities)
                    totalRecordsInserted++
                }
                if (totalRecordsInserted > 0) {
                    Log.i(TAG, "Inserted $totalRecordsInserted CyclingPedalingCadenceRecord records from ${file.name}")
                } else {
                    Log.i(TAG, "No CyclingPedalingCadenceRecord objects found in ${file.name}, file is empty.")
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error processing Avro file ${file.name} for type CyclingPedalingCadenceRecord", e)
            false
        }
    }
}
