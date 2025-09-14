package io.github.hitoshura25.healthsyncapp.file

import java.io.File
import android.util.Log
import com.github.avrokotlin.avro4k.Avro
import com.github.avrokotlin.avro4k.AvroObjectContainer
import com.github.avrokotlin.avro4k.ExperimentalAvro4kApi
import com.github.avrokotlin.avro4k.schema
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializerOrNull
import java.nio.file.Files

class FileHandlerImpl : FileHandler {

    override fun <T : Any> writeAvroFile(records: Sequence<T>, outputFile: File): Boolean {
        val recordList = records.toList()
        if (recordList.isEmpty()) {
            Log.i("FileHandlerImpl", "No records to write for ${outputFile.name}. Skipping file creation.")
            return true
        }

        val serializer = Avro.serializersModule.serializerOrNull(recordList.first()::class.java)
        // Call a private, strongly-typed helper using the concrete KClass and its serializer.
        // The cast to List<T> for recordList is safe here because we derived the serializer from its elements.

        @Suppress("UNCHECKED_CAST")
        return writeTypedAvroFile(
            serializer as KSerializer<T>,
            recordList, // Already List<T>
            outputFile,
        )
    }

    // Private helper function where ConcreteType is known and used for type-safe Avro operations.
    // In this specific setup, ConcreteType will be the same as T from the public method due to how elementSerializer is derived.
    @OptIn(ExperimentalAvro4kApi::class)
    private fun <ConcreteType : Any> writeTypedAvroFile(
        serializer: KSerializer<ConcreteType>,
        typedRecords: List<ConcreteType>,
        outputFile: File
    ): Boolean {
        val result = runCatching {
            val schema = Avro.schema(serializer)
            Files.newOutputStream(outputFile.toPath()).buffered().use { stream ->
                AvroObjectContainer.openWriter(schema, serializer, stream).use { writer ->
                    typedRecords.forEach { typedRecord -> writer.writeValue(typedRecord) }
                }
            }
        }

        return result.isSuccess
    }
}
