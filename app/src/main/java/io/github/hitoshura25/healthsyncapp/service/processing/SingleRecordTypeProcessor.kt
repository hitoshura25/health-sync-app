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
    private val avroTypeClass: Class<AvroType>
) : RecordProcessor {

    private val TAG = "SingleRecordTypeProcessor"

    override suspend fun process(file: File): Boolean {
        return try {
            @Suppress("UNCHECKED_CAST")
            val avroRecords = Files.newInputStream(file.toPath()).buffered().use { stream ->
                AvroObjectContainer.decodeFromStream(
                    Avro.serializersModule.serializer(avroTypeClass),
                    stream,
                ).toList() as List<AvroType>
            }
            if (avroRecords.isNotEmpty()) {
                val entities = avroRecords.map { toEntityMapper(it) }
                daoInsertFunction(entities)
                Log.i(TAG, "Inserted ${entities.size} $recordTypeName entities from ${file.name}")
            } else {
                Log.i(TAG, "No $recordTypeName objects found in ${file.name}, file is empty.")
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error processing Avro file ${file.name} for type $recordTypeName", e)
            false
        }
    }
}