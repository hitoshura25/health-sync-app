@file:OptIn(ExperimentalAvro4kApi::class)
package io.github.hitoshura25.healthsyncapp.service.processing

import android.util.Log
import androidx.room.withTransaction
import com.github.avrokotlin.avro4k.AvroObjectContainer
import com.github.avrokotlin.avro4k.ExperimentalAvro4kApi
import com.github.avrokotlin.avro4k.decodeFromStream
import io.github.hitoshura25.healthsyncapp.data.avro.AvroSleepSessionRecord
import io.github.hitoshura25.healthsyncapp.data.local.database.AppDatabase
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.SleepSessionDao
import io.github.hitoshura25.healthsyncapp.data.local.database.dao.SleepStageDao
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toSleepSessionEntity
import io.github.hitoshura25.healthsyncapp.data.mapper.avroToRoom.toSleepStageEntity
import java.io.File
import java.nio.file.Files
import javax.inject.Inject

class SleepSessionProcessor @Inject constructor(
    private val appDatabase: AppDatabase,
    private val sleepSessionDao: SleepSessionDao,
    private val sleepStageDao: SleepStageDao
) : RecordProcessor {
    private val TAG = "SleepSessionProcessor"

    override suspend fun process(file: File): Boolean {
        try {
            val avroRecords = Files.newInputStream(file.toPath()).buffered().use { stream ->
                AvroObjectContainer.decodeFromStream<AvroSleepSessionRecord>(stream).toList()
            }
            if (avroRecords.isEmpty()) {
                Log.i(TAG, "No SleepSessionRecord objects found in ${file.name}, file is empty.")
                return true
            }

            var allTransactionsSucceeded = true
            for (avroRecord in avroRecords) {
                try {
                    val sessionEntity = avroRecord.toSleepSessionEntity()
                    val stageEntities = avroRecord.stages.map { stageAvro ->
                        stageAvro.toSleepStageEntity(sessionHcUidParam = sessionEntity.hcUid)
                    }

                    appDatabase.withTransaction {
                        sleepSessionDao.insertAll(listOf(sessionEntity))
                        if (stageEntities.isNotEmpty()) {
                            sleepStageDao.insertAll(stageEntities)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to process and insert session ${avroRecord.metadata.id} from file ${file.name}", e)
                    allTransactionsSucceeded = false
                    break // Stop processing this file on the first error
                }
            }
            return allTransactionsSucceeded
        } catch (e: Exception) {
            Log.e(TAG, "Error processing Avro file ${file.name} for type SleepSession", e)
            return false
        }
    }
}
