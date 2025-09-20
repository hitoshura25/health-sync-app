@file:OptIn(ExperimentalAvro4kApi::class)
package io.github.hitoshura25.healthsyncapp.service.processing

import android.util.Log
import androidx.room.withTransaction
import com.github.avrokotlin.avro4k.AvroObjectContainer
import com.github.avrokotlin.avro4k.ExperimentalAvro4kApi
import com.github.avrokotlin.avro4k.decodeFromStream
import io.github.hitoshura25.healthsyncapp.data.avro.AvroCyclingPedalingCadenceRecord
import io.github.hitoshura25.healthsyncapp.data.local.database.AppDatabase
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.CyclingPedalingCadenceRecordDao
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toCyclingPedalingCadenceRecordEntity
import java.io.File
import java.nio.file.Files
import javax.inject.Inject

class CyclingPedalingCadenceRecordProcessor @Inject constructor(
    private val appDatabase: AppDatabase,
    private val cyclingPedalingCadenceRecordDao: CyclingPedalingCadenceRecordDao
) : RecordProcessor {
    private val TAG = "CyclingPedalingCadenceRecordProcessor"

    override suspend fun process(file: File): Boolean {
        try {
            val avroRecords = Files.newInputStream(file.toPath()).buffered().use { stream ->
                AvroObjectContainer.decodeFromStream<AvroCyclingPedalingCadenceRecord>(stream).toList()
            }
            if (avroRecords.isEmpty()) {
                Log.i(TAG, "No CyclingPedalingCadenceRecord objects found in ${file.name}, file is empty.")
                return true
            }

            var allTransactionsSucceeded = true
            for (avroRecord in avroRecords) {
                try {
                    val (recordEntity, sampleEntities) = avroRecord.toCyclingPedalingCadenceRecordEntity()
                    appDatabase.withTransaction {
                        cyclingPedalingCadenceRecordDao.insertRecordWithSamples(recordEntity, sampleEntities)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to process and insert CyclingPedalingCadenceRecord ${avroRecord.metadata.id} from file ${file.name}", e)
                    allTransactionsSucceeded = false
                    break
                }
            }
            return allTransactionsSucceeded
        } catch (e: Exception) {
            Log.e(TAG, "Error processing Avro file ${file.name} for type CyclingPedalingCadence", e)
            return false
        }
    }
}