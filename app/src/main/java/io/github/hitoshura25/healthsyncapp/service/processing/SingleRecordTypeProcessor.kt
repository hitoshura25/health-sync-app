@file:OptIn(ExperimentalAvro4kApi::class)
package io.github.hitoshura25.healthsyncapp.service.processing

import android.util.Log
import com.github.avrokotlin.avro4k.Avro
import com.github.avrokotlin.avro4k.AvroObjectContainer
import com.github.avrokotlin.avro4k.ExperimentalAvro4kApi
import kotlinx.serialization.serializer
import java.io.File
import java.nio.file.Files

class SingleRecordTypeProcessor<AvroType : Any, EntityType : Any>(
    private val toEntityMapper: (AvroType) -> EntityType,
    private val daoInsertFunction: suspend (List<EntityType>) -> Unit,
    private val recordTypeName: String,
    private val avroTypeClass: Class<AvroType>,
    private val batchSize: Int = 1000
) : RecordProcessor {

    private val TAG = "SingleRecordTypeProcessor"

    override suspend fun process(file: File): Boolean {
        return try {
            Files.newInputStream(file.toPath()).buffered().use { stream ->
                @Suppress("UNCHECKED_CAST")
                val avroRecordsSequence = AvroObjectContainer.decodeFromStream(
                    Avro.serializersModule.serializer(avroTypeClass),
                    stream,
                ) as Sequence<AvroType>

                var totalInserted = 0
                val chunkedSequence = avroRecordsSequence.chunked(batchSize)

                chunkedSequence.forEach { chunk ->
                    if (chunk.isNotEmpty()) {
                        val entities = chunk.map { toEntityMapper(it) }
                        daoInsertFunction(entities)
                        totalInserted += entities.size
                        Log.d(TAG, "Inserted a batch of ${entities.size} $recordTypeName entities.")
                    }
                }

                if (totalInserted > 0) {
                    Log.i(TAG, "Successfully inserted a total of $totalInserted $recordTypeName entities from ${file.name}")
                } else {
                    Log.i(TAG, "No $recordTypeName objects found to insert in ${file.name}.")
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error processing Avro file ${file.name} for type $recordTypeName", e)
            false
        }
    }
}