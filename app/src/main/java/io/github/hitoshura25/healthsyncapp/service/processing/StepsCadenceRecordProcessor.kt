@file:OptIn(ExperimentalAvro4kApi::class)
package io.github.hitoshura25.healthsyncapp.service.processing

import android.util.Log
import com.github.avrokotlin.avro4k.AvroObjectContainer
import com.github.avrokotlin.avro4k.ExperimentalAvro4kApi
import com.github.avrokotlin.avro4k.decodeFromStream
import io.github.hitoshura25.healthsyncapp.data.avro.AvroStepsCadenceRecord
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.StepsCadenceRecordDao
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toStepsCadenceRecordEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toStepsCadenceSampleEntity
import java.io.File
import java.nio.file.Files
import javax.inject.Inject

class StepsCadenceRecordProcessor @Inject constructor(
    private val stepsCadenceRecordDao: StepsCadenceRecordDao
) : RecordProcessor {
    private val TAG = "StepsCadenceRecordProcessor"

    override suspend fun process(file: File): Boolean {
        return try {
            Files.newInputStream(file.toPath()).buffered().use { stream ->
                val avroRecords = AvroObjectContainer.decodeFromStream<AvroStepsCadenceRecord>(stream)
                var totalRecordsInserted = 0
                avroRecords.forEach { avroRecord ->
                    val recordEntity = avroRecord.toStepsCadenceRecordEntity()
                    val sampleEntities = avroRecord.samples.map {
                        it.toStepsCadenceSampleEntity(parentRecordUid = recordEntity.hcUid)
                    }
                    stepsCadenceRecordDao.insertRecordWithSamples(recordEntity, sampleEntities)
                    totalRecordsInserted++
                }
                if (totalRecordsInserted > 0) {
                    Log.i(TAG, "Inserted $totalRecordsInserted StepsCadenceRecord records from ${file.name}")
                } else {
                    Log.i(TAG, "No StepsCadenceRecord objects found in ${file.name}, file is empty.")
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error processing Avro file ${file.name} for type StepsCadenceRecord", e)
            false
        }
    }
}
