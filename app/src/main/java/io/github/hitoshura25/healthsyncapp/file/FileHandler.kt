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

    /**
     * Returns the application-specific directory intended for staging Avro files.
     * Implementations should ensure this directory exists.
     * @return The staging directory File object.
     */
    fun getStagingDirectory(): File

    /**
     * Returns the application-specific directory intended for completed Avro files.
     * Implementations should ensure this directory exists.
     * @return The completed directory File object.
     */
    fun getCompletedDirectory(): File

    /**
     * Lists all files within the specified directory.
     * @param directory The directory to list files from.
     * @return A list of File objects, or an empty list if the directory is empty or does not exist.
     */
    fun listFiles(directory: File): List<File>

    /**
     * Moves a file from a source path to a target path.
     * @param sourceFile The file to move.
     * @param destinationFile The target file path.
     * @return True if the file was moved successfully, false otherwise.
     */
    fun moveFile(sourceFile: File, destinationFile: File): Boolean

    /**
     * Deletes the specified file.
     * @param file The file to delete.
     * @return True if the file was deleted successfully, false otherwise.
     */
    fun deleteFile(file: File): Boolean

}
