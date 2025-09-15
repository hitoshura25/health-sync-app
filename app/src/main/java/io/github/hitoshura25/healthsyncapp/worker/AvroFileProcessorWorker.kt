package io.github.hitoshura25.healthsyncapp.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.hitoshura25.healthsyncapp.service.AvroFileProcessingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class AvroFileProcessorWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val avroFileProcessingService: AvroFileProcessingService
) : CoroutineWorker(appContext, workerParams) {

    private val TAG = "AvroFileProcWorker"

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.i(TAG, "AvroFileProcessorWorker started.")
        try {
            val success = avroFileProcessingService.processStagedAvroFiles()
            if (success) {
                Log.i(TAG, "AvroFileProcessingService completed successfully.")
                Result.success()
            } else {
                Log.w(TAG, "AvroFileProcessingService reported one or more files failed to process or move.")
                // If any file or part of a file (like a single sleep session in a multi-session file) fails,
                // the service returns false. This is treated as a failure for the worker.
                Result.failure()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during Avro file processing in worker.", e)
            // Consider if retry is appropriate. If the error is likely transient (e.g., network issues if service called remote things,
            // or temporary DB lock), retry might be good. For parsing/data errors, failure might be better.
            Result.retry() 
        }
    }
}
