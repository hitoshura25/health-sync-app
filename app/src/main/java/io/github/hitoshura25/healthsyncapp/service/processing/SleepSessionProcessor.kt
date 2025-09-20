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
            Files.newInputStream(file.toPath()).buffered().use { stream ->
                val avroRecords = AvroObjectContainer.decodeFromStream<AvroSleepSessionRecord>(stream)
                var recordCount = 0
                var success = true

                avroRecords.forEach { avroRecord ->
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
                        recordCount++
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to process and insert session ${avroRecord.metadata.id} from file ${file.name}", e)
                        success = false
                        return@forEach // Continue to the next record
                    }
                }

                if (recordCount > 0) {
                    Log.i(TAG, "Successfully processed $recordCount sleep sessions from ${file.name}")
                } else {
                    Log.i(TAG, "No sleep sessions to process in ${file.name}")
                }
                return success
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing Avro file ${file.name} for type SleepSession", e)
            return false
        }
    }
}
