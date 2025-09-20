@file:OptIn(ExperimentalAvro4kApi::class)
package io.github.hitoshura25.healthsyncapp.service.processing

import android.util.Log
import androidx.room.withTransaction
import com.github.avrokotlin.avro4k.AvroObjectContainer
import com.github.avrokotlin.avro4k.ExperimentalAvro4kApi
import com.github.avrokotlin.avro4k.decodeFromStream
import io.github.hitoshura25.healthsyncapp.data.avro.AvroPowerRecord
import io.github.hitoshura25.healthsyncapp.data.local.database.AppDatabase
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.PowerRecordDao
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toPowerRecordEntity
import java.io.File
import java.nio.file.Files
import javax.inject.Inject

class PowerRecordProcessor @Inject constructor(
    private val appDatabase: AppDatabase,
    private val powerRecordDao: PowerRecordDao
) : RecordProcessor {
    private val TAG = "PowerRecordProcessor"

    override suspend fun process(file: File): Boolean {
        try {
            val avroRecords = Files.newInputStream(file.toPath()).buffered().use { stream ->
                AvroObjectContainer.decodeFromStream<AvroPowerRecord>(stream).toList()
            }
            if (avroRecords.isEmpty()) {
                Log.i(TAG, "No PowerRecord objects found in ${file.name}, file is empty.")
                return true
            }

            var allTransactionsSucceeded = true
            for (avroRecord in avroRecords) {
                try {
                    val (recordEntity, sampleEntities) = avroRecord.toPowerRecordEntity()
                    appDatabase.withTransaction {
                        powerRecordDao.insertRecordWithSamples(recordEntity, sampleEntities)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to process and insert PowerRecord ${avroRecord.metadata.id} from file ${file.name}", e)
                    allTransactionsSucceeded = false
                    break
                }
            }
            return allTransactionsSucceeded
        } catch (e: Exception) {
            Log.e(TAG, "Error processing Avro file ${file.name} for type Power", e)
            return false
        }
    }
}
