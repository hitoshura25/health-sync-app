package io.github.hitoshura25.healthsyncapp.file

import android.content.Context
import android.util.Log
import com.github.avrokotlin.avro4k.Avro
import com.github.avrokotlin.avro4k.AvroObjectContainer
import com.github.avrokotlin.avro4k.ExperimentalAvro4kApi
import com.github.avrokotlin.avro4k.schema
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializerOrNull
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class FileHandlerImpl(private val context: Context) : FileHandler {

    companion object {
        private const val AVRO_STAGING_SUBDIR = "avro_staging"
        private const val AVRO_COMPLETED_SUBDIR = "avro_completed"
        private const val TAG = "FileHandlerImpl"
    }

    override fun <T : Any> writeAvroFile(records: Sequence<T>, outputFile: File): Boolean {
        val recordList = records.toList()
        if (recordList.isEmpty()) {
            Log.i(TAG, "No records to write for ${outputFile.name}. Skipping file creation.")
            return true // Considered success as there's nothing to do.
        }

        // It's crucial that T is a concrete, serializable type.
        // We get the serializer for the actual type of the elements in the list.
        val firstElement = recordList.first() // Get the first element to determine its class
        val elementSerializer = Avro.serializersModule.serializerOrNull(firstElement::class.java)
            ?: run {
                Log.e(TAG, "Could not find Avro serializer for type: ${firstElement::class.java.name}")
                return false
            }

        // Suppress UNCHECKED_CAST: The list and serializer are for the same underlying type T.
        // This relies on the caller ensuring all elements in `records` are of the same serializable type.
        @Suppress("UNCHECKED_CAST")
        return writeTypedAvroFile(
            elementSerializer as KSerializer<T>, // Cast to KSerializer<T>
            recordList, // recordList is already List<T>
            outputFile,
        )
    }

    @OptIn(ExperimentalAvro4kApi::class)
    private fun <ConcreteType : Any> writeTypedAvroFile(
        serializer: KSerializer<ConcreteType>,
        typedRecords: List<ConcreteType>,
        outputFile: File
    ): Boolean {
        return try {
            // Ensure parent directory exists
            outputFile.parentFile?.mkdirs()

            val schema = Avro.schema(serializer)
            Files.newOutputStream(outputFile.toPath()).buffered().use { stream ->
                AvroObjectContainer.openWriter(schema, serializer, stream).use { writer ->
                    typedRecords.forEach { typedRecord -> writer.writeValue(typedRecord) }
                }
            }
            Log.d(TAG, "Successfully wrote ${typedRecords.size} records to ${outputFile.absolutePath}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error writing Avro file ${outputFile.absolutePath}", e)
            false
        }
    }

    override fun getStagingDirectory(): File {
        return File(context.filesDir, AVRO_STAGING_SUBDIR).also {
            if (!it.exists()) {
                it.mkdirs()
            }
        }
    }

    override fun getCompletedDirectory(): File {
        return File(context.filesDir, AVRO_COMPLETED_SUBDIR).also {
            if (!it.exists()) {
                it.mkdirs()
            }
        }
    }

    override fun listFiles(directory: File): List<File> {
        return if (directory.exists() && directory.isDirectory) {
            directory.listFiles()?.toList() ?: emptyList()
        } else {
            if (!directory.exists()) Log.w(TAG, "Directory does not exist: ${directory.absolutePath}")
            else if (!directory.isDirectory) Log.w(TAG, "Path is not a directory: ${directory.absolutePath}")
            emptyList()
        }
    }

    override fun moveFile(sourceFile: File, destinationFile: File): Boolean {
        return try {
            if (!sourceFile.exists()) {
                Log.w(TAG, "Source file for move does not exist: ${sourceFile.absolutePath}")
                return false
            }
            // Ensure destination directory exists
            destinationFile.parentFile?.mkdirs()
            // Using Files.move for potentially more atomic operation and better error handling
            Files.move(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            Log.d(TAG, "Successfully moved ${sourceFile.absolutePath} to ${destinationFile.absolutePath}")
            true
        } catch (e: IOException) {
            Log.e(TAG, "Error moving file from ${sourceFile.absolutePath} to ${destinationFile.absolutePath}", e)
            // Attempt simple rename as a fallback if it's a different kind of IO error
            // and then re-check. This is a bit optimistic.
            val simpleRenameSuccess = sourceFile.renameTo(destinationFile)
            if (simpleRenameSuccess) {
                 Log.d(TAG, "Successfully moved (via renameTo fallback) ${sourceFile.absolutePath} to ${destinationFile.absolutePath}")
            }
            simpleRenameSuccess
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error moving file from ${sourceFile.absolutePath} to ${destinationFile.absolutePath}", e)
            false
        }
    }

    override fun deleteFile(file: File): Boolean {
        return try {
            if (!file.exists()) {
                Log.w(TAG, "File to delete does not exist: ${file.absolutePath}")
                return true // Or false, depending on desired idempotency. True means "it's gone".
            }
            val success = file.delete()
            if (success) {
                Log.d(TAG, "Successfully deleted file: ${file.absolutePath}")
            } else {
                Log.w(TAG, "Failed to delete file: ${file.absolutePath}")
            }
            success
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException deleting file: ${file.absolutePath}", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error deleting file: ${file.absolutePath}", e)
            false
        }
    }
}
