@file:OptIn(ExperimentalAvro4kApi::class)
package io.github.hitoshura25.healthsyncapp.service.processing

import android.util.Log
import com.github.avrokotlin.avro4k.AvroObjectContainer
import com.github.avrokotlin.avro4k.ExperimentalAvro4kApi
import com.github.avrokotlin.avro4k.decodeFromStream
import io.github.hitoshura25.healthsyncapp.data.avro.AvroStepsRecord
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.StepsRecordDao
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toStepsRecordEntity
import java.io.File
import java.nio.file.Files
import javax.inject.Inject

class StepsRecordProcessor @Inject constructor(
    private val stepsRecordDao: StepsRecordDao
) : RecordProcessor {
    private val TAG = "StepsRecordProcessor"

    override suspend fun process(file: File): Boolean {
        return try {
            val avroRecords = Files.newInputStream(file.toPath()).buffered().use { stream ->
                AvroObjectContainer.decodeFromStream<AvroStepsRecord>(stream).toList()
            }
            if (avroRecords.isNotEmpty()) {
                val entities = avroRecords.map { it.toStepsRecordEntity() }
                stepsRecordDao.insertAll(entities)
                Log.i(TAG, "Inserted ${entities.size} StepsRecord entities from ${file.name}")
            } else {
                Log.i(TAG, "No StepsRecord objects found in ${file.name}, file is empty.")
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error processing Avro file ${file.name} for type StepsRecord", e)
            false
        }
    }
}
