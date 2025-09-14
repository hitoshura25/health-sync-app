package io.github.hitoshura25.healthsyncapp.file

import java.io.File

interface FileHandler {
    /**
     * Writes a sequence of Avro-serializable records to a specified file.
     *
     * @param T The type of the records, which must be @Serializable.
     * @param records The sequence of records to write.
     * @param outputFile The file to write the records to.
     * @return True if writing was successful, false otherwise.
     */
    fun <T : Any> writeAvroFile(records: Sequence<T>, outputFile: File): Boolean
}

// A concrete implementation will be created later and injected into the worker.
// For example:
// class AvroFileHandlerImpl(private val context: Context) : FileHandler { ... }
