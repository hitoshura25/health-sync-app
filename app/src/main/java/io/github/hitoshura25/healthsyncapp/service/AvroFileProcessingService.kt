package io.github.hitoshura25.healthsyncapp.service

import android.util.Log
import io.github.hitoshura25.healthsyncapp.data.HealthConnectConstants.RECORD_TYPES_SUPPORTED
import io.github.hitoshura25.healthsyncapp.data.SupportedHealthRecordType
import io.github.hitoshura25.healthsyncapp.file.FileHandler
import io.github.hitoshura25.healthsyncapp.service.processing.RecordProcessorFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AvroFileProcessingService @Inject constructor(
    private val fileHandler: FileHandler,
    private val processorFactory: RecordProcessorFactory // Inject the factory
) {
    private val TAG = "AvroFileProcService"

    suspend fun processStagedAvroFiles(): Boolean = withContext(Dispatchers.IO) {
        val stagingDir = fileHandler.getStagingDirectory()
        val completedDir = fileHandler.getCompletedDirectory()
        var overallSuccess = true

        Log.d(TAG, "Starting to process files in staging directory: ${stagingDir.absolutePath}")

        val allStagedFiles = fileHandler.listFiles(stagingDir).filter { it.isFile && it.name.endsWith(".avro") }
        if (allStagedFiles.isEmpty()) {
            Log.i(TAG, "No Avro files found in staging directory.")
            return@withContext true
        }

        Log.i(TAG, "Found ${allStagedFiles.size} Avro files to potentially process.")

        RECORD_TYPES_SUPPORTED.forEach { supportedType ->
            val recordTypeName = supportedType.recordKClass.simpleName
            val filesForThisType = allStagedFiles.filter { it.name.startsWith("${recordTypeName}_") }

            if (filesForThisType.isEmpty()) {
                return@forEach
            }

            Log.i(TAG, "Processing ${filesForThisType.size} files for ${recordTypeName}.")

            for (file in filesForThisType) {
                Log.d(TAG, "Processing file: ${file.name}")
                try {
                    // Delegate to the factory and the specific processor
                    val fileProcessedSuccessfully = processAvroFileForType(file, supportedType)

                    if (fileProcessedSuccessfully) {
                        val destinationFile = File(completedDir, file.name)
                        if (fileHandler.moveFile(file, destinationFile)) {
                            Log.i(TAG, "Successfully moved ${file.name} to completed directory.")
                        } else {
                            Log.e(TAG, "Failed to move ${file.name}. It remains in staging.")
                            overallSuccess = false
                        }
                    } else {
                        Log.e(TAG, "File ${file.name} was not processed successfully. Not moving.")
                        overallSuccess = false
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing file ${file.name}. It remains in staging.", e)
                    overallSuccess = false
                }
            }
        }
        return@withContext overallSuccess
    }

    private suspend fun processAvroFileForType(file: File, supportedType: SupportedHealthRecordType<*>): Boolean {
        // The new, simplified logic
        val processor = processorFactory.create(supportedType)
        return processor.process(file)
    }
}