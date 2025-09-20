@file:OptIn(ExperimentalAvro4kApi::class)
package io.github.hitoshura25.healthsyncapp.service.processing

import android.util.Log
import com.github.avrokotlin.avro4k.AvroObjectContainer
import com.github.avrokotlin.avro4k.ExperimentalAvro4kApi
import com.github.avrokotlin.avro4k.decodeFromStream
import io.github.hitoshura25.healthsyncapp.data.avro.AvroHeartRateRecord
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.HeartRateSampleDao
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toHeartRateSampleEntities
import java.io.File
import java.nio.file.Files
import javax.inject.Inject

class HeartRateRecordProcessor @Inject constructor(
    private val heartRateSampleDao: HeartRateSampleDao
) : RecordProcessor {
    private val TAG = "HeartRateRecordProcessor"

    override suspend fun process(file: File): Boolean {
        return try {
            val avroHeartRateRecords = Files.newInputStream(file.toPath()).buffered().use { stream ->
                AvroObjectContainer.decodeFromStream<AvroHeartRateRecord>(stream).toList()
            }
            if (avroHeartRateRecords.isNotEmpty()) {
                val allSampleEntities = avroHeartRateRecords.flatMap { avroRecord ->
                    avroRecord.toHeartRateSampleEntities()
                }
                if (allSampleEntities.isNotEmpty()){
                    heartRateSampleDao.insertAll(allSampleEntities)
                    Log.i(TAG, "Inserted ${allSampleEntities.size} HeartRateSample entities from ${file.name}")
                } else {
                    Log.i(TAG, "No actual samples found within HeartRateRecord(s) in ${file.name}.")
                }
            } else {
                Log.i(TAG, "No HeartRateRecord objects found in ${file.name}, file is empty.")
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error processing Avro file ${file.name} for type HeartRate", e)
            false
        }
    }
}
