package io.github.hitoshura25.healthsyncapp.service.processing

import java.io.File

/**
 * Interface for processing a specific type of health data from an Avro file.
 */
interface RecordProcessor {
    /**
     * Processes the given Avro file.
     * @param file The Avro file to process.
     * @return `true` if the file was processed successfully, `false` otherwise.
     */
    suspend fun process(file: File): Boolean
}
